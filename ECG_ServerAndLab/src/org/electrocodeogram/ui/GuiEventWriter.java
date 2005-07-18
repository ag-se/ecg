/*
 * Created on 11.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.util.List;

import org.electrocodeogram.core.Core;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.writer.EventWriter;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GuiEventWriter extends EventWriter
{

    private static GuiEventWriter theInstance = null;
    
    private MessagesTarget target = null;
    
    /**
     * @param name
     */
    private GuiEventWriter(MessagesTarget target)
    {
        super(Core.getInstance().getModuleRegistry(),"GuiEventWriter");
        
        assert(target != null);
        
        this.target = target;
        
        this.activate();
    }
    
    private GuiEventWriter()
    {
        super(Core.getInstance().getModuleRegistry(),"GuiEventWriter");
    }

    public static GuiEventWriter getInstance(MessagesTarget target)
    {
        if (theInstance == null)
        {
            theInstance = new GuiEventWriter(target);
        }
        else
        {
            if(theInstance.target == null)
            {
                theInstance.target = target;
            }
        }
        return theInstance;
    }
    
    public static GuiEventWriter getInstance()
    {
        if(theInstance == null)
        {
            theInstance = new GuiEventWriter();
        }
        return theInstance;
    }
    
    /* (non-Javadoc)
     * @see org.electrocodeogram.module.writer.EventWriter#write(org.electrocodeogram.EventPacket)
     */
    public void write(ValidEventPacket eventPacket)
    {
        if(target != null)
        {
        if (eventPacket.getSourceId() == Core.getInstance().getConfigurator().getSelectedModuleCellId())
        {
          target.append(eventPacket.getTimeStamp().toString() + "," + eventPacket.getHsCommandName());

          List argList = eventPacket.getArglist();

          if (argList != null) {

              Object[] args = eventPacket.getArglist().toArray();

              int count = args.length;
              
              for (int i = 0; i < count; i++) {
                  String str = (String) args[i];
                  
                  if(str.equals(new String("")))
                  {
                      continue;
                  }
                  target.append("," + str);
                  
                  
              }

          }
          target.append("\n");

//          JScrollBar vertBar = scrollPane.getVerticalScrollBar();
//          if (vertBar.getValue() == vertBar.getMaximum() - vertBar.getVisibleAmount()) {
//              shouldScroll = true;
//          }
        }
      }        
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String currentPropertyName, Object propertyValue)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.EventPacket)
     */


}
