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
		// Collect the list of contigs
		ArrayList<String> allContigs = new RunIDXStats().collect();
		ArrayList<String> contigs = new ArrayList<>();

		// Work out how many contigs-per-task will be needed
		int count = allContigs.size();
		int tasks = CLIParserFB.sgeTasks;
		int taskID = CLIParserFB.sgeTaskID - 1;


		// This breaks the contigs array (by the number of tasks) into as even
		// a number of parts-per-job as possible, eg, for 13 contigs and 4 tasks
		// we would use 4, 3, 3, 3 as the contigs-per-task

		int mod = count % tasks;
		int div = count / tasks;

		if (taskID < mod)
		{
			int startAt = taskID * (div+1);
			for (int i = startAt; i < startAt+(div+1); i++)
			{
				contigs.add("-r");
				contigs.add(allContigs.get(i));
			}
		}
		else
		{
			int startAt = (mod*(div+1)) + ((taskID - mod) * div);
			for (int i = startAt; i < startAt+div; i++)
			{
				contigs.add("-r");
				contigs.add(allContigs.get(i));
			}
		}

		// Summarise...
		System.out.println("This is task " + (taskID+1) + " of " + tasks);
		System.out.println("Contigs with reads: " + allContigs.size());
		System.out.println("Contigs to be processed: " + (contigs.size()/2));

		if (contigs.size() == 0)
		{
			System.out.println("There are no contigs to be processed by this task - quitting");
			System.exit(0);
		}


		// Build the command to use for running FreeBayes
		ArrayList<String> commands = new ArrayList<String>();

		commands.add(CLIParserFB.freebayesPath);

		if (CLIParserFB.bamFiles != null)
		{
			for (String bamFile: CLIParserFB.bamFiles)
			{
				commands.add("-b");
				commands.add(bamFile);
			}
		}
		if (CLIParserFB.bamFileList != null)
		{
			commands.add("-L");
			commands.add(CLIParserFB.bamFileList);
		}


		for (String cmd: CLIParserFB.fbOptions)
			commands.add(cmd);

		commands.addAll(contigs);

		System.out.println("Running FreeBayes using command:");
		for (String cmd: commands)
			System.out.print(cmd + " ");
		System.out.println();



		ProcessBuilder pb = new ProcessBuilder(commands);
		Process proc = pb.start();

		new StreamCatcher.Error(proc.getErrorStream()).start();
		new StreamCatcher.Error(proc.getInputStream()).start();

		if (proc.waitFor() != 0)
			System.exit(1);
	}
}