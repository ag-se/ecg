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
import org.electrocodeogram.event.ValidEventPacket;

/**
 * This class is a test data generator. It provides methods to create
 * many types of valid and invalid event data.
 */
public class EventGenerator
{

    private static final String filename = "pseudorandom.strings";

    private BufferedReader br = null;

    private int lineCount = 100;

    private int lineLength = 100;

    private String[] randomStrings = new String[this.lineLength];

    /**
     * This enum collects represents all declared HackyStat SensorDataTypes.
     */
    public enum SensorDataType {
        ACTIVITY, BUFFTRANS, BUILD, CLI, COMMIT, COVERAGE, DEPENDENCY, FILEMETRIC, ISSUE, PERF, REVIEWACTIVITY, REVIEWISSUE, UNITTEST
    }

    /**
     * This creates the EventGenerator and initializes the randomString Array
     * by reading in the "pseudorandom.strings" file.
     * @throws IOException If initialization of the Reader or the reading of the file fails
     */
    public EventGenerator() throws IOException
    {

        File file = new File(
                ".." + File.separator + "ECG_TestHelper" + File.separator + filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        this.br = new BufferedReader(new FileReader(file));

        int i = 0;

        while (i < this.lineCount && (this.randomStrings[i++] = this.br.readLine()) != null) {
            // do nothing  
        }

        this.br.close();

    }

    // create a syntactically valid or invalid Date
    private Date createDate(boolean syntValidDatePar)
    {
        if (syntValidDatePar) {
            return new Date();
        }

        return null;

    }

    // create a syntactically valid or invalid commandName
    private String createCommandName(boolean syntValidCommandName, int line) throws NoTestDataException
    {
        if (!syntValidCommandName) {
            return null;
        }

        if (line < 0 || line > this.lineCount) {
            throw new NoTestDataException();
        }

        return this.randomStrings[line];

    }

    //  create a syntactically valid or invalid argList of given size
    private List createDeterministicArgList(boolean argListNotNull, boolean argListOfString, int argListLength, int argListEntrySize) throws NoTestDataException
    {
        if (!argListNotNull) {
            return null;
        }

        if (!argListOfString) {
            return createListOfIntegers(argListLength);
        }

        return createValidArglist(createDeterministicPayloadStringArray(argListLength, argListEntrySize));

    }

    private List createNonDeterministicArgList(int argListLength, int argListEntrySize) throws NoTestDataException
    {
        return createValidArglist(createNonDeterministicPayloadStringArray(argListLength, argListEntrySize));

    }

    /**
     * This method creates and returns a single EventPacket.
     * The data stored in the EventPacket is allowed to be syntactically invalid
     * in respect to the event data rules. So this method is used for testing purposes. 
     * 
     * @param syntValidDate
     *            Shall the timestamp be syntactically valid?
     * @param syntValidCommandName
     *            Shall the commmandName be syntactically valid?
     * @param line Is the line number of the String to use as the commandName from the "pseudorandom.strings" file
     * @param argListNotNull
     *            Shall the argList be not null?
     * @param argListOfString
     *            Shall the argList be of type List<String>?
     * @param argListLength
     *            The length of the argList
     * @param argListEntrySize
     *            The size of each list element
     * @return An EventPacket of the desired kind
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     */
    public EventPacket createEventPacket(boolean syntValidDate, boolean syntValidCommandName, int line, boolean argListNotNull, boolean argListOfString, int argListLength, int argListEntrySize) throws NoTestDataException
    {
        EventPacket eventPacket = null;
        eventPacket = new EventPacket(
                0,
                createDate(syntValidDate),
                createCommandName(syntValidCommandName, line),
                createDeterministicArgList(argListNotNull, argListOfString, argListLength, argListEntrySize));

        return eventPacket;
    }

    /**
     * This method creates and returns a single ValidEventPacket.
     * If this method returns the ValidEventPacket it is assured that
     * this object carries syntactically valid event data in it.
     * If this method is not able to create an ValidEventPacket from
     * the given event data parameters, am IllegalEventParameterException
     * is thrown.
     * 
     * @param syntValidDate
     *            Shall the timestamp be syntactically valid?
     * @param syntValidCommandName
     *            Shall the commmandName be syntactically valid?
     * @param line Is the line number of the String to use as the commandName from the "pseudorandom.strings" file
     * @param argListNotNull
     *            Shall the argList be not null?
     * @param argListOfString
     *            Shall the argList be of type List<String>?
     * @param argListLength
     *            The length of the argList
     * @param argListEntrySize
     *            The size of each list element
     * @return An EventPacket of the desired kind
     * @throws IllegalEventParameterException This is thrown if the passes parameter data does not conform to the syntax rules of event data.
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     */
    public ValidEventPacket createValidEventPacket(boolean syntValidDate, boolean syntValidCommandName, int line, boolean argListNotNull, boolean argListOfString, int argListLength, int argListEntrySize) throws IllegalEventParameterException, NoTestDataException
    {
        ValidEventPacket eventPacket = null;
        eventPacket = new ValidEventPacket(
                0,
                createDate(syntValidDate),
                createCommandName(syntValidCommandName, line),
                createDeterministicArgList(argListNotNull, argListOfString, argListLength, argListEntrySize));

        return eventPacket;
    }

    /**
     * This method creates and returns a ValidEventPacket with a payload of the given size.
     * @param size Is the size of the payload.
     * @return The event with the payload
     * @throws IllegalEventParameterException This is thrown if the passes parameter data does not conform to the syntax rules of event data.
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     */
    public ValidEventPacket createPayloadEventPacket(int size) throws IllegalEventParameterException, NoTestDataException
    {
        ValidEventPacket eventPacket = null;
        eventPacket = new ValidEventPacket(0, createDate(true),
                createCommandName(true, 0),
                createNonDeterministicArgList(10, size / 10));

        return eventPacket;
    }

    /**
     * This method creates and returns a valid HackyStat SensorDataType event.
     * @param type Is the SensorDataType to create
     * @param line Is the line number of the String to use as the commandName from the "pseudorandom.strings" file
     * @return A valid HackyStat event.
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     */
    public ValidEventPacket createHackyStatEventPacket(SensorDataType type, int line) throws NoTestDataException
    {

        if (line < 0 || line > this.lineCount) {
            throw new NoTestDataException();
        }

        ValidEventPacket eventPacket = null;

        String[] args = null;

        switch (type)
        {
        case ACTIVITY:

            args = new String[] { "add", this.randomStrings[line] };

            try {
                eventPacket = new ValidEventPacket(0, createDate(true),
                        "Activity", Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

            break;

        case BUFFTRANS:

            args = new String[] { "add", this.randomStrings[line], this.randomStrings[line], this.randomStrings[line] };

            try {
                eventPacket = new ValidEventPacket(0, createDate(true),
                        "BuffTrans", Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

            break;

        case BUILD:

            args = new String[] { "add", this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line] };

            try {
                eventPacket = new ValidEventPacket(0, createDate(true),
                        "Build", Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

            break;

        case CLI:

            args = new String[] { "add", this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line] };

            try {
                eventPacket = new ValidEventPacket(0, createDate(true), "CLI",
                        Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

            break;

        case COMMIT:

            args = new String[] { "add", this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], this.randomStrings[line], "" + line, "" + line, "" + line, this.randomStrings[line] };

            try {
                eventPacket = new ValidEventPacket(0, createDate(true),
                        "Commit", Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

            break;

        case FILEMETRIC:

            args = new String[] { "addFileMetric", "bcml", "C:\\cvs\\foobarproject\\src\foo\\bar\\Bar.java", "foo.bar.Bar", "cbo=1,loc=2", "1049798488530" };

            try {
                eventPacket = new ValidEventPacket(0, createDate(true),
                        "FileMetric", Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

            break;

        case UNITTEST:

            args = new String[] { "add", this.randomStrings[line], this.randomStrings[line], "3000", this.randomStrings[line], this.randomStrings[line] };

            try {
                eventPacket = new ValidEventPacket(0, createDate(true),
                        "UnitTest", Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

            break;

        default:
            break;
        }
        return eventPacket;
    }

    /**
     * Given a String[] this method returns a syntactically valid List of it
     * with "add" and "testdata" being the first to entries.
     * 
     * @param payload
     *            The String[] that builds the payload for the argList
     * @return The argList for use in a syntactically valid EventPacket
     */
    private List createValidArglist(String[] payload)
    {
        List list = Arrays.asList(payload);

        return list;
    }

    // create a List of Integer objects
    private List createListOfIntegers(int listSize)
    {
        List<Integer> linkedList = new LinkedList<Integer>();

        for (int i = 0; i < listSize; i++) {
            Integer integer = new Integer(i);

            linkedList.add(integer);
        }

        return linkedList;
    }

    /**
     * This methods creates a String[] of pseudo-random payload
     * by reading in the file "pseudorandom.strings".
     * 
     * @param arraySize
     *            The size of the String[]
     * @param stringSize
     *            The size of each String element
     * @return
     *            An Array of random Strings
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available 
     */
    private String[] createDeterministicPayloadStringArray(int arraySize, int stringSize) throws NoTestDataException
    {

        if (arraySize < 0 || arraySize > this.lineCount) {
            throw new NoTestDataException();
        }

        if (stringSize < 0 || stringSize > this.lineLength) {
            throw new NoTestDataException();
        }

        String[] payloadStringArray = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {

            String randomString = this.randomStrings[i];

            payloadStringArray[i] = randomString.substring(0, stringSize);

        }

        return payloadStringArray;
    }

    private String[] createNonDeterministicPayloadStringArray(int arraySize, int stringSize) throws NoTestDataException
    {

        if (arraySize < 0) {
            throw new NoTestDataException();
        }

        if (stringSize < 0) {
            throw new NoTestDataException();
        }

        String[] payloadStringArray = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {

            String randomString = this.createRandomString(stringSize);

            payloadStringArray[i] = randomString.substring(0, stringSize);

        }

        return payloadStringArray;
    }

    /**
     * This methods creates and returns a random String of the given element
     * count. It is used to generate the file "pseudorandom.strings".
     * 
     * @param stringSize
     *            The number of elements in the String to create randomly
     * @return The randomly created String
     */
    private String createRandomString(int stringSize)
    {

        Random random = new Random();

        String string = "";

        for (int i = 0; i < stringSize; i++) {
            int rand = random.nextInt();

            int max = Character.MAX_VALUE;

            int value = rand % max;

            char c = (char) value;

            string += c;
        }

        return string;
    }

    /**
     * Calling this method causes it to generate a file with randomized
     * Strings to be used in the testcases through the ECG framework testing.
     * @throws IOException If writing the file fails
     */
    public void generateRandomStringFile() throws IOException
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.filename));

        for (int i = 0; i < this.lineCount; i++) {
            bw.write(this.createRandomString(this.lineLength) + "\n");
        }

        bw.close();
    }

    /**
     * When the main method is called, the file "pseudorandom.strings" is (re)created.
     * 
     * @param args Is not used
     */
    public static void main(String args[])
    {
        try {
            EventGenerator eventGenerator = new EventGenerator();

            eventGenerator.generateRandomStringFile();
        }
        catch (IOException e) {

            e.printStackTrace();
        }

    }
}
