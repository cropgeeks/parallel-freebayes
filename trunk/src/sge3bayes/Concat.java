package sge3bayes;

import java.io.*;
import java.util.*;

public class Concat
{
	public static void main(String[] args)
		throws Exception
	{
		CLIParserCC.parse(args);

		new Concat().run();
	}

	public Concat()
	{
	}

	public void run()
		throws Exception
	{
		boolean isFirstFile = true;

		// The output file being written to
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(CLIParserCC.concatFile)));

		File[] files = new File(CLIParserCC.concatDir).listFiles();

		Arrays.sort(files, new AlphanumComparator());

		for (File file: files)
		{
			if (file.isDirectory())
				continue;

			System.out.println(file);

			BufferedReader in = new BufferedReader(new FileReader(file));

			String str = null;
			while ((str = in.readLine()) != null)
			{
				if (str.startsWith("##"))
					continue;

				if (str.startsWith("#") && isFirstFile == false)
					continue;

				out.println(str);
			}

			in.close();

			isFirstFile = false;
		}

		out.close();
	}
}