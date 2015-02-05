package sge3bayes;

import java.io.*;
import java.util.*;

public class ParallelRunner
{
	public static void main(String[] args)
		throws Exception
	{
		CLIParserFB.parse(args);
		CLIParserFB.parseEnvironment();

		new ParallelRunner().run();
	}

	private void run()
		throws Exception
	{
		RunIDXStats idx = new RunIDXStats();

		// Collect the list of contigs
		ArrayList<Contig> contigs = idx.collect();
		// And the total length across them all
		long length = idx.calculateLength();

		System.out.println("Total length (bases) across all contigs/refseqs: "
			+ length);

		// Holds each argument being passed through to Freebayes
		ArrayList<String> args = new ArrayList<>();


		// 1) Work out how many bases-per-task will be needed
		int tasks = CLIParserFB.sgeTasks;
		int taskID = CLIParserFB.sgeTaskID - 1;
		long[] values = calcStartEnd(length, tasks, taskID);

		// 2) Now get the starting and ending bases for this task
		long start = values[0];
		long end = values[1];

		// 3) For each contig, decide if it's being handled by this task and if
		// so, what start/end values it should use
		for (Contig c: contigs)
		{
			long cS = calcContigStart(c, start, end);
			long cE = calcContigEnd(c, start, end);

			if (cS != -1 && cE != -1)
			{
				args.add("-r");
				args.add(c.getName() + ":" + cS + "-" + cE);
			}
		}


		// Summarise...
		System.out.println("This is task " + (taskID+1) + " of " + tasks);

		// Safety check (eg 500 tasks and just 100 bases)
		if (args.size() == 0)
		{
			System.out.println("Nothing for this task to do - quitting");
			System.exit(0);
		}

		runFreeBayes(args);
	}

	private void runFreeBayes(ArrayList<String> args)
		throws Exception
	{
		// Build the command to use for running FreeBayes
		ArrayList<String> commands = new ArrayList<String>();

		commands.add(CLIParserFB.freebayesPath);

		// Provide the BAM files, either one at a time...
		if (CLIParserFB.bamFiles != null)
		{
			for (String bamFile: CLIParserFB.bamFiles)
			{
				commands.add("-b");
				commands.add(bamFile);
			}
		}
		// ...or as part of a list
		if (CLIParserFB.bamFileList != null)
		{
			commands.add("-L");
			commands.add(CLIParserFB.bamFileList);
		}

		// Additional FreeBayes parameters
		for (String cmd: CLIParserFB.fbOptions)
			commands.add(cmd);

		// And finally the list of -r arguments
		commands.addAll(args);


		System.out.println("Running FreeBayes using command:");
		for (String cmd: commands)
			System.out.print(cmd + " ");


		// Run it!
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process proc = pb.start();

		new StreamCatcher.Error(proc.getErrorStream()).start();
		new StreamCatcher.Error(proc.getInputStream()).start();

		if (proc.waitFor() != 0)
			System.exit(1);
	}

	// This breaks the job into as even a number of parts-per-job as possible,
	// eg, for a total length of 13 and 4 tasks we would use 4, 3, 3, 3 as the
	// bases-per-task.
	static long[] calcStartEnd(long length, int tasks, int taskID)
	{
		// [0] = start, [1] = end
		long[] values = new long[2];

		long mod = length % tasks;
		long div = length / tasks;

		if (taskID < mod)
		{
			long startAt = taskID * (div+1);

			values[0] = startAt;
			values[1] = startAt + div;
		}
		else
		{
			long startAt = (mod*(div+1)) + ((taskID - mod) * div);

			values[0] = startAt;
			values[1] = startAt + div - 1;
		}

		return values;
	}

	static long calcContigStart(Contig c, long start, long end)
	{
		// Does this task's START occur in this contig?
		if (start >= c.getStart() && start <= c.getEnd())
			return start-c.getStart();
		// Or, was the START before this contig
		else if (start < c.getStart())
			return 0;
		else
			return -1;
	}

	static long calcContigEnd(Contig c, long start, long end)
	{
		// Does this task's END occur in this contig?
		if (end <= c.getEnd() && end >= c.getStart())
			return c.getLength()-1 - (c.getEnd()-end);
		else if (end > c.getEnd())
			return c.getLength()-1;
		else
			return -1;
	}
}