package de.fu_berlin.inf.focustracker.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class ECGLogAnalyzer {

	private String logFileName = "C:/Dokumente und Einstellungen/wenzlaff/ecg_log/out.log";
	
	
	public static void main(String[] args) throws IOException, SAXException {
		new ECGLogAnalyzer();
	}
	
	public ECGLogAnalyzer() throws IOException, SAXException {
		analyzeLog();
	}
	
	public void analyzeLog() throws IOException, SAXException {
		File logFile = new File(logFileName);
		BufferedReader br = new BufferedReader(new FileReader(logFile));
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			if(line.indexOf("add;msdt.focus.xsd;<?xml version=\"1.0\"?>") != -1) {
				String xml = line.substring(line.indexOf("<?xml version=\"1.0\"?>"));
//				System.err.println(xml);
				DOMParser parser = new DOMParser();
				parser.parse(new InputSource(new StringReader(xml)));
				Document doc = parser.getDocument();
				String resourcename = doc.getElementsByTagName("resourcename").item(0).getTextContent();
				String elementName = doc.getElementsByTagName("element").item(0).getTextContent();
				String elementType = doc.getElementsByTagName("elementtype").item(0).getTextContent();
				String focus = doc.getElementsByTagName("hasfocus").item(0).getTextContent();
				String rating = null;
				if(doc.getElementsByTagName("rating").item(0) != null) { 
					rating = doc.getElementsByTagName("rating").item(0).getTextContent();
				}
				String timestamp = doc.getElementsByTagName("timestamp").item(0).getTextContent();
				System.out.println(timestamp + ": " + elementName + " " + rating + " (" + elementType + ")");
				
			}
		}
	}
	
}
