package org.electrocodeogram.test.msdt;

import java.io.IOException;

import junit.framework.TestCase;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.event.ValidEventPacket.VALIDITY_LEVEL;
import org.electrocodeogram.system.ISystemRoot;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.EventGenerator.MicroSensorDataType;
import org.electrocodeogram.test.module.TestModule;
import org.electrocodeogram.test.server.mockClient.MockClient;

/**
 * This class collects testcases for validating the event validator mechanism on the ECG serverside.
 */
public class MsdtTests extends TestCase
{

	//private MockMsdtRegistry _mockMsdtRegistry = null;

	private EventGenerator _eventGenerator = null;

	private MockClient _mockClient = null;

	private ISystemRoot _root;

	private TestModule _testModule;

	/**
	 * @throws IOException 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws IOException
	{

		if (this._root == null)
		{
			_root = SystemRoot.getSystemInstance();
		}

		if (this._testModule == null)
		{
			this._testModule = new TestModule();

			this._testModule.registerMSDTs();
		}

		this._eventGenerator = new EventGenerator();

		//this._mockMsdtRegistry = new MockMsdtRegistry();

		this._mockClient = new MockClient();
		
		this._mockClient.setValidityLevel(VALIDITY_LEVEL.ECG);

	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown()
	{
		this._mockClient = null;

		this._eventGenerator = null;

		//this._mockMsdtRegistry = null;
	}

	/**
	 * Testcase SE13 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
	 * with the "activity" element containing the value "added" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidResourceAddedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEADDED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE14 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
	 * with the "activity" element containing the value "removed" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidResourceRemovedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEREMOVED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE15 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
	 * with the "activity" element containing the value "changed" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidResourceChangedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCECHANGED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE16 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
	 * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "false".
	 */
	public void testInvalidResourceMicroActivtyIsNotAcceptedForUnknownActivity()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEACTIVITYUNKNOWN);

		try
		{
			this._mockClient.passEventData(packet);

			assertTrue(false);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(true);
		}

	}

	/**
	 * Testcase SE17 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Codechange" MSDT EventPacket
	 * to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidCodechangeMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.CODECHANGE);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE18 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
	 * with the "activity" element containing the value "closed" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidEditorClosedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORCLOSED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE19 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
	 * with the "activity" element containing the value "activated" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidEditorActivatedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORACTIVATED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE20 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
	 * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidEditorDeactivatedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORDEACTIVATED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE21 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
	 * with the "activity" element containing the value "opened" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidEditorOpenedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITOROPENED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE22 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
	 * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "false".
	 */
	public void testInvalidEditorMicroActivtyForUnknownActivity()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORACTIVITYUNKNOWN);

		try
		{
			this._mockClient.passEventData(packet);

			assertTrue(false);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(true);
		}

	}

	/**
	 * Testcase SE23 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
	 * with the "activity" element containing the value "closed" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidPartClosedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTCLOSED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE24 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
	 * with the "activity" element containing the value "activated" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidPartActivatedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTACTIVATED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE25 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
	 * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidPartDeactivatedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTDEACTIVATED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE26 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
	 * with the "activity" element containing the value "opened" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidPartOpenedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTOPENED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE27 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
	 * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "false".
	 */
	public void testInvalidPartMicroActivtyForUnknownActivity()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTACTIVITYUNKNOWN);

		try
		{
			this._mockClient.passEventData(packet);

			assertTrue(false);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(true);
		}

	}

	/**
	 * Testcase SE28 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
	 * with the "debug" attribute of the "run" element set to "false" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidRunDebugMicroActivtyIsAcceptedWithoutDebug()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGNODEBUG);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE29 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
	 * with the "debug" attribute of the "run" element set to "true" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidRunDebugMicroActivtyIsAcceptedWithDebug()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHDEBUG);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE30 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
	 * with the "debug" attribute of the "run" element set to "illegalTestValue" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "false".
	 */
	public void testInvalidRunDebugMicroActivtyIsAcceptedWithIllegalDebug()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHILLEGALDEBUG);

		try
		{
			this._mockClient.passEventData(packet);

			assertTrue(false);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(true);
		}

	}

	/**
	 * Testcase SE31 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
	 * with the "activity" element containing the value "closed" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidWindowClosedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWCLOSED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE32 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
	 * with the "activity" element containing the value "activated" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidWindowActivatedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWACTIVATED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE33 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
	 * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValiWindowDeactivatedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWDEACTIVATED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE34 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
	 * with the "activity" element containing the value "opened" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "true".
	 */
	public void testValidWindowOpenedMicroActivtyIsAccepted()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWOPENED);

		try
		{
			this._mockClient.passEventData(packet);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(false);
		}

		assertTrue(true);

	}

	/**
	 * Testcase SE35 according to the document TESTPLAN Version 2.0 or higher.
	 * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
	 * with the "activity" element containing the value "unknownTestValue" to the ECG server's event validator.
	 * The test is successfull if the result from the event validator is "false".
	 */
	public void testInvalidWindowMicroActivtyForUnknownActivity()
	{
		WellFormedEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWACTIVITYUNKNOWN);

		try
		{
			this._mockClient.passEventData(packet);

			assertTrue(false);
		}
		catch (IllegalEventParameterException e)
		{
			assertTrue(true);
		}

	}

}
