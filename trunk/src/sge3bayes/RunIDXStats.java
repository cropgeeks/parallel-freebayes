package sge3bayes;

import java.io.*;
import java.util.*;

public class RunIDXStats
{
	private ArrayList<String> contigs = new ArrayList<String>();

	ArrayList<String> collect()
		throws Exception
	{
		ProcessBuilder pb = new ProcessBuilder(CLIParserFB.samtoolsPath, "idxstats", CLIParserFB.bamFiles[0]);

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

	private class IdxStatsCatcher extends StreamCatcher
	{
		IdxStatsCatcher(InputStream in) { super(in); }

		protected void processLine(String line)
		{
			String[] tokens = line.split("\t");

			if (tokens.length == 4)
			{
				String contigName = tokens[0];
				int readCount = Integer.parseInt(tokens[2]);

				if (readCount > 0)
					contigs.add(contigName);
			}
		}
	}
}