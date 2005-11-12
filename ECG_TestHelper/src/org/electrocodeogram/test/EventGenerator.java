package org.electrocodeogram.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;

/**
 * Is used to create events of any kind for testing purposes.
 */
public class EventGenerator {

    /**
     * The name of the file that contains the paylod strings.
     */
    private static final String FILENAME = "pseudorandom.strings";

    /**
     * Used to read in the {@link #FILENAME} file.
     */
    private BufferedReader br = null;

    /**
     * The number of lines in the {@link #FILENAME} file.
     */
    private static final int LINE_COUNT = 100;

    /**
     * The length of each line in the {@link #FILENAME} file.
     */
    private static final int LINE_LENGTH = 100;

    /**
     * Contains each line from {@link #FILENAME} as an element.
     */
    private String[] randomStrings = new String[LINE_LENGTH];

    /**
     * All <em>HackyStat SensorDataTypes</em>.
     */
    public enum SensorDataType {
        ACTIVITY, BUFFTRANS, BUILD, CLI, COMMIT, COVERAGE, DEPENDENCY,
            FILEMETRIC, ISSUE, PERF, REVIEWACTIVITY, REVIEWISSUE, UNITTEST
    }

    /**
     * Contains multiple entries for each of the predefined
     * <em>MicroSensorDataTypes</em>. For every
     * <em>MicroSensorDataType</em> there is at least one entry for
     * valid value variations and one invalid varitation.
     */
    public enum MicroSensorDataType {
        RESOURCEADDED, RESOURCEREMOVED, RESOURCECHANGED,
            RESOURCEACTIVITYUNKNOWN, CODECHANGE, WINDOWOPENED, WINDOWCLOSED,
            WINDOWACTIVATED, WINDOWDEACTIVATED, WINDOWACTIVITYUNKNOWN,
            PARTOPENED, PARTCLOSED, PARTACTIVATED, PARTDEACTIVATED,
            PARTACTIVITYUNKNOWN, RUNDEBUGNODEBUG, RUNDEBUGWITHDEBUG,
            RUNDEBUGWITHILLEGALDEBUG, EDITOROPENED, EDITORCLOSED,
            EDITORACTIVATED, EDITORDEACTIVATED, EDITORACTIVITYUNKNOWN,
            TESTRUNSTARTED, TESTRUNENDED, TESTRUNSTOPPED,
            TESTRUNACTIVITYUNKNOWN, TESTSTARTED, TESTENDED, TESTFAILED,
            TESTRUNTERMINATED, TESTRERAN, TESTACTIVITYUNKNOWN
    }

    /**
     * Creates the <em>EventGenerator</em> and initializes
     * {@link #randomStrings} by reading in the {@link #FILENAME}
     * file. If the {@link #FILENAME} file is not found it is created
     * by {@link #generateRandomStringFile()}.
     * @throws IOException
     *             If the reading of {@link #FILENAME} fails.
     */
    public EventGenerator() throws IOException {

        File file = new File(".." + File.separator + "ECG_TestHelper"
                             + File.separator + FILENAME);

        if (!file.exists()) {
            this.generateRandomStringFile();
        }

        this.br = new BufferedReader(new FileReader(file));

        int i = 0;

        while (i < LINE_COUNT
               && (this.randomStrings[i++] = this.br.readLine()) != null) {
            // all happens in the condition, sorry ;)
        }

        this.br.close();

    }

    /**
     * Creates a <em>Data</em> that is either the current
     * <em>Date</em> or <code>null</code>.
     * @param wellformed
     *            If wellformed is <code>true</code> the current
     *            <em>Date</em> is returned. Otherwise
     *            <code>null</code> is returned.
     * @return If wellformed is <code>true</code> the current
     *         <em>Date</em> is returned. Otherwise
     *         <code>null</code> is returned.
     */
    private Date createDate(final boolean wellformed) {
        if (wellformed) {
            return new Date();
        }

        return null;

    }

    /**
     * Creates a wellformed <em>commandName</em> by returning the
     * given line of the {@link #FILENAME} file or <code>null</code>.
     * @param wellformed
     *            If wellformed is <code>true</code> the line from
     *            the {@link #FILENAME} file given by linenumber is
     *            returned. Otherwise <code>null</code> is returned.
     * @param linenumber
     *            Is the number of the line from the {@link #FILENAME}
     *            file to be returned as the <em>commandName</em>.
     *            This is only used when wellfromed is
     *            <code>true</code>.
     * @return Is the number of the line from the {@link #FILENAME}
     *         file to be returned as the <em>commandName</em>.
     * @throws NoTestDataException
     *             If linenumber exceeds the {@link #FILENAME} file
     */
    private String createCommandName(final boolean wellformed,
        final int linenumber) throws NoTestDataException {
        if (!wellformed) {
            return null;
        }

        if (linenumber < 0 || linenumber > LINE_COUNT) {
            throw new NoTestDataException();
        }

        return this.randomStrings[linenumber];

    }

    /**
     * Used to create a stringlist with the given attributes. The
     * entries are comming from the {@link #FILENAME} file.
     * @param argListNotNull
     *            Shall the list be <code>null</code>?
     * @param argListOfString
     *            Shall it be a stringlist? If not, an integerlist is
     *            returned
     * @param argListLength
     *            The length of the list
     * @param argListEntrySize
     *            The size of the list's entries
     * @return The list with the given attributes with entries are
     *         from the {@link #FILENAME} file
     * @throws NoTestDataException
     *             If linenumber exceeds the {@link #FILENAME} file
     */
    private List createDeterministicArgList(final boolean argListNotNull,
        final boolean argListOfString, final int argListLength,
        final int argListEntrySize) throws NoTestDataException {
        if (!argListNotNull) {
            return null;
        }

        if (!argListOfString) {
            return createListOfIntegers(argListLength);
        }

        return Arrays.asList(createDeterministicPayloadStringArray(
            argListLength, argListEntrySize));

    }

    /**
     * Creates a stringlist with random entries.
     * @param argListLength
     *            The length of the list
     * @param argListEntrySize
     *            The size of each list's entry
     * @return A list with the given attributes a random entries
     * @throws NoTestDataException
     *             If linenumber exceeds the {@link #FILENAME} file
     */
    private List createNonDeterministicArgList(final int argListLength,
        final int argListEntrySize) throws NoTestDataException {
        return Arrays.asList(createRandomPayloadStringArray(argListLength,
            argListEntrySize));

    }

    /**
     * Creates an event that can be malformed or wellformed.
     * @param wellformedDate
     *            Shall the timestamp be wellformed?
     * @param wellformedCommandName
     *            Shall the commmandName be wellformed?
     * @param linenumber
     *            Is the number of the line from the {@link #FILENAME}
     *            file to be returned as the <em>commandName</em>.
     * @param argListNotNull
     *            Shall the argList be wellformed?
     * @param argListOfString
     *            Shall it be a stringlist?
     * @param argListLength
     *            The length of the argList
     * @param argListEntrySize
     *            The size of each list's element
     * @return An event with the given attributes
     * @throws NoTestDataException
     *             If a pseudo-random String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final EventPacket createEventPacket(final boolean wellformedDate,
        final boolean wellformedCommandName, final int linenumber,
        final boolean argListNotNull, final boolean argListOfString,
        final int argListLength, final int argListEntrySize)
        throws NoTestDataException {
        EventPacket eventPacket = null;
        eventPacket = new EventPacket(0, createDate(wellformedDate),
            createCommandName(wellformedCommandName, linenumber),
            createDeterministicArgList(argListNotNull, argListOfString,
                argListLength, argListEntrySize));

        return eventPacket;
    }

    /**
     * Creates an event that is wellformed.
     * @param wellformedDate
     *            Shall the timestamp be wellformed?
     * @param wellformedCommandName
     *            Shall the commmandName be wellformed?
     * @param linenumber
     *            Is the number of the line from the {@link #FILENAME}
     *            file to be returned as the <em>commandName</em>.
     * @param argListNotNull
     *            Shall the argList be wellformed?
     * @param argListOfString
     *            Shall it be a stringlist?
     * @param argListLength
     *            The length of the argList
     * @param argListEntrySize
     *            The size of each list's element
     * @return A wellformed event with the given attributes
     * @throws IllegalEventParameterException
     *             If it is thrown by
     *             {@link WellFormedEventPacket#WellFormedEventPacket(int, java.util.Date, java.lang.String, java.util.List)}
     * @throws NoTestDataException
     *             If a pseudo-random String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final WellFormedEventPacket createWellformedEventPacket(
        final boolean wellformedDate, final boolean wellformedCommandName,
        final int linenumber, final boolean argListNotNull,
        final boolean argListOfString, final int argListLength,
        final int argListEntrySize) throws IllegalEventParameterException,
        NoTestDataException {
        WellFormedEventPacket eventPacket = null;
        eventPacket = new WellFormedEventPacket(0, createDate(wellformedDate),
            createCommandName(wellformedCommandName, linenumber),
            createDeterministicArgList(argListNotNull, argListOfString,
                argListLength, argListEntrySize));

        return eventPacket;
    }

    /**
     * This method creates a wellformed event with payload of the
     * given size.
     * @param size
     *            Is the number of chars in the payload.
     * @return The event with the payload
     * @throws IllegalEventParameterException
     *             If it is thrown by
     *             {@link WellFormedEventPacket#WellFormedEventPacket(int, java.util.Date, java.lang.String, java.util.List)}
     * @throws NoTestDataException
     *             If a pseudo-random String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final WellFormedEventPacket createPayloadEventPacket(final int size)
        throws IllegalEventParameterException, NoTestDataException {
        WellFormedEventPacket eventPacket = null;
        eventPacket = new WellFormedEventPacket(0, createDate(true),
            createCommandName(true, 0), createNonDeterministicArgList(10,
                size / 10));

        return eventPacket;
    }

    /**
     * Creates a valid <em>HackyStat</em> event of the given
     * {@link SensorDataType}. SensorDataType event.
     * @param type
     *            Is the <em>HackyStat SensorDataType</em> of the
     *            event
     * @param line
     *            Is the number of the line from the {@link #FILENAME}
     *            file to be returned as the <em>commandName</em>
     * @return A valid <em>HackyStat</em> event of the given
     *         <em>SensorDataType</em>
     * @throws NoTestDataException
     *             If a pseudo-random String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final WellFormedEventPacket createHackyStatEventPacket(
        final SensorDataType type, final int line) throws NoTestDataException {

        if (line < 0 || line > LINE_COUNT) {
            throw new NoTestDataException();
        }

        WellFormedEventPacket eventPacket = null;

        String[] args = null;

        switch (type) {
            case ACTIVITY:

                args = new String[] {"add", this.randomStrings[line],
                    this.randomStrings[line]};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case BUFFTRANS:

                args = new String[] {"add", this.randomStrings[line],
                    this.randomStrings[line], this.randomStrings[line]};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "BuffTrans", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case BUILD:

                args = new String[] {"add", this.randomStrings[line],
                    this.randomStrings[line], this.randomStrings[line],
                    this.randomStrings[line]};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Build", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case CLI:

                args = new String[] {"add", this.randomStrings[line],
                    this.randomStrings[line], this.randomStrings[line],
                    this.randomStrings[line], this.randomStrings[line]};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "CLI", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case COMMIT:

                args = new String[] {"add", this.randomStrings[line],
                    this.randomStrings[line], this.randomStrings[line],
                    this.randomStrings[line], this.randomStrings[line],
                    this.randomStrings[line], "" + line, "" + line, "" + line,
                    this.randomStrings[line]};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Commit", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case FILEMETRIC:

                args = new String[] {this.randomStrings[line],
                    "C:\\cvs\\foobarproject\\src\foo\\bar\\Bar.java",
                    "foo.bar.Bar", "cbo=1,loc=2", "1049798488530"};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "FileMetric", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case UNITTEST:

                args = new String[] {this.randomStrings[line],
                    this.randomStrings[line], this.randomStrings[line], "3",
                    this.randomStrings[line], this.randomStrings[line]};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "UnitTest", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }
        return eventPacket;
    }

    /**
     * Creates a valid <em>MicroActivityEvent</em> of the given
     * {@link MicroSensorDataType}.
     * @param type
     *            Is the <em>MicroSensorDataType</em> of the event
     * @return A valid <em>MicroActivityEvent</em> of the given
     *         <em>MicroSensorDataType</em>.
     */
    public final WellFormedEventPacket createECGEventPacket(
        final MicroSensorDataType type) {

        WellFormedEventPacket eventPacket = null;

        String[] args = null;

        String activity = null;

        switch (type) {

            case TESTRUNSTARTED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprjectname"
                           + "</projectname></commonData><testrun><activity>started</activity><elapsedtime>0</elapsedtime><testcount>"
                           + 15 + "</testcount></testrun></microActivity>";

                args = new String[] {"add", "msdt.testrun.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTRUNENDED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprjectname"
                           + "</projectname></commonData><testrun><activity>ended</activity><elapsedtime>"
                           + 100
                           + "</elapsedtime><testcount>"
                           + 15
                           + "</testcount></testrun></microActivity>";

                args = new String[] {"add", "msdt.testrun.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTRUNSTOPPED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprjectname"
                           + "</projectname></commonData><testrun><activity>stopped</activity><elapsedtime>"
                           + 100
                           + "</elapsedtime><testcount>"
                           + 15
                           + "</testcount></testrun></microActivity>";

                args = new String[] {"add", "msdt.testrun.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTRUNTERMINATED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprjectname"
                           + "</projectname></commonData><testrun><activity>terminated</activity><elapsedtime>0</elapsedtime><testcount>"
                           + 15 + "</testcount></testrun></microActivity>";

                args = new String[] {"add", "msdt.testrun.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTRUNACTIVITYUNKNOWN:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprjectname"
                           + "</projectname></commonData><testrun><activity>unknownTestActivity</activity><elapsedtime>0</elapsedtime><testcount>"
                           + 15 + "</testcount></testrun></microActivity>";

                args = new String[] {"add", "msdt.testrun.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTSTARTED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprojectname"
                           + "</projectname></commonData><test><activity>started</activity><name>"
                           + "testName"
                           + "</name><id>"
                           + "testId"
                           + "</id><status>ok</status></test></microActivity>";

                args = new String[] {"add", "msdt.test.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTACTIVITYUNKNOWN:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprojectname"
                           + "</projectname></commonData><test><activity>unknownTestActivity</activity><name>"
                           + "testName"
                           + "</name><id>"
                           + "testId"
                           + "</id><status>ok</status></test></microActivity>";

                args = new String[] {"add", "msdt.test.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTENDED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprojectname"
                           + "</projectname></commonData><test><activity>ended</activity><name>"
                           + "testName"
                           + "</name><id>"
                           + "testId"
                           + "</id><status>ok</status></test></microActivity>";

                args = new String[] {"add", "msdt.test.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTFAILED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprojectname"
                           + "</projectname></commonData><test><activity>failed</activity><name>"
                           + "testName"
                           + "</name><id>"
                           + "testId"
                           + "</id><status>"
                           + "error"
                           + "</status></test></microActivity>";

                args = new String[] {"add", "msdt.test.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case TESTRERAN:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                           + "testusername"
                           + "</username><projectname>"
                           + "testprojectname"
                           + "</projectname></commonData><test><activity>reran</activity><name>"
                           + "testName"
                           + "</name><id>"
                           + "testId"
                           + "</id><status>ok</status></test></microActivity>";

                args = new String[] {"add", "msdt.test.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case RESOURCEADDED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><resource><activity>added</activity><resourcename>testResourceName</resourcename><resourcetype>testResourceType</resourcetype></resource></microActivity>";

                args = new String[] {"add", "msdt.resource.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case RESOURCEREMOVED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><resource><activity>removed</activity><resourcename>testResourceName</resourcename><resourcetype>testResourceType</resourcetype></resource></microActivity>";

                args = new String[] {"add", "msdt.resource.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case RESOURCECHANGED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><resource><activity>changed</activity><resourcename>testResourceName</resourcename><resourcetype>testResourceType</resourcetype></resource></microActivity>";

                args = new String[] {"add", "msdt.resource.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case RESOURCEACTIVITYUNKNOWN:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><resource><activity>unknownTestActivity</activity><resourcename>testResourceName</resourcename><resourcetype>testResourceType</resourcetype></resource></microActivity>";

                args = new String[] {"add", "msdt.resource.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case CODECHANGE:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><codechange><document>testDocument</document><documentname>testDocumentName</documentname></codechange></microActivity>";

                args = new String[] {"add", "msdt.codechange.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case EDITOROPENED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><editor><activity>opened</activity><editorname>testEditorName</editorname></editor></microActivity>";

                args = new String[] {"add", "msdt.editor.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case EDITORCLOSED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><editor><activity>closed</activity><editorname>testEditorName</editorname></editor></microActivity>";

                args = new String[] {"add", "msdt.editor.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }
                break;

            case EDITORACTIVATED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><editor><activity>activated</activity><editorname>testEditorName</editorname></editor></microActivity>";

                args = new String[] {"add", "msdt.editor.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case EDITORDEACTIVATED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><editor><activity>deactivated</activity><editorname>testEditorName</editorname></editor></microActivity>";

                args = new String[] {"add", "msdt.editor.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case EDITORACTIVITYUNKNOWN:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><editor><activity>unknownTestActivity</activity><editorname>testEditorName</editorname></editor></microActivity>";

                args = new String[] {"add", "msdt.editor.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case PARTOPENED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><part><activity>opened</activity><partname>testPartName</partname></part></microActivity>";

                args = new String[] {"add", "msdt.part.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case PARTCLOSED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><part><activity>closed</activity><partname>testPartName</partname></part></microActivity>";

                args = new String[] {"add", "msdt.part.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }
                break;

            case PARTACTIVATED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><part><activity>activated</activity><partname>testPartName</partname></part></microActivity>";

                args = new String[] {"add", "msdt.part.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case PARTDEACTIVATED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><part><activity>deactivated</activity><partname>testPartName</partname></part></microActivity>";

                args = new String[] {"add", "msdt.part.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case PARTACTIVITYUNKNOWN:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><part><activity>unknownTestActivity</activity><partname>testPartName</partname></part></microActivity>";

                args = new String[] {"add", "msdt.part.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case RUNDEBUGNODEBUG:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><run debug=\"false\"></run></microActivity>";

                args = new String[] {"add", "msdt.rundebug.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case RUNDEBUGWITHDEBUG:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><run debug=\"true\"></run></microActivity>";

                args = new String[] {"add", "msdt.rundebug.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case RUNDEBUGWITHILLEGALDEBUG:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><run debug=\"illegalTestValue\"></run></microActivity>";

                args = new String[] {"add", "msdt.rundebug.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case WINDOWOPENED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><window><activity>opened</activity><windowname>testWindowName</windowname></window></microActivity>";

                args = new String[] {"add", "msdt.window.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case WINDOWCLOSED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><window><activity>closed</activity><windowname>testWindowName</windowname></window></microActivity>";

                args = new String[] {"add", "msdt.window.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }
                break;

            case WINDOWACTIVATED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><window><activity>activated</activity><windowname>testWindowName</windowname></window></microActivity>";

                args = new String[] {"add", "msdt.window.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case WINDOWDEACTIVATED:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><window><activity>deactivated</activity><windowname>testWindowName</windowname></window></microActivity>";

                args = new String[] {"add", "msdt.window.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            case WINDOWACTIVITYUNKNOWN:

                activity = "<?xml version=\"1.0\"?><microActivity><commonData><username>testUserName</username><projectname>testProjectName</projectname></commonData><window><activity>unknownTestActivity</activity><windowname>testWindowName</windowname></window></microActivity>";

                args = new String[] {"add", "msdt.window.xsd", activity};

                try {
                    eventPacket = new WellFormedEventPacket(0,
                        createDate(true), "Activity", Arrays.asList(args));
                } catch (IllegalEventParameterException e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }
        return eventPacket;
    }

    // /**
    // * Given a String[] this method returns a syntactically valid
    // List
    // * of it with "add" and "testdata" being the first to entries.
    // * @param payload
    // * The String[] that builds the payload for the argList
    // * @return The argList for use in a syntactically valid
    // * EventPacket
    // */
    // private List createValidArglist(String[] payload) {
    // List list = Arrays.asList(payload);
    //
    // return list;
    // }

    /**
     * Creates a list of <em>Integer</em> objects. This is used for
     * the creation of malformed events.
     * @param listSize
     *            Is the length of the list.
     * @return The <em>Integer</em> list.
     */
    private List createListOfIntegers(final int listSize) {
        List < Integer > linkedList = new LinkedList < Integer >();

        for (int i = 0; i < listSize; i++) {
            Integer integer = new Integer(i);

            linkedList.add(integer);
        }

        return linkedList;
    }

    /**
     * Creates a <code>String[]</code>, which elements are the
     * lines from the {@link #FILENAME} file beginning with the first
     * line.
     * @param length
     *            The length of the array.
     * @param elementSize
     *            The size of the elements
     * @return The array with elements from the {@link #FILENAME} file
     * @throws NoTestDataException
     *             If a pseudo-random String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    private String[] createDeterministicPayloadStringArray(final int length,
        final int elementSize) throws NoTestDataException {

        if (length < 0 || length > LINE_COUNT) {
            throw new NoTestDataException();
        }

        if (elementSize < 0 || elementSize > LINE_LENGTH) {
            throw new NoTestDataException();
        }

        String[] payloadStringArray = new String[length];

        for (int i = 0; i < length; i++) {

            String randomString = this.randomStrings[i];

            payloadStringArray[i] = randomString.substring(0, elementSize);

        }

        return payloadStringArray;
    }

    /**
     * Creates a <code>String[]</code>, which elements are ransom
     * strings.
     * @param length
     *            The length of the array.
     * @param elementSize
     *            The size of the elements
     * @return The array with random string elements
     * @throws NoTestDataException
     *             If a pseudo-random String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    private String[] createRandomPayloadStringArray(final int length,
        final int elementSize) throws NoTestDataException {

        if (length < 0) {
            throw new NoTestDataException();
        }

        if (elementSize < 0) {
            throw new NoTestDataException();
        }

        String[] payloadStringArray = new String[length];

        for (int i = 0; i < length; i++) {

            String randomString = this.createRandomString(elementSize);

            payloadStringArray[i] = randomString.substring(0, elementSize);

        }

        return payloadStringArray;
    }

    /**
     * Creates a random string of the given size. It is used to
     * generate the {@link  #FILENAME} file.
     * @param size
     *            The size of the string.
     * @return The randomly created string
     */
    private String createRandomString(int size) {

        Random random = new Random();

        String string = "";

        for (int i = 0; i < size; i++) {
            int rand = random.nextInt();

            int max = Character.MAX_VALUE;

            int value = rand % max;

            char c = (char) value;

            string += c;
        }

        return string;
    }

    /**
     * This method generates the {@link #FILENAME} file with
     * randomized strings in each line.
     * @throws IOException
     *             If writing the file fails
     */
    public void generateRandomStringFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(
            EventGenerator.FILENAME));

        for (int i = 0; i < LINE_COUNT; i++) {
            bw.write(this.createRandomString(LINE_LENGTH) + "\n");
        }

        bw.close();
    }

    /**
     * Calls {@link #generateRandomStringFile()} to create a new
     * {@link #FILENAME} file.
     * @param args
     *            Is not used
     */
    public static void main(final String args[]) {
        try {
            EventGenerator eventGenerator = new EventGenerator();

            eventGenerator.generateRandomStringFile();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}
