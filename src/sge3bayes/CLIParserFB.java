package sge3bayes;

import org.apache.commons.cli.*;

class CLIParserFB
{
	private static Options options = new Options();

	// Options for running in ParallelFreeBayes mode
	static String samtoolsPath = "samtools";
	static String freebayesPath = "freebayes";
	static String[] bamFiles = null;
	static int sgeTaskID = 1;
	static int sgeTasks = 1;

	// "Left over" options that get passed to FreeBayes itself
	static String[] fbOptions;

	static void parse(String args[])
	{
		options.addOption("h", "help", false, "prints this help text");

		options.addOption(OptionBuilder.withLongOpt("samtools")
			.withDescription("path to samtools")
			.hasArg()
			.withArgName("PATH")
			.create());

		options.addOption(OptionBuilder.withLongOpt("freebayes")
			.withDescription("path to freebayes")
			.hasArg()
			.withArgName("PATH")
			.create());

		options.addOption(OptionBuilder.withLongOpt("bam-file")
			.withDescription("BAM file being processed")
			.hasArg()
			.withArgName("FILE")
			.isRequired()
			.create("b"));


		CommandLineParser parser = new BasicParser();

		try
		{
			CommandLine cmd = parser.parse(options, args, true);

			fbOptions = cmd.getArgs();

			// The path to samtools
			if (cmd.hasOption("samtools"))
				samtoolsPath = cmd.getOptionValue("samtools");

			// The path to freebayes
			if (cmd.hasOption("freebayes"))
				freebayesPath = cmd.getOptionValue("freebayes");

			// BAM file
			if (cmd.hasOption("b"))
				bamFiles = cmd.getOptionValues("b");
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
		formatter.printHelp("ParallelFreeBayes [OPTIONS] [FREEBAYES OPTIONS]", options);
	}

	// Parses the environment variables to determine how many parts there are to
	// this job, and which part this particular instance is
	static void parseEnvironment()
	{
		try
		{
			sgeTaskID = Integer.parseInt(System.getenv("SGE_TASK_ID"));
			sgeTasks  = Integer.parseInt(System.getenv("SGE_TASK_LAST"));
		}
		catch (Exception e)
		{
			System.out.println("Unable to determine SGE environment variables");
		}

		System.out.println("Task ID: " + sgeTaskID + " of " + sgeTasks);
	}
}