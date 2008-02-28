package org.electrocodeogram.cpc.test.tests;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.imports.control.ImportToolAdapterResult;
import org.electrocodeogram.cpc.imports.control.ImportToolAdapterTask;
import org.electrocodeogram.cpc.imports.sdd.imports.SDDImportToolAdapter;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SDDImportTest
{
	private static Log log = LogFactory.getLog(SDDImportTest.class);

	private static final String PROJECT_NAME = "SDDImportTest";

	/**
	 * Perform pre-test initialization.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", true, true);
	}

	/**
	 * Perform post-test cleanup.
	 */
	@After
	public void tearDown() throws Exception
	{
		TestUtils.defaultTearDown(PROJECT_NAME);
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSDDImport() throws Exception
	{
		SDDImportToolAdapter sddImportToolAdapter = new SDDImportToolAdapter();

		//get a list of all java files in our project
		List<IFile> files = CoreFileUtils.getSupportedFilesInProject(PROJECT_NAME);

		//initialise the options
		Map<String, String> options = new HashMap<String, String>();

		//create the task
		ImportToolAdapterTask importTask = new ImportToolAdapterTask();
		importTask.setFiles(files);
		importTask.setOptions(options);

		ImportToolAdapterResult importResult = new ImportToolAdapterResult();

		//now run the import
		sddImportToolAdapter.processImport(null, importTask, importResult);

		//display result
		for (ICloneFile cloneFile : importResult.getCloneMap().keySet())
		{
			log.info("FILE: " + cloneFile);
			log.info("  CLONES: " + importResult.getCloneMap().get(cloneFile));
		}

		log.info("GROUPS: " + importResult.getCloneGroups());
	}
}
