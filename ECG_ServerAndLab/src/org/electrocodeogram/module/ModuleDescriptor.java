/*
 * Created on 06.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.module;

import java.util.Properties;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModuleDescriptor
{

    private int id = -1;
    
    private String name = null;
    
    private Class clazz = null;
    
    private Properties properties = null;
    
    // TODO : make the prop file XML
    
    public ModuleDescriptor(int id, String name, Class clazz, Properties properties)
    {
        this.id = id;
        
        this.properties = properties;
        
        this.name = name;
        
        this.clazz = clazz;
        
    }
    
    public Class getClazz()
    {
        return clazz;
    }
    public String getName()
    {
        return name;
    }
    public Properties getProperties()
    {
        return properties;
    }

    public int getId()
    {
        return this.id;
    }
}
