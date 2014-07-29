package sge3bayes;

import java.io.*;

public abstract class StreamCatcher extends Thread
{
	private BufferedReader reader = null;

	public StreamCatcher(InputStream in)
	{
		reader = new BufferedReader(new InputStreamReader(in));
	}

	public void run()
	{
		try
		{
			String line = null;

			while ((line = reader.readLine()) != null)
				processLine(line);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}

		try { reader.close(); }
		catch (IOException e) {}
	}

	protected abstract void processLine(String line);

	// Concrete implementation of StreamCatcher to handle error streams, doing
	// nothing more than printing any errors back to the console
	public static class Error extends StreamCatcher
	{
		Error(InputStream in) { super(in); }

		protected void processLine(String line)
		{
			System.err.println(line);
		}
	}
}