package de.fu_berlin.inf.atl.storer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.logging.Logger;

public class FileStorer implements Storer {

	Logger fLogger = Logger.getLogger("ATL");

	String fFilename;

	PrintWriter out;

	boolean enabled;

	StringBuffer fDataToFlush = new StringBuffer();

	void open() {
		try {
			out = new PrintWriter(new FileWriter(fFilename,
			/* append */true), /* autoflush */true);
		} catch (IOException e) {
			out = null; // In case out was != null when entering
			fLogger.severe("Could not open output-file");
		}
	}

	/**
	 * Will replace $t in the filename with the current time-stamp and open the
	 * output stream
	 */
	public FileStorer(String filename) {

		Calendar now = Calendar.getInstance();
		String nowString = String.format("%1$tY-%1$tm-%1$td-%1$tH.%1$tM.%1$tS",
				now);
		fFilename = filename.replaceAll("\\$t", nowString);

		open();

		enabled = true;
	}

	public void log(String s) {
		if (!enabled)
			return;

		// Write to buffer in case something goes wrong
		fDataToFlush.append(s);

		flush();
	}

	void flush() {
		if (out == null)
			open();

		if (out == null)
			return;

		out.println(fDataToFlush.toString());
		if (out.checkError()) {
			fLogger.severe("Writing to file " + fFilename
					+ " failed. Storing to memory");
			out.close();
			out = null;
		} else {
			// Successfully written!
			// Start new buffer
			fDataToFlush = new StringBuffer();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean disabled) {
		this.enabled = disabled;
	}

	public void shutdown() {
		flush();
		if (fDataToFlush.length() > 0){
			fLogger.severe("Could not write all data before shuting down. Missing the following lines:\n" + fDataToFlush.toString());
		}
		if (out != null)
			out.close();
	}
}
