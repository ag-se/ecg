package org.electrocodeogram.codereplay.eventIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.electrocodeogram.codereplay.dataProvider.DataProvider;
import org.electrocodeogram.codereplay.dataProvider.ReplayElement;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.views.AbstractView;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;


/**
 * Implementation of AbstractReader that is able to read files that contain 'exactcodechange' events
 * provided by the 'ecg_eclipsesensor'. All other events are ignored.
 * 
 * @author marco kranz
 */
public class EventReader extends AbstractReader{

	private String name = "EventReader";
	// xml document
	private Document doc = null;
	// event timestamp
	private Date timestamp;
	
	private static EventReader reader = null;

	int counter;
	
	private EventReader(){
	}
	
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.codereplay.eventIO.AbstractReader#getInstance()
	 */
	public static AbstractReader getInstance(){
		if(reader == null)
			reader = new EventReader();
		return reader;
	} 
	
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.codereplay.eventIO.AbstractReader#getName()
	 */
	public String getName(){
		return name;
	}
	

	/* (non-Javadoc)
	 * @see org.electrocodeogram.codereplay.eventIO.AbstractReader#openFile(java.io.File)
	 */
	public void openFile(File file) {
		System.out.println("enter openfile: "+DateFormat.getInstance().format(Calendar.getInstance().getTime()));
		String line = null;
		counter = 0;
		BufferedReader reader = null;
		try{
		reader = new BufferedReader(new FileReader(file));
		line = reader.readLine();
		}catch(Exception e){
			e.printStackTrace();
		}
		String element = "";
		String[] parts;
		String timestampstring;
		boolean elementComplete = false;
		
		while(line != null){
			element = "";
			if(line.contains(";msdt.exactcodechange.xsd;")){
				elementComplete = false;
				//System.out.println("\nline.contains(msdt.exactcodechange.xsd)\n");
				//System.out.println("----> "+line);
				
				while(!elementComplete){
					while(!line.contains("</exactCodeChange>")){
						element = element + line+"\n";		// line breaks get lost on the way
						try {
							line = reader.readLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					while(line.contains("</exactCodeChange>")){
						//System.out.println("element: " + element);
						int index = line.indexOf("</exactCodeChange>");
						element = element + line.substring(0, index+18);
						//System.out.println("element + line.substring(0, index+18): " + element);
						line = line.substring(index+18);
						//System.out.println("rest of line: " + line);
						if(!isInCDATA(element)){
							element = element + line + "\n";
							elementComplete = true;
						}
						//else
							//element = element + line.substring(index);
					}
				}
				
				/*do{
					if(line.contains("<![CDATA"))
						cdata = true;
					if(line.contains("]]>"))
						cdata = false;
					if(!cdata)
						if(line.lastIndexOf("]]>") < line.lastIndexOf("</exactCodeChange>")){
							elementComplete = true;
							element = element + line;
							break;
						}
					element = element + line+"\n";		// line breaks get lost on the way
					try {
						line = reader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}while(!elementComplete);*/
					
					
					
					/*if(line.contains("<![CDATA")){
						while(!line.contains("]]>")){
							try {
								line = reader.readLine();
							} catch (IOException e) {
								e.printStackTrace();
							}
							element = element + line+"\n";		// line breaks get lost on the way
						}
					}*/
					
					//System.out.println("while...!line.contains(</exactCodeChange>)");
					//System.out.println("-> "+line);
				//System.out.println("\n<------- complete element ---------->\n"+element);
				//}
				try{
					//first, extract timestamp...
					//element = element + line;
					if(element.startsWith("<?xml version=\"1.0\"?>")){
						System.out.println("--------!!! ERROR !!!----------");
						return;
					}
					parts = element.split("#", 3);
					timestampstring = parts[0];
					// ...from the rest, extract the xml part...
					element = parts[2];
					//System.out.println("parts[2]-> "+parts[2]);
					element = element.substring(element.indexOf(";<")+1);
					//System.out.println("element-> "+element);
					// ...and create a xml document
					InputSource source = new InputSource();
					source.setCharacterStream(new StringReader(element));
					DocumentBuilder builder = null;
					builder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
					// the document
					try{
					doc = builder.parse(source);
					}catch(SAXParseException e){e.printStackTrace();}
					// the timestamp
			           timestamp = new SimpleDateFormat("EE dd.MM.yyyy HH:mm:ss z").parse(timestampstring);
			           //System.out.println("timestamp: "+timestamp);
			           parseContent(timestamp, doc);
				}catch (ParserConfigurationException e) {
					System.out.println(e);
					//throw new ParserConfigurationException(e.getMessage());
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}	// end of if
			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("noOfElements: "+counter+" "+DateFormat.getInstance().format(Calendar.getInstance().getTime()));
	}
	
	
	private boolean isInCDATA(String element) {
		if(element.lastIndexOf("]]>") < element.lastIndexOf("<![CDATA"))
			return true;
		else return false;
	}


	private void parseContent(Date timestamp, Document doc){
		counter++;
		// first, check wether this is a code change or an identifier change
		NodeList list = doc.getElementsByTagName("typeOfChange");
		
		if(("IDENTIFIER_CHANGED").equals(list.item(0).getTextContent())){
			// if its an identifier change, change the mapping in the mapping table
			list = doc.getElementsByTagName("identifier");
			String oldID = list.item(0).getTextContent();
			list = doc.getElementsByTagName("codeOrIdentifier");
			String newID = list.item(0).getTextContent();
			DataProvider.getInstance().changeIdentifier(oldID, newID);
		}
		else{ 
			// otherwise extract the needed data from the xml document... 
			// get path
			list = doc.getElementsByTagName("path");
			String[] path = new String[list.getLength()];
			for(int i=0; i < list.getLength(); i++){
				path[i] = list.item(i).getTextContent();
			}
			// get type of change
			list = doc.getElementsByTagName("typeOfChange");
			String change = list.item(0).getTextContent();
			// get element name
			list = doc.getElementsByTagName("elementName");
			String name = list.item(0).getTextContent();
			name.replace("]]&gt;","]]>");
			// get source
			list = doc.getElementsByTagName("codeOrIdentifier");
			String code = list.item(0).getTextContent();
			code.replace("]]&gt;","]]>");
//			 get identifier
			list = doc.getElementsByTagName("identifier");
			String identifier = list.item(0).getTextContent();
			identifier.replace("]]&gt;","]]>");
			ReplayElement repelem;
			repelem = new ReplayElement(timestamp, path, change, name, identifier, code);
			DataProvider.getInstance().insertReplayElement(repelem);
		}	
	}
}
