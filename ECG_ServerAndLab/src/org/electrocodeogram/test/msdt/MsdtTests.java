package org.electrocodeogram.test.msdt;

import java.io.IOException;

import junit.framework.TestCase;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.EventValidator;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.EventGenerator.MicroSensorDataType;

/**
 * This class collects testcases for validating the event validator mechanism on the ECG serverside.
 */
public class MsdtTests extends TestCase
{

    private MockMsdtRegistry mockMsdtRegistry = null;

    private EventValidator eventValidator = null;

    private EventGenerator eventGenerator = null;

    /**
     * @throws IOException 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws  IOException
    {

        this.eventGenerator = new EventGenerator();
        
        this.mockMsdtRegistry = new MockMsdtRegistry();
       
        this.eventValidator = new EventValidator(this.mockMsdtRegistry);
        
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown()
    {
        this.eventValidator = null;

        this.eventGenerator = null;

        this.mockMsdtRegistry = null;
    }

    /**
     * Testcase SE13 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "added" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidResourceAddedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEADDED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE14 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "removed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidResourceRemovedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEREMOVED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE15 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "changed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidResourceChangedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCECHANGED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE16 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Resource" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidResourceMicroActivtyIsNotAcceptedForUnknownActivity()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.RESOURCEACTIVITYUNKNOWN);

        assertNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE17 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Codechange" MSDT EventPacket
     * to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidCodechangeMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.CODECHANGE);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE18 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "closed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorClosedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORCLOSED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE19 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "activated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorActivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORACTIVATED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE20 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorDeactivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORDEACTIVATED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE21 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "opened" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidEditorOpenedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.EDITOROPENED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE22 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Editor" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidEditorMicroActivtyForUnknownActivity()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.EDITORACTIVITYUNKNOWN);

        assertNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE23 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "closed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartClosedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.PARTCLOSED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE24 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "activated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartActivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.PARTACTIVATED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE25 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartDeactivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.PARTDEACTIVATED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE26 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "opened" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidPartOpenedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.PARTOPENED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE27 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Part" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestActivity" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidPartMicroActivtyForUnknownActivity()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.PARTACTIVITYUNKNOWN);

        assertNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE28 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
     * with the "debug" attribute of the "run" element set to "false" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidRunDebugMicroActivtyIsAcceptedWithoutDebug()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGNODEBUG);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE29 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
     * with the "debug" attribute of the "run" element set to "true" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidRunDebugMicroActivtyIsAcceptedWithDebug()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHDEBUG);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE30 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "RunDebug" MSDT EventPacket
     * with the "debug" attribute of the "run" element set to "illegalTestValue" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidRunDebugMicroActivtyIsAcceptedWithIllegalDebug()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHILLEGALDEBUG);

        assertNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE31 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "closed" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidWindowClosedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWCLOSED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE32 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "activated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidWindowActivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWACTIVATED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE33 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "deactivated" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValiWindowDeactivatedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWDEACTIVATED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE34 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "opened" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "true".
     */
    public void testValidWindowOpenedMicroActivtyIsAccepted()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWOPENED);

        assertNotNull(this.eventValidator.validate(packet));

    }

    /**
     * Testcase SE35 according to the document TESTPLAN Version 2.0 or higher.
     * This testcase passes a single syntactically valid "Activity" SDT and "Window" MSDT EventPacket
     * with the "activity" element containing the value "unknownTestValue" to the ECG server's event validator.
     * The test is successfull if the result from the event validator is "false".
     */
    public void testInvalidWindowMicroActivtyForUnknownActivity()
    {
        ValidEventPacket packet = this.eventGenerator.createECGEventPacket(MicroSensorDataType.WINDOWACTIVITYUNKNOWN);

        assertNull(this.eventValidator.validate(packet));

    }

}
