package sge3bayes;

import org.apache.commons.cli.*;

class CLIParserCC
{
	private static Options options = new Options();

	static String concatDir;
	static String concatFile;

	static void parse(String args[])
	{
		options.addOption("h", "help", false, "prints this help text");

		options.addOption(OptionBuilder.withLongOpt("dir")
			.withDescription("path to the directory containing vcf files")
			.hasArg()
			.withArgName("PATH")
			.isRequired()
			.create("d"));

		options.addOption(OptionBuilder.withLongOpt("output")
			.withDescription("file to create containing the concatenated results")
			.hasArg()
			.withArgName("FILE")
			.isRequired()
			.create("o"));


		CommandLineParser parser = new BasicParser();

		try
		{
			CommandLine cmd = parser.parse(options, args, false);

			concatDir = cmd.getOptionValue("dir");
			concatFile = cmd.getOptionValue("output");
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			System.out.println();

			help();

			System.exit(1);
		}
	}

	private static void help()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -cp pfb.jar sge3bayes.Concat [OPTIONS]", options);
	}
}