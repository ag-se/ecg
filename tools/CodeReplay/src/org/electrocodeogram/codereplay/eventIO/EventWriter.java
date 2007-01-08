package org.electrocodeogram.codereplay.eventIO;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.electrocodeogram.codereplay.dataProvider.Replay;
import org.electrocodeogram.codereplay.dataProvider.ReplayElement;

/**
 * Implementation of AbstractWriter that is able to write files in the format specified by 'ecg_exactcodechange.xsd'.
 * (see ecg_lab/msdt for the .xsd)
 * 
 * @author marco kranz
 */
public class EventWriter extends AbstractWriter{

	private String name = "EventWriter";
	// singleton instance
	private static EventWriter writer;
	
	private PrintStream printstream;
	
	
	private EventWriter(){
	}
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.codereplay.eventIO.AbstractWriter#getInstance()
	 */
	public static EventWriter getInstance(){
		if(writer == null)
			writer = new EventWriter();
		return writer;
	}
	
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.codereplay.eventIO.AbstractWriter#getName()
	 */
	public String getName(){
		return name;
	}
	
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.codereplay.eventIO.AbstractWriter#write(java.io.File, java.util.Enumeration)
	 */
	public void write(File file, Enumeration replays) throws IOException{
		printstream = new PrintStream(file);
		ReplayElement replayelement;
		Replay replay;
		Collection elements;
		Iterator it;
		while(replays.hasMoreElements()){
			replay = (Replay)replays.nextElement();
			elements = replay.getElements();
			it = elements.iterator();
			while(it.hasNext()){
				replayelement = (ReplayElement)it.next();
				writeElement(replayelement);
			}
		}
	}

	private void writeElement(ReplayElement elem) {
		String output;
		SimpleDateFormat dateformat = new SimpleDateFormat("EE dd.MM.yyyy HH:mm:ss z");
		StringBuffer buffer = dateformat.format(elem.getTimestamp(), new StringBuffer(), new FieldPosition(0));
		output = buffer.toString()+"#Activity#;add;msdt.exactcodechange.xsd;<?xml version=\"1.0\"?><microActivity><commonData><username> </username><projectname> </projectname></commonData><exactCodeChange>";
		String[] path = elem.getPath();
		for(int i = 0; i < path.length; i++){
				output = output + "<path>"+path[i]+"</path>";
		}
		output = output + "<change><typeOfChange>"+elem.getExactChange()+"</typeOfChange>" +
						"<elementName>"+elem.getName()+"</elementName>" +
						"<identifier>"+elem.getIdentifier()+"</identifier>" +
						"<codeOrIdentifier><![CDATA["+elem.getSource()+"]]>" +
						"</codeOrIdentifier></change></exactCodeChange></microActivity>";
		printstream.println(output);
	}
}
