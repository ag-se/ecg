package org.electrocodeogram.msdt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hackystat.kernel.sdt.SensorDataTypeException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class MsdtManager {

	private Map<String,MicroSensorDataType> msdtMap = new HashMap<String,MicroSensorDataType>();

	private Logger logger = null;

	

	public MsdtManager() throws FileNotFoundException {

		this.logger = Logger.getLogger("MstdManager");

		loadMstdDefsFromResource();
	
	}

	private void loadMstdDefsFromResource() throws FileNotFoundException
	{

		String stdDefsDirPath = "msdt";

		File stdDefsDir = new File(stdDefsDirPath);
		
		if(!stdDefsDir.exists() || !stdDefsDir.isDirectory())
		{
			throw new FileNotFoundException("Das Verzeichnis \"msdt\" exisitiert nicht oder ist kein Verzeichnis");
		}

		String[] defs = stdDefsDir.list();

		if (defs != null)
		{
			for (int i = 0; i < defs.length; i++)
			{
				File defFile = new File(stdDefsDirPath + File.separator + defs[i]);

				FileInputStream fis = new FileInputStream(defFile);

				this.logger.log(Level.INFO,"Lese mSDT Definition " + defFile.getName());
				
				processSdtStream(fis);
			}
		}
		else
		{
			this.logger.log(Level.INFO,"Es wurden keine mSDT Definitionen gefunden.");
		}
		
	}

	private void processSdtStream(InputStream sdtStream) {
		try {
			SAXBuilder builder = new SAXBuilder();

			Document doc = builder.build(sdtStream);

			Element microSensorDataTypes = doc.getRootElement();
			
			List msdtList = microSensorDataTypes.getChildren("microsensordatatype");
			
			for (Iterator i = msdtList.iterator(); i.hasNext();) {
				
				Element msdtElement = (Element) i.next();
				
				String msdtName = msdtElement.getAttributeValue("name");
				
				if (msdtName == null) {
					throw new SensorDataTypeException("mSDT Definitionsfehler - Es wurde kein oder ein doppelter \"name\" gefunden " + msdtName);
				}

				String enabled = msdtElement.getAttributeValue("enabled");
				
				if ((enabled != null) && ("false".equals(enabled))) {
					
					this.logger.log(Level.INFO,"Der mSDT ist deaktiviert: " + msdtName);
					
					return;
				}
				
				// Process all of the attributes and return the new SDT if successful.
				MicroSensorDataType msdt = processAttributes(msdtElement, msdtName);
				
				// Process the EntryAttribute specifications.
				List entryAttributeList = msdtElement.getChildren("entryattribute");
				
				for (Iterator j = entryAttributeList.iterator(); j.hasNext();) {
					
					Element entryElement = (Element) j.next();
					
					processEntryAttribute(msdt, entryElement);
				}
				
				this.msdtMap.put(msdtName, msdt);
				
				this.logger.log(Level.INFO,"Der mSDT ist registriert worden:     " + msdt.getName());
			}
		} catch (Exception e) {
			this.logger.log(Level.SEVERE,"Wärend des Lesens des mSDT kam es zu einem Fehler. " + sdtStream + ":\n" + e);
		}
	}
	
	private MicroSensorDataType processAttributes(Element msdtElement, String msdtName) throws Exception {
		
	    // Get docstring, error if attribute missing.
	    String docstring = msdtElement.getAttributeValue("docstring");
	    
	    if (docstring == null) {
	      throw new SensorDataTypeException("mSDT Definitionsfehler - Es wurde kein oder ein doppelter \"docstring\" gefunden " + msdtName);
	    }
	    
	    // DELETED VERSIONING INFO FROM SDT DEFINITION.
	    // Get version, error if attribute missing.
	    // String version = sdtElement.getAttributeValue("version");
	    // if (version == null) {
	    //   throw new SensorDataTypeException("Command definition error: missing version.");
	    // }
	    // Get contact, error if attribute missing.
	    
	    String contact = msdtElement.getAttributeValue("contact");
	    if (contact == null) {
	      throw new SensorDataTypeException("mSDT Definitionsfehler - Es wurde kein oder ein doppelter \"contact\" gefunden " + msdtName);
	    }
	    
	    return new MicroSensorDataType(msdtName, docstring, contact);
	  }
	
	 private boolean isClass(String className) {
		    try {
		      Class.forName(className);
		      return true;
		    }
		    catch (ClassNotFoundException e) {
		      return false;
		    }
		  }
	
	 private void processEntryAttribute(MicroSensorDataType msdt, Element entry) throws Exception {
		 
		    // Get and verify legal attribute name.
		    String attributeName = entry.getAttributeValue("name");
		    
		    if (attributeName == null) {
		      throw new SensorDataTypeException("mSDT Attribut-Definitionsfehler - Es wurde kein oder ein doppelter \"name\" gefunden ");
		    }
		    
		    // Get and verify legal attribute type if supplied.
		    String typeName = entry.getAttributeValue("type");
		    
		    if ((typeName != null) && !isClass(typeName)) {
		      throw new SensorDataTypeException("mSDT Attribut-Definitionsfehler - Der Typ des Attributs ist ungültig " + typeName);
		    }
		    
		    
		    msdt.addAttribute(attributeName, typeName);
		    
		  }

	public MicroSensorDataType getMicroSensorDataType(String sdtName) throws MicroSensorDataTypeNotFoundException {
	
		if(!this.msdtMap.containsKey(sdtName))
		{
			throw new MicroSensorDataTypeNotFoundException();
		}
		
		return this.msdtMap.get(sdtName);
			
	}
}
