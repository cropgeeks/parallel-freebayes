package sge3bayes;

import java.io.*;
import java.util.*;

public class RunIDXStats
{
	private ArrayList<Contig> contigs = new ArrayList<>();

	ArrayList<Contig> collect()
		throws Exception
	{
		ProcessBuilder pb = new ProcessBuilder(CLIParserFB.samtoolsPath, "idxstats", getBamFile());

		Process proc = pb.start();

		Thread eStream = new StreamCatcher.Error(proc.getErrorStream());
		eStream.start();
		Thread oStream = new IdxStatsCatcher(proc.getInputStream());
		oStream.start();

		if (proc.waitFor() != 0)
			System.exit(1);

		while (oStream.isAlive() || eStream.isAlive())
			Thread.sleep(10);

		return contigs;
	}

	private String getBamFile()
		throws Exception
	{
		// If -b option was used, just take the first file
		if (CLIParserFB.bamFiles != null)
			return CLIParserFB.bamFiles[0];

		// If -L option was used, read the first line of the file and use that
		BufferedReader in = new BufferedReader(new FileReader(new File(CLIParserFB.bamFileList)));
		return in.readLine();
	}

	// Calculates and returns the total genome size across all the contigs
	long calculateLength()
	{
		long length = 0;
		for (Contig contig: contigs)
			length += contig.getLength();

		return length;
	}

	private class IdxStatsCatcher extends StreamCatcher
	{
		IdxStatsCatcher(InputStream in) { super(in); }

		long bases = 0;

		protected void processLine(String line)
		{
			String[] tokens = line.split("\t");

			if (tokens.length == 4)
			{
				// Each line contains a ref: name, length, mapped reads, unmapped reads
				String name = tokens[0];
				long length = Long.parseLong(tokens[1]);
				long readCount = Long.parseLong(tokens[2]);

				if (readCount > 0)
				{
					contigs.add(new Contig(name, bases, length));
					bases += length;
				}
			}
		}
	}
}