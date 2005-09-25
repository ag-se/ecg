package org.electrocodeogram.test.msdt;

import java.io.IOException;

import junit.framework.TestCase;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.validation.EventValidator;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.EventGenerator.MicroSensorDataType;

/**
 * This class collects testcases for validating the event validator mechanism on the ECG serverside.
 */
public class MsdtTests extends TestCase
{

    private MockMsdtRegistry _mockMsdtRegistry = null;

    private EventValidator _eventValidator = null;

    private EventGenerator _eventGenerator = null;

    /**
     * @throws IOException 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws  IOException
    {

        this._eventGenerator = new EventGenerator();
        
        this._mockMsdtRegistry = new MockMsdtRegistry();
       
        this._eventValidator = new EventValidator(this._mockMsdtRegistry);
        
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown()
    {
        this._eventValidator = null;

        this._eventGenerator = null;

        this._mockMsdtRegistry = null;
    }

    /**
     * Testcase SE13 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "added" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidResourceAddedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEADDED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE14 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "removed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidResourceRemovedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEREMOVED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE15 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "changed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidResourceChangedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCECHANGED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE16 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidResourceMicroActivtyIsNotAcceptedForUnknownActivity()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEACTIVITYUNKNOWN);

        assertNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE17 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Codechange" MSDT EventPacket
     * to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidCodechangeMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.CODECHANGE);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE18 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "closed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorClosedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORCLOSED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE19 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "activated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorActivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORACTIVATED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE20 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorDeactivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORDEACTIVATED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE21 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "opened" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorOpenedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITOROPENED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE22 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidEditorMicroActivtyForUnknownActivity()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORACTIVITYUNKNOWN);

        assertNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE23 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "closed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartClosedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTCLOSED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE24 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "activated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartActivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTACTIVATED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE25 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartDeactivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTDEACTIVATED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE26 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "opened" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartOpenedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTOPENED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE27 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidPartMicroActivtyForUnknownActivity()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.PARTACTIVITYUNKNOWN);

        assertNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE28 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
     * with the "debug" attribute of the "run" element set to "false" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidRunDebugMicroActivtyIsAcceptedWithoutDebug()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGNODEBUG);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE29 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
     * with the "debug" attribute of the "run" element set to "true" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidRunDebugMicroActivtyIsAcceptedWithDebug()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHDEBUG);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE30 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
     * with the "debug" attribute of the "run" element set to "illegalTestValue" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidRunDebugMicroActivtyIsAcceptedWithIllegalDebug()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHILLEGALDEBUG);

        assertNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE31 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "closed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidWindowClosedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWCLOSED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE32 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "activated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidWindowActivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWACTIVATED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE33 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValiWindowDeactivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWDEACTIVATED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE34 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "opened" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidWindowOpenedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWOPENED);

        assertNotNull(this._eventValidator.validate(packet));

    }

    /**
     * Testcase SE35 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestValue" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidWindowMicroActivtyForUnknownActivity()
    {
        ValidEventPacket packet = this._eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWACTIVITYUNKNOWN);

        assertNull(this._eventValidator.validate(packet));

    }

}
