package org.electrocodeogram.module.source;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * 
 */
public class ManualAnnotatorSourceModule extends SourceModule
{

	private ManualAnnotatorFrame _frame;
	
	ManualAnnotatorEvents _events;
	
	/**
	 * @param arg0
	 * @param arg1
	 */
	public ManualAnnotatorSourceModule(String arg0, String arg1)
	{
		super(arg0, arg1);
		
		
	}

	/**
	 * @param propertyName
	 * @param propertyValue
	 * @throws ModulePropertyException
	 * 
	 */
	@Override
	public void setProperty(String propertyName, String propertyValue) throws ModulePropertyException
	{
		if(propertyName.equals("Show Dialog"))
		{
			this._frame.setVisible(true);
		}
		else if(propertyName.equals("Events"))
		{
			
		}
		else
		{
			throw new ModulePropertyException(
					"The module does not support a property with the given name: " + propertyName);
		}
		
		getLogger().log(Level.INFO,"The " + propertyName + " property has been set to " + propertyValue);
	}

	public void analyseCoreNotification()
	{

	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public void initialize()
	{
		for(ModuleProperty property : this.runtimeProperties)
		{
			if(property.getName().equals("Events"))
			{
				this._events = new ManualAnnotatorEvents(property.getValue());
			}
		}
		
	}

	/**
	 * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
	 */
	@Override
	public void startReader(SourceModule sourceModule)
	{
		this._frame = new ManualAnnotatorFrame(this);
		
	}

	
	private static class ManualAnnotatorFrame extends JFrame
	{
		
		private ManualAnnotatorSourceModule _sourceModule;
		
		public ManualAnnotatorFrame(ManualAnnotatorSourceModule sourceModule)
		{
			this._sourceModule = sourceModule;
			
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			this.setTitle("Manual Annotation");
			
			this.setBounds(10,10,200,200);
			
			this.setLayout(new FlowLayout());
			
			this.setEvents(this._sourceModule._events.getEvents());
			
			this.pack();
			
			this.setVisible(true);
				
		}
		
		public void setEvents(String[] events)
		{
			for(String event : events)
			{
				if(event != null && !event.equals(""))
				{
					JButton btnEvent = new JButton(event);
					
					btnEvent.addActionListener(new EventActionAdapter(event,this._sourceModule));
					
					this.getContentPane().add(btnEvent);
				}
			}
		}
		
		private static class EventActionAdapter implements ActionListener
		{

			private String _event;
			
			private ManualAnnotatorSourceModule _sourceModule;
			
			public EventActionAdapter(String event, ManualAnnotatorSourceModule sourceModule)
			{
				this._event = event;
				
				this._sourceModule = sourceModule;
			}
			
			/**
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e)
			{
				String microActivity = "<?xml version=\"1.0\"?><microActivity><manual><value>" + this._event + "</value></manual></microActivity>";
				
				String[] args = { "add", "MicroActivity", microActivity};
				
				try
				{
					ValidEventPacket packet = new ValidEventPacket(this._sourceModule.getId(),new Date(),"Activity",Arrays.asList(args));
					
					this._sourceModule.append(packet);
				}
				catch (IllegalEventParameterException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.source.SourceModule#stopReader()
	 */
	@Override
	public void stopReader()
	{
		this._frame.dispose();
		
		this._frame = null;
		
	}
}
