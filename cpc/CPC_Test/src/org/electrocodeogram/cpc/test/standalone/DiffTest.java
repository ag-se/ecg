package org.electrocodeogram.cpc.test.standalone;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.reconciler.provider.DefaultDiffProvider;
import org.electrocodeogram.cpc.reconciler.utils.diff.LineDiffResult;
import org.electrocodeogram.cpc.test.utils.StandaloneTestUtils;


/**
 * A small test class for execution of a standalone diff.<br/>
 * This class is not used by CPC. 
 * 
 * @author vw
 */
public class DiffTest
{
	private static Log log;

	/**
	 * Takes two file names as parameters, reads their contents and generates a character based diff.<br/>
	 * The result as well as the time taken is printed to STDOUT.
	 */
	public static void main(String[] args) throws IOException
	{
		StandaloneTestUtils.initEclipseEnvironment();
		log = LogFactory.getLog(DiffTest.class);

		//we expect two filenames as parameters
		if (args == null || args.length != 2)
		{
			log.error("main() - usage: DiffTest <oldFile> <newFile>");
			System.exit(1);
		}

		File oldFile = new File(args[0]);
		File newFile = new File(args[1]);

		if (!oldFile.canRead() || !newFile.canRead())
		{
			log.error("main() - unable to read one of the files: " + oldFile + ", " + newFile);
			System.exit(1);
		}

		String oldText = readFileContent(oldFile);
		String newText = readFileContent(newFile);

		long start = System.currentTimeMillis();

		testCharDiff(oldText, newText);
		//testLineDiff(oldText, newText);

		long end = System.currentTimeMillis();

		log.info("main() - time taken: " + (end - start) + " ms");
	}

	@SuppressWarnings("unused")
	private static void testCharDiff(String oldText, String newText)
	{
		IDiffProvider diffProvider = (IDiffProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IDiffProvider.class);
		assert (diffProvider != null);
		List<IDiffResult> diffs = diffProvider.charDiff(oldText, newText);

		for (IDiffResult diff : diffs)
		{
			log.info(diff);

			if (diff.isInsert())
				log.info("ADD LOCATION: " + oldText.substring(diff.getOffset() - 10, diff.getOffset()) + "[^here^]"
						+ oldText.substring(diff.getOffset(), diff.getOffset() + 10));

			else if (diff.isDelete())
			{
				String deletedStr = oldText.substring(diff.getOffset(), diff.getOffset() + diff.getLength());
				log.info("DEL: " + deletedStr);
				if (!diff.getText().equals(deletedStr))
				{
					log.error("diff content doesn't match offset content - offset: " + diff.getOffset());
					log.error("  in diff  : \"" + diff.getText() + "\"");
					log.error("  in source: \"" + deletedStr + "\"");
					System.exit(1);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static void testLineDiff(String oldText, String newText)
	{
		List<String> oldTextList = DefaultDiffProvider.splitIntoLines(oldText);
		List<String> newTextList = DefaultDiffProvider.splitIntoLines(newText);

		List<LineDiffResult> diffs = DefaultDiffProvider.lineDiff(oldTextList, newTextList);

		for (LineDiffResult diff : diffs)
		{
			log.info(diff);

			if (diff.getDelLength() > 0)
			{
				for (int i = diff.getDelOffset(); i < diff.getDelOffset() + diff.getDelLength(); ++i)
					log.trace("DEL: " + i + " - " + oldTextList.get(i));
			}

			if (diff.getAddLength() > 0)
			{
				for (int i = diff.getAddOffset(); i < diff.getAddOffset() + diff.getAddLength(); ++i)
					log.trace("ADD: " + i + " - " + newTextList.get(i));
			}
		}
	}

	/**
	 * Reads and returns the contents of the given file.
	 */
	private static String readFileContent(File file) throws IOException
	{
		int len = (int) file.length();
		byte[] buf = new byte[len];

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		int read = bis.read(buf);
		bis.close();

		if (read != len)
		{
			log.error("readFileContent() -short read while reading file - expected: " + len + ", read: " + read
					+ ", file: " + file);
			System.exit(1);
		}

		return new String(buf);
	}

}
