package sge3bayes;

import java.io.*;
import java.util.*;

public class RunIDXStats
{
	private ArrayList<String> contigs = new ArrayList<String>();

	ArrayList<String> collect()
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