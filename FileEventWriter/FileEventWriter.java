/*
 * Created on 07.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


import org.electrocodeogram.module.writer.EventWriter;
import org.electrocodeogram.sensorwrapper.EventPacket;


/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileEventWriter extends EventWriter
{

    private final String HOME = System.getProperty("user.home");
    
    private final String DIR = "microStat";
    
    private final String FILENAME = "FileEventWriter";
    
    private final String EXTENSION = ".txt";
    
    private BufferedWriter bw = null;

    private static int count = 0;
    
    File file = null;
    
    public FileEventWriter()
    {
        super("FileEventWriter");
        file = new File(HOME + File.separator + DIR);
        if (! file.exists()) file.mkdir();
        file = new File(HOME + File.separator + DIR + File.separator + count + FILENAME + EXTENSION);
        while(file.exists())
        {
            count++;
            file = new File(HOME + File.separator + DIR + File.separator + count + FILENAME + EXTENSION);
            
        }
    }
    
    /* (non-Javadoc)
     * @see net.datenfabrik.microstat.core.writer.EventWriter#write(java.lang.String)
     */
    public void write(EventPacket eventPacket)
    {
        try {
            bw = new BufferedWriter(new FileWriter(file,true));
            
            bw.write(eventPacket.getTimeStamp().toString() + " : ");
            
            bw.write(eventPacket.getCommandName());
            
            List argList = eventPacket.getArglist();
            
            if (argList != null)
            {
            
	            Object[] args = argList.toArray();
	            
	            for (int i=0;i<args.length;i++)
	            {
	                String str = (String) args[i];
	                
	                bw.write(" " + str);
	            }
            }    
            
            bw.newLine();
            
            bw.close();
        
            
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
