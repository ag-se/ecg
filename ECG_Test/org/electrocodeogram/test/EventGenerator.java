package org.electrocodeogram.test;

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

    /**
     * This enum collects represents all declared HackyStat SensorDataTypes.
     */
    public enum SensorDataType {ACTIVITY, BUFFTRANS, BUILD, CLI, COMMIT, COVERAGE, DEPENDENCY, FILEMETRIC, ISSUE, PERF, REVIEWACTIVITY, REVIEWISSUE, UNITTEST};

   
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
     * This method creates and returns a single EventPacket.
     * The data stored in the EventPacket is allowed to be syntactically invalid
     * in respect to the event data rules. So this method is used for testing purposes. 
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
    public EventPacket createEventPacket(boolean syntValidDate, boolean syntValidCommandName, boolean argListNotNull, boolean argListOfString, int argListLength, int argListEntrySize)
    {
        EventPacket eventPacket = null;
        eventPacket = new EventPacket(
                0,
                createDate(syntValidDate),
                createCommandName(syntValidCommandName),
                createArgList(argListNotNull, argListOfString, argListLength, argListEntrySize));

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
     * @throws IllegalEventParameterException This is thrown if the passes parameter data does not conform to the syntax rules of event data.
     */
    public ValidEventPacket createValidEventPacket(boolean syntValidDate, boolean syntValidCommandName, boolean argListNotNull, boolean argListOfString, int argListLength, int argListEntrySize) throws IllegalEventParameterException
    {
        ValidEventPacket eventPacket = null;
        eventPacket = new ValidEventPacket(
                0,
                createDate(syntValidDate),
                createCommandName(syntValidCommandName),
                createArgList(argListNotNull, argListOfString, argListLength, argListEntrySize));

        return eventPacket;
    }
    
    /**
     * This method creates and returns a valid HackyStat SensorDataType event.
     * @param type Is the SensorDataType to create
     * @return A valid HackyStat event.
     */
    public ValidEventPacket createHackyStatEventPacket(SensorDataType type)
    {
        ValidEventPacket eventPacket = null;
     
        String[] args = null;
        
        switch (type)
        {
        case ACTIVITY:
            
            args = new String[]{"add","test"};
            
            try {
                eventPacket = new ValidEventPacket(
                        0,
                        createDate(true),
                        "Activity",
                        Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }
            
            break;

        case BUFFTRANS:
            
            args = new String[]{"add","test","test","test"};
            
            try {
                eventPacket = new ValidEventPacket(
                        0,
                        createDate(true),
                        "BuffTrans",
                        Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }
       
        case BUILD:
            
            args = new String[]{"add","test","test","test","test","test","test","test","test","test","test"};
            
            try {
                eventPacket = new ValidEventPacket(
                        0,
                        createDate(true),
                        "Build",
                        Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }
            
            break;
            
        case CLI:
            
            args = new String[]{"add","test","test","test","test"};
            
            try {
                eventPacket = new ValidEventPacket(
                        0,
                        createDate(true),
                        "CLI",
                        Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }

        case COMMIT:
            
            args = new String[]{"add","test","test","test","test","test","test","1","1","1","test"};
            
            try {
                eventPacket = new ValidEventPacket(
                        0,
                        createDate(true),
                        "Commit",
                        Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }
            
            break;
            
        case FILEMETRIC:
            
            args = new String[]{"addFileMetric","bcml","C:\\cvs\\foobarproject\\src\foo\\bar\\Bar.java","foo.bar.Bar","cbo=1,loc=2","1049798488530"};
            
            try {
                eventPacket = new ValidEventPacket(
                        0,
                        createDate(true),
                        "FileMetric",
                        Arrays.asList(args));
            }
            catch (IllegalEventParameterException e) {
                e.printStackTrace();
            }
            
            break;
          
        case UNITTEST:
            
            args = new String[]{"add","test","test","3000","test","test"};
            
            try {
                eventPacket = new ValidEventPacket(
                        0,
                        createDate(true),
                        "UnitTest",
                        Arrays.asList(args));
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
