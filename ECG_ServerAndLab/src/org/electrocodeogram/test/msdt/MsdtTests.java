package org.electrocodeogram.test.msdt;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.EventValidator;
import org.electrocodeogram.msdt.MsdtManager;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.EventGenerator.MicroSensorDataType;

/**
 * This class collects testcases for validating the event validator mechanism on the ECG serverside.
 */
public class MsdtTests extends TestCase
{

    private MsdtManager msdtManager = null;

    private EventValidator eventValidator = null;

    private EventGenerator eventGenerator = null;

    /**
     * This creates the class and the needed MsdtManager and EventGenerator objects.
     */
    public MsdtTests()
    {
        try {
            this.msdtManager = new MsdtManager();

            this.eventGenerator = new EventGenerator();
        }
        catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp()
    {
        this.eventValidator = new EventValidator(this.msdtManager);

        this.eventValidator.setAllowNonHackyStatSDTConformEvents(false);

        this.eventValidator.setAllowNonECGmSDTConformEvents(false);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown()
    {
        this.eventValidator = null;
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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertFalse(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertFalse(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertFalse(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertFalse(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertTrue(this.eventValidator.validate(packet));

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

        assertFalse(this.eventValidator.validate(packet));

    }

}
