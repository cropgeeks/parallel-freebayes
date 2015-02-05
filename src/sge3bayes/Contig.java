package sge3bayes;

class Contig
{
	private String name;
	// This is the contig's "start" within the overall (total length of all
	// contigs) genome space. The contig's actual start is obviously always "1"
	private long start;
	private long length;

	Contig(String name, long start, long length)
	{
		this.name = name;
		this.start = start;
		this.length = length;
	}

	String getName()
		{ return name; }

	long getStart()
		{ return start; }

	long getEnd()
		{ return start+length-1; }

	long getLength()
		{ return length; }
}