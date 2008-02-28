package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorCloneFile;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class XMLMappingTest
{
	private static Log log = LogFactory.getLog(XMLMappingTest.class);

	private static final String PROJECT_NAME = "XMLMappingTest";

	/**
	 * Perform pre-test initialisation.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", false, false);
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
	 * Add and remove a couple of clones. They are not persisted, all actions are done in cache.
	 */
	@Test
	public void testXMLMappingProvider() throws StoreLockingException
	{
		log.debug("testXMLMappingProvider()");

		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);
		assertTrue("unable to get ICloneFactoryProvider", cloneFactoryProvider != null);

		IMappingProvider mappingProvider = (IMappingProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IMappingProvider.class);
		assertTrue("unable to get IXMLMappingProvider", mappingProvider != null);

		/*
		 * Create some clones.
		 */

		log.trace("testXMLMappingProvider() - creating clones.");

		List<IClone> clones = new LinkedList<IClone>();

		ICloneFile file = (ICloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		((ICreatorCloneFile) file).setProject(PROJECT_NAME);
		((ICreatorCloneFile) file).setPath("/some/file");
		((ICreatorCloneFile) file).setModificationDate(System.currentTimeMillis());
		((ICreatorCloneFile) file).setSize(1234);

		ICloneGroup group1 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);

		IClone clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 10, 20, null);
		ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) cloneFactoryProvider
				.getInstance(ICloneModificationHistoryExtension.class);
		Date creationDate = new Date();
		history.addCloneDiff(new CloneDiff("creator", creationDate, false, 10, 5, "abc"));
		history.addCloneDiff(new CloneDiff("creator", new Date(creationDate.getTime() + 1), false, 20, 5, "def"));
		history.addCloneDiff(new CloneDiff("creator", new Date(creationDate.getTime() + 2), false, 30, 5, ""));
		clone1.addExtension(history);
		clones.add(clone1);

		ICloneNonWsPositionExtension nonWsPos = (ICloneNonWsPositionExtension) cloneFactoryProvider
				.getInstance(ICloneNonWsPositionExtension.class);
		nonWsPos.setStartNonWsOffset(5);
		nonWsPos.setEndNonWsOffset(13);
		clone1.addExtension(nonWsPos);

		clones.add(TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 30, 40, null));
		clones.add(TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 50, 60, null));

		ICloneGroup group2 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);

		clones.add(TestCloneUtils.createTestClone(cloneFactoryProvider, file, group2, 10, 20, null));
		clones.add(TestCloneUtils.createTestClone(cloneFactoryProvider, file, group2, 30, 40, null));

		/*
		 * Now map to XML.
		 */
		log.trace("testXMLMappingProvider() - mapping clone data to xml.");

		MappingStore xmlStore = new MappingStore(file, clones);
		String xml = null;
		try
		{
			xml = mappingProvider.mapToString(xmlStore, true);
		}
		catch (MappingException e)
		{
			assertTrue("exception during xml mapping - object to xml - " + e, false);
		}

		if (log.isTraceEnabled())
			log.trace("testXMLMappingProvider() - mapping result - o2xml: " + xml);

		/*
		 * Map back from XML.
		 */

		MappingStore xmlResult = null;
		try
		{
			xmlResult = mappingProvider.mapFromString(xml);
		}
		catch (MappingException e)
		{
			assertTrue("exception during xml mapping - xml to object - " + e, false);
		}

		if (log.isTraceEnabled())
			log.trace("testXMLMappingProvider() - mapping result - xml2o: " + xmlResult);

		/*
		 * Now ensure that no data was lost/modified/added.
		 */

		//check clone file
		assertTrue("clone file does not match", CoreClonePositionUtils.statefulObjectsEqual(xmlStore
				.getStatefulParentObject(), xmlResult.getStatefulParentObject(), true));

		//check all clones
		assertTrue("clone count missmatch", xmlStore.getStatefulChildObjects().size() == xmlResult
				.getStatefulChildObjects().size());

		for (int i = 0; i < xmlStore.getStatefulChildObjects().size(); ++i)
		{
			IStatefulObject oldClone = xmlStore.getStatefulChildObjects().get(i);
			IStatefulObject newClone = xmlResult.getStatefulChildObjects().get(i);

			assertTrue("wrong order of clone elements - old: " + oldClone + ", new: " + newClone, oldClone
					.equals(newClone));

			assertTrue("clone data missmatch - old: " + oldClone + ", new: " + newClone, CoreClonePositionUtils
					.statefulObjectsEqual(oldClone, newClone, true));
		}

		log.debug("testXMLMappingProvider() - done.");
	}
}
