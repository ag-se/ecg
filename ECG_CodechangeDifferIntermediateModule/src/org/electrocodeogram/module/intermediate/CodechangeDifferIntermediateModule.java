package org.electrocodeogram.module.intermediate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.xerces.parsers.DOMParser;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import bmsi.util.Diff;


/**
 *
 */
public class CodechangeDifferIntermediateModule extends IntermediateModule
{

    private HashMap<MicroSensorDataType,Boolean> msdtFilterMap;
    
    private JDialog dlgFilterConfiguration;
    
    private HashMap<MicroSensorDataType,JCheckBox> chkMsdtSelection;
    
    private JPanel pnlCheckBoxes;
    
    private JPanel pnlMain;
    
    private Diff diff;
    
    private String lastCode; 
    
    
    /**
     * @param arg0
     * @param arg1
     */
    public CodechangeDifferIntermediateModule(String arg0, String arg1)
    {
        super(arg0, arg1);
    }
    
    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public TypedValidEventPacket analyse(TypedValidEventPacket packet)
    {
        if(packet.getMicroSensorDataType().getName().equals("msdt.codechange.xsd"))
        {
            if(this.lastCode == null)
            {
                this.lastCode = getCode(packet);
                
                return null;
            }
            
            String currentCode = getCode(packet);
            
            String[] lastLines = null;
            
            String[] currentLines = null;
            
            try {
                lastLines = getLines(this.lastCode);
                
                currentLines = getLines(currentCode);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            this.diff = new Diff(lastLines,currentLines);
            
            Diff.change changes = this.diff.diff(Diff.forwardScript);
            
            this.getLogger().log(Level.INFO,"first deleted at: " + changes.line0);
            
            this.getLogger().log(Level.INFO,"first inserted at: " + changes.line1);
            
            this.lastCode = currentCode;
        }
                
        return null;
       
    }

    /**
     * @param code
     * @return
     * @throws IOException 
     */
    private String[] getLines(String code) throws IOException
    {
        
        if(code == null)
        {
            return null;
        }
        
        ArrayList<String> lines = new ArrayList<String>();
        
        BufferedReader reader = new BufferedReader(new StringReader(code));
        
        String line = null;
        
        while((line = reader.readLine()) != null)
        {
            lines.add(line);
        }
        
        return lines.toArray(new String[0]);
        
        
    }

    private String getCode(TypedValidEventPacket packet)
    {
        Object object = packet.getArglist().get(TypedValidEventPacket.MICROACTIVITY_INDEX);
        
        assert(object instanceof String);
        
        String microActivity = (String) object;
        
        Document document = null;

        InputSource inputSource = new InputSource(new StringReader(microActivity));
        
        DOMParser parser = new DOMParser();
        
        try {
            parser.parse(inputSource);
        }
        catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        document = parser.getDocument();
        
        Node documentNode = document.getElementsByTagName("document").item(0);
        
        return documentNode.getFirstChild().getNodeValue();
        
    }
    
    /**
     * @param propertyName
     * @param propertyValue
     */
    @Override
    public void setProperty(String propertyName, String propertyValue)
    {
       
    }


    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    @Override
    public void analyseCoreNotification()
    {
        
    }

  

    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#initialize()
     */
    @Override
    public void initialize()
    {
        this.msdtFilterMap = new HashMap<MicroSensorDataType,Boolean>();
        
             
        this.setProcessingMode(ProcessingMode.ANNOTATOR);
        
    }
    
   }
