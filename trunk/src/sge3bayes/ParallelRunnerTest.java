package sge3bayes;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

public class ParallelRunnerTest
{
	@Test
	public void calcStartEnd()
	{
		long[] values = ParallelRunner.calcStartEnd(100, 10, 0);
		Assert.assertEquals(0, values[0]);
		Assert.assertEquals(9, values[1]);

		values = ParallelRunner.calcStartEnd(100, 10, 9);
		Assert.assertEquals(90, values[0]);
		Assert.assertEquals(99, values[1]);

		values = ParallelRunner.calcStartEnd(100, 3, 1);
		Assert.assertEquals(34, values[0]);
		Assert.assertEquals(66, values[1]);
	}

	@Test
	public void calcContigStart()
	{
		Contig c = new Contig("", 0, 100);
		Assert.assertEquals(0, ParallelRunner.calcContigStart(c, 0, 99));
		Assert.assertEquals(10, ParallelRunner.calcContigStart(c, 10, 99));
		Assert.assertEquals(-1, ParallelRunner.calcContigStart(c, 100, 99));

		c = new Contig("", 100, 100);
		Assert.assertEquals(0, ParallelRunner.calcContigStart(c, 0, 99));
		Assert.assertEquals(0, ParallelRunner.calcContigStart(c, 50, 99));
		Assert.assertEquals(1, ParallelRunner.calcContigStart(c, 101, 99));
		Assert.assertEquals(-1, ParallelRunner.calcContigStart(c, 250, 99));
	}

	@Test
	public void calcContigEnd()
	{
		Contig c = new Contig("", 0, 100);
		Assert.assertEquals(99, ParallelRunner.calcContigEnd(c, 0, 99));
		Assert.assertEquals(99, ParallelRunner.calcContigEnd(c, 0, 101));

		c = new Contig("", 100, 100);
		Assert.assertEquals(5, ParallelRunner.calcContigEnd(c, 0, 105));
		Assert.assertEquals(-1, ParallelRunner.calcContigEnd(c, 0, 50));
	}
}