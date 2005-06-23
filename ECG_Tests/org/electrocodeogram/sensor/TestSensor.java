package org.electrocodeogram.sensor;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.electrocodeogram.TestEventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This is the ECG TestSensor used for automated JUnit tests. It is capable of
 * generating valid and different kinds of invalid EventPackets and it defines
 * methods to send these EventPackets.
 */
public class TestSensor
{

    private SensorShell shell = null;

    private SensorProperties properties = null;

    /**
     * Creates a TestSensor instance and initializes it with a SensorShell.
     *
     */
    public TestSensor()
    {
        this.properties = new SensorProperties("TestSensor");

        this.shell = new SensorShell(this.properties, false, "TestSensor");
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
    private String createCommandName(boolean syntValidCommandName)
    {
        if (syntValidCommandName) {
            return createRandomString(20);
        }

        return null;

    }

    //  create a syntactically valid or invalid argList of given size
    private List createArgList(boolean argListNotNull, boolean argListOfString, int argListLength, int argListEntrySize)
    {
        if (!argListNotNull) {
            return null;
        }

        if (!argListOfString) {
            return createListOfIntegers(argListLength);
        }

        return createValidArglist(createPayloadStringArray(argListLength, argListEntrySize));

    }

    /**
     * This method creates and returns a single EventPacket for testing purpose.
     * 
     * @param syntValidDate
     *            Shall the timestamp be syntacticallly vaild?
     * @param syntValidCommandName
     *            Shall the commmandName be syntacticallly vaild?
     * @param argListNotNull
     *            Shall the argList be not null?
     * @param argListOfString
     *            Shall the argList be of type List<String>?
     * @param argListLength
     *            The length of the argList
     * @param argListEntrySize
     *            The size of each list element
     * @return An EventPacket of the desired kind
     */
    public TestEventPacket createEventPacket(boolean syntValidDate, boolean syntValidCommandName, boolean argListNotNull, boolean argListOfString, int argListLength, int argListEntrySize)
    {
        TestEventPacket eventPacket = null;
        eventPacket = new TestEventPacket(
                0,
                createDate(syntValidDate),
                createCommandName(syntValidCommandName),
                createArgList(argListNotNull, argListOfString, argListLength, argListEntrySize));

        return eventPacket;
    }

    /**
     * This method passes a single given EventPacket to the ECG SensorShell.
     * 
     * @param eventPacket
     *            The EventPacket to pass
     * @return The result as given by the ECG Sensorshell. "true" means the
     *         EventPacket is syntactically valid and accepted. "false" means
     *         the eventPacket is syntactically invalid and not acccepted.
     */
    public boolean sendEvent(TestEventPacket eventPacket)
    {
        return this.shell.doCommand(eventPacket.getTimeStamp(), eventPacket.getHsCommandName(), eventPacket.getArglist());
    }

    /**
     * Given a String[] this method returns a syntactically valid List of it
     * with "add" and "testdata" being the first to entries.
     * 
     * @param payload
     *            The String[] that builds the payload for the argList
     * @return The argList for use in a syntactically valid eventPacket
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
     * This methods creates a String[] of random payload.
     * 
     * @param arraySize
     *            The size of the String[]
     * @param stringSize
     *            The size of each String element
     * @return
     *            An Arrray of random Strings
     */
    private String[] createPayloadStringArray(int arraySize, int stringSize)
    {
        String[] payloadStringArray = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {
            payloadStringArray[i] = new String(createRandomString(stringSize));
        }

        return payloadStringArray;
    }

    /**
     * This methods creates and returns a random String of the given element
     * count.
     * 
     * @param stringSize
     *            The number of elements in the String to create randomly
     * @return The randomly created String
     */
    private String createRandomString(int stringSize)
    {

        Random random = new Random();

        String string = new String();

        for (int i = 0; i < stringSize; i++) {
            int rand = random.nextInt();

            int max = Character.MAX_VALUE;

            int value = rand % max;

            char c = (char) value;

            string += c;
        }

        return string;
    }

}
