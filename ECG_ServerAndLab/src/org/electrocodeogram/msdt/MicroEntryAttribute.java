package org.electrocodeogram.msdt;

import org.hackystat.kernel.sdt.SensorDataTypeException;

public class MicroEntryAttribute {

	private int index;

	private String name;

	private String typeName;

	public MicroEntryAttribute(int index, String name, String typeName)
	{

		this.index = index;

		this.name = name;

		this.typeName = typeName;

	}

}
