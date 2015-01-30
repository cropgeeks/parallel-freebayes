package sge3bayes;

import org.apache.commons.cli.*;

class CLIParserFB
{
	private static Options options = new Options();

	// Options for running in ParallelFreeBayes mode
	static String samtoolsPath = "samtools";
	static String freebayesPath = "freebayes";
	static String[] bamFiles = null;
	static String bamFileList = null;

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


		// Ideally -b and -L could both be provided, but commons-cli doesn't
		// seem to support that. What is important, is that one OR the other
		// MUST be provided, and that does work
		OptionGroup fileOptions = new OptionGroup();
		fileOptions.setRequired(true);

		fileOptions.addOption(OptionBuilder.withLongOpt("bam")
			.withDescription("add FILE to the set of BAM files to be analyzed")
			.hasArg()
			.withArgName("FILE")
//			.isRequired()
			.create("b"));

		fileOptions.addOption(OptionBuilder.withLongOpt("bam-list")
			.withDescription("a file containing a list of BAM files to be analyzed")
			.hasArg()
			.withArgName("FILE")
//			.isRequired()
			.create("L"));

		options.addOptionGroup(fileOptions);


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

			// Path to a list of BAM files
			if (cmd.hasOption("L"))
				bamFileList = cmd.getOptionValue("L");
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