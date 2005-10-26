package org.electrocodeogram.module.source;

import java.util.StringTokenizer;

public class ManualAnnotatorEvents
{
	
	private String[] _events;
	
	public ManualAnnotatorEvents(String value)
	{
		if(value == null || value.equals(""))
		{
			return;
		}
		
		StringTokenizer stringTokenizer = new StringTokenizer(value,",");
		
		this._events = new String[stringTokenizer.countTokens()];
		
		for(int i=0;i<this._events.length;i++)
		{
			this._events[i] = stringTokenizer.nextToken();
		}
	}

	public String[] getEvents()
	{
		return this._events;
	}
}
