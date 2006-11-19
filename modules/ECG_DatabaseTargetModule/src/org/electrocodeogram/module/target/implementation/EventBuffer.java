package org.electrocodeogram.module.target.implementation;

import java.util.LinkedList;

import org.electrocodeogram.event.ValidEventPacket;

public class EventBuffer{
	
	
	LinkedList<ValidEventPacket>buffer;
	
	int maxElements;
	
	public EventBuffer(int size){
		this.buffer = new LinkedList<ValidEventPacket>();
		maxElements = size;
		
	}
	
	public synchronized void put(ValidEventPacket eventPacket)
	{
		while(maxElements == buffer.size())
		{
			try
			{
				wait();
			} //try
			catch (InterruptedException ie)
			{
				System.out.println("An InterruptedException caught\n"+ie.getMessage());
				ie.printStackTrace();
			} //catch
		} //while
		
		buffer.add(eventPacket);
		notifyAll();
	} //put()
	
	
	
	public synchronized ValidEventPacket get()
	{
		while ( buffer.isEmpty() )
		{
			try
			{
				wait();
			} //try
			catch (InterruptedException ie)
			{
				System.out.println("An InterruptedException caught\n"+ie.getMessage());
				ie.printStackTrace();
			} //catch
		} //while
		
		notifyAll();
		return( buffer.removeFirst() );
	} //get()
	
	
	public synchronized int getSize()
	{
		return ( buffer.size() );
	} //getSize()

}
