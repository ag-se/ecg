package org.electrocodeogram.module.intermediate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.xerces.parsers.DOMParser;
import org.electrocodeogram.core.Core;
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
            
            this.logger.log(Level.INFO,changes.toString());
            
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
    public void setProperty(String propertyName, Object propertyValue)
    {
       if(propertyValue.equals("configureFilter"))
       {
           configureFilter();
       }
    }


    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    @Override
    public void analyseCoreNotification()
    {
        setFilterMap();
    }

    JDialog getDialog()
    {
        return this.dlgFilterConfiguration;
    }
    
    /**
     * 
     */
    private void setFilterMap()
    {
        this.msdtFilterMap = new HashMap<MicroSensorDataType,Boolean>();
        
        MicroSensorDataType[] msdts = Core.getInstance().getMsdtRegistry().getMicroSensorDataTypes();
        
        for(MicroSensorDataType msdt : msdts)
        {
            if(msdt.getName().equals("msdt.part.xsd"))
            {
                this.msdtFilterMap.put(msdt,new Boolean(false));
            }
            else
            {
                this.msdtFilterMap.put(msdt,new Boolean(true));
            }
        }
    }

    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#initialize()
     */
    @Override
    public void initialize()
    {
        this.msdtFilterMap = new HashMap<MicroSensorDataType,Boolean>();
        
        setFilterMap();
        
        this.setProcessingMode(ProcessingMode.FILTER);
        
    }
    
    /**
     * 
     */
    public void configureFilter()
    {
        if(this.msdtFilterMap.size() == 0)
        {
            JOptionPane.showMessageDialog(Core.getInstance().getGui().getRootFrame(),"There are no MicroSensorDataTypes loaded yet. Please add at least one SourceModule to load the core MicroSensorDataTypes.","Configure Filter Message",JOptionPane.INFORMATION_MESSAGE);
            
            return;
        }
        
        initializeCheckBoxes();
        
        this.dlgFilterConfiguration = new JDialog(Core.getInstance().getGui().getRootFrame(),"Configure Filter");
        
        this.pnlCheckBoxes = createMainPanel();
        
        this.dlgFilterConfiguration.getContentPane().add(this.pnlCheckBoxes);
        
        this.dlgFilterConfiguration.pack();
        
        this.dlgFilterConfiguration.setVisible(true);
    }
    
    void updateMsdtFilterMap()
    {
        for(MicroSensorDataType msdt : chkMsdtSelection.keySet())
        {
            JCheckBox chkBox = chkMsdtSelection.get(msdt);
            
            this.msdtFilterMap.remove(msdt);
            
            if(chkBox.isSelected())
            {
                this.msdtFilterMap.put(msdt,new Boolean(false));
            }
            else
            {
                this.msdtFilterMap.put(msdt,new Boolean(true));
            }
        }
    }
  
    
    void initializeCheckBoxes()
    {
        this.chkMsdtSelection = new HashMap<MicroSensorDataType,JCheckBox>();
        
        for(MicroSensorDataType msdt : this.msdtFilterMap.keySet())
        {
            if(this.msdtFilterMap.get(msdt).equals(new Boolean(true)))
            {
                this.chkMsdtSelection.put(msdt,new JCheckBox(msdt.getName(),false));
            }
            else
            {
                this.chkMsdtSelection.put(msdt,new JCheckBox(msdt.getName(),true));
            }
            
        }
    }
    
    void refreshCheckBoxes()
    {
        for(MicroSensorDataType msdt : this.msdtFilterMap.keySet())
        {
            if(this.msdtFilterMap.get(msdt).equals(new Boolean(true)))
            {
                this.chkMsdtSelection.get(msdt).setSelected(false);
            }
            else
            {
                this.chkMsdtSelection.get(msdt).setSelected(true);
            }
            
        }
    }
    
    JPanel createCheckBoxPanel()
    {
        this.pnlCheckBoxes = new JPanel();
        
        JPanel pnlLeft = new JPanel();
        
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
                
        JPanel pnlRight = new JPanel();
        
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        
        boolean left = true;
        
        for(JCheckBox chkMsdt : chkMsdtSelection.values())
        {
            if(left)
            {
                pnlLeft.add(chkMsdt);
                
                left = false;
            }
            else
            {
                pnlRight.add(chkMsdt);
                
                left = true;
            }
            
        }
        
        pnlCheckBoxes.add(pnlLeft);
        
        pnlCheckBoxes.add(pnlRight);
        
        return this.pnlCheckBoxes;
    }
    
    JPanel createMainPanel()
    {
        this.pnlMain = new JPanel();
        
        this.pnlMain.setLayout(new BorderLayout());
        
        this.pnlMain.add(new JLabel("Select the MicroSensorDataTypes that shall be filtered out"),BorderLayout.NORTH);
        
        this.pnlMain.add(createCheckBoxPanel(),BorderLayout.CENTER);
        
        this.pnlMain.add(getButtonPanel(),BorderLayout.SOUTH);
        
        return this.pnlMain;
    }
    
    /**
     * @return
     */
    private Component getButtonPanel()
    {
        JButton btnOK = new JButton("OK");
        
        btnOK.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                updateMsdtFilterMap();
                
                getDialog().dispose();
                
            }});
        
        JButton btnCancel = new JButton("Cancel");
        
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                getDialog().dispose();
                
            }});
        
        JButton btnClearAll = new JButton("Clear all");
        
        btnClearAll.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                for(JCheckBox chkMsdt : chkMsdtSelection.values())
                {
                    chkMsdt.setSelected(false);
                }
                
            }});
        
        JButton btnRestore = new JButton("Restore");
        
        btnRestore.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                refreshCheckBoxes();
                
            }

           
                
            });
        
        JPanel pnlButtons = new JPanel();
        
        pnlButtons.add(btnRestore);
        
        pnlButtons.add(btnClearAll);
        
        pnlButtons.add(btnCancel);
        
        pnlButtons.add(btnOK);
        
        return pnlButtons;
    }

}
