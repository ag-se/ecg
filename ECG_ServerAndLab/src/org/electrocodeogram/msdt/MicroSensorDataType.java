package org.electrocodeogram.msdt;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hackystat.kernel.sdt.EntryAttribute;
import org.hackystat.kernel.sdt.SensorDataType;
import org.hackystat.kernel.sdt.SensorDataTypeException;

public class MicroSensorDataType
{
	private int attributeOrderVal = 1;
	
	private List<MicroEntryAttribute> attributeList = new ArrayList<MicroEntryAttribute>();
	
	private HashMap<String, MicroEntryAttribute> attributeMap = new HashMap<String, MicroEntryAttribute>();
	
	private int typedAttributes = 0;
	
	  private String name;
	  
	  private String docstring;
	  
	  private String contact;

	public MicroSensorDataType(String name, String docstring, String contact) throws SensorDataTypeException
	{
		this.name = name;
		
		this.docstring = docstring;
		
		this.contact = contact;
	}
	
	void addAttribute(String name, String type) throws SensorDataTypeException {
		
		MicroEntryAttribute entry = new MicroEntryAttribute(this.attributeOrderVal++, name, type);
 
		this.attributeList.add(entry);
 
		this.attributeMap.put(name, entry);
 
		if (type != null)
		{
			this.typedAttributes++;
		}
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String[] getEntryAttributeNames()
	{
		return this.attributeMap.keySet().toArray(new String[0]);
	}
}
