/*
 * Class: SocketSourceModule
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.system.ModuleSystem;

/**
 * This module receives event data from multiple client sensors.
 * The communication is done over Sockets. Each new incoming
 * communication request starts a new SocketServerThread which
 * than receives the event data from the new client.
 *
 */
public class SocketSourceModule extends SourceModule
{

	/**
	 * 
	 */
	private static final int DEFAULT_PORT = 22222;

	/**
	 * 
	 */
	protected static final int MAX_PORT = 65536;

	/**
	 * 
	 */
	protected static final int MIN_PORT = 1024;

	private static Logger logger = LogHelper.createLogger(SocketSourceModule.class.getName());

	private int port;

	private SocketServer socketServer;

	/**
	 * @param moduleClassId
	 * @param name
	 */
	public SocketSourceModule(String moduleClassId, String name)
	{
		super(moduleClassId, name);

        logger.entering(this.getClass().getName(), "SocketSourceModule");
        
		logger.exiting(this.getClass().getName(), "SocketSourceModule");

	}

//	/**
//	 * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
//	 */
//	@Override
//	public void startReader(SourceModule sourceModule)
//	{
//		_logger.entering(this.getClass().getName(), "startReader");
//
//		this._socketServer = new SocketServer(sourceModule, this._port);
//
//		this._socketServer.start();
//
//		_logger.exiting(this.getClass().getName(), "startReader");
//
//	}

//	public void stopReader()
//	{
//		_logger.entering(this.getClass().getName(), "stopReader");
//
//		this._socketServer.shutDown();
//
//		this._socketServer = null;
//
//		_logger.exiting(this.getClass().getName(), "stopReader");
//	}

	
	@Override
	public void analyseCoreNotification()
	{
		logger.entering(this.getClass().getName(), "analyseCoreNotification");

		// not implemented

		logger.exiting(this.getClass().getName(), "analyseCoreNotification");
	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public void initialize()
	{
		logger.entering(this.getClass().getName(), "initialize");

		this.port = DEFAULT_PORT;

		logger.exiting(this.getClass().getName(), "initialize");
	}

  
    @Override
    public void propertyChanged(ModuleProperty moduleProperty) throws ModulePropertyException
    {
        if(moduleProperty.getName().equals("Show Clients"))
        {
            JFrame frame = ModuleSystem.getInstance().getRootFrame();
            
            if(this.socketServer == null)
            {
                JOptionPane.showMessageDialog(frame,"The module has not been started yet.","Show Clients",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
            String message = "";
            
            int count = this.socketServer.getSensorCount();
            
            message += "Connected to " + count + " ECG clients.\n";
            
            for(int i=0;i<count;i++)
            {
                message += "Client " + i + ": " + this.socketServer.getSensorNames()[i] + " at " + this.socketServer.getSensorAddresses()[i].toString() + "\n";
            }
            
            JOptionPane.showMessageDialog(frame,message,"ECG Clients",JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        if (moduleProperty.getName().equals("port"))
        {
            logger.log(Level.INFO, "Request to set the property: " + moduleProperty.getName());

            try
            {
                int portValue = Integer.parseInt(moduleProperty.getValue());

                if (portValue > MIN_PORT && portValue < MAX_PORT)
                {
                    this.port = portValue;

                    logger.log(Level.INFO, "Property: " + moduleProperty.getName() + " set.");

                    if(this.getState())
                    {
                        this.deactivate();
                        
                        this.activate();
                        
                    }
                   
                }
                else
                {
                    logger.log(Level.WARNING, "The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");

                    throw new ModulePropertyException(
                            "The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".", this.getName(), this.getId(), moduleProperty.getName(), moduleProperty.getValue());
                }

            }
            catch (NumberFormatException e)
            {
                logger.log(Level.WARNING, "The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");

                throw new ModulePropertyException(
                        "The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".", this.getName(), this.getId(), moduleProperty.getName(), moduleProperty.getValue());
            } catch (ModuleActivationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public EventReader[] getEventReader() {
        
        logger.entering(this.getClass().getName(), "getEventReader");
        
        logger.exiting(this.getClass().getName(), "getEventReader",this.socketServer.getEventReader());
        
        return this.socketServer.getEventReader();
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public void preStart() {
        
        logger.entering(this.getClass().getName(), "preStart");
        
        if(this.socketServer == null)
        {
            logger.log(Level.FINE,"No SocketServer started yet. Going to start one...");
            
            this.socketServer = new SocketServer(this,this.port);
            
            this.socketServer.start();
            
            logger.log(Level.FINE,"A SocketServer has been started.");
        }
        else
        {
            logger.log(Level.FINE,"A SocketServer is already started.");
        }
        
        logger.exiting(this.getClass().getName(), "preStart");
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     */
    @Override
    public void postStop() {
       
        logger.entering(this.getClass().getName(), "postStop");
        
        if(this.socketServer != null)
        {
            logger.log(Level.FINE,"A SocketServer is present. Going to shut it down...");
            
            this.socketServer.shutDown();
            
            this.socketServer = null;
            
            logger.log(Level.FINE,"The SocketServer has been shut down.");
        }
        else
        {
            logger.log(Level.FINE,"The SocketServer is already shut down.");
        }
        
        logger.exiting(this.getClass().getName(), "postStop");
    }
}