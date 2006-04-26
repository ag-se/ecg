/*
 * Created on 04.07.2004
 *
 */
package de.fu_berlin.inf.focustracker.beaninfo.util;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wenzlaff
 */
public abstract class ExtSimpleBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

    	PropertyInfo[] infos = getProperties();
    	if (infos == null) {
    		infos = computeDefaultPropertyInfos();
    	}
    	
    	PropertyDescriptor[] props = new PropertyDescriptor[infos.length];
			
    	try {
        	for (int i = 0; i < props.length; i++) {
        		if(!infos[i].isReadOnly()) {
        			props[i] = new PropertyDescriptor(infos[i].getProperty(), getClazz());
        		} else {
        			props[i] = new PropertyDescriptor(infos[i].getProperty(), getClazz(), "is" + capitalize(infos[i].getProperty()), null);
        		}
		    	props[i].setDisplayName(infos[i].getDisplayName());
		    	if(infos[i].getEditorClass() != null) {
		    		props[i].setPropertyEditorClass(infos[i].getEditorClass());
		    	}
		    	if (props[i].getName().equals("id")) {
		    		props[i].setExpert(true);
		    		props[i].setWriteMethod(null);
		    	}
	            props[i].setValue("ComparableColumnTTV", Boolean.TRUE );
        	}
    	} catch (Exception e) {
//    		log.error(e.getMessage(), e);
    		e.printStackTrace();
    		return null;
    	}
    	
    	return props;
    }

    /* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		
		return new BeanDescriptor(this.getClazz(), getCustomizerClazz());

	}
    
    public abstract PropertyInfo[] getProperties();
    public abstract Class getClazz();
    public abstract Class getCustomizerClazz();
    
    protected PropertyInfo[] computeDefaultPropertyInfos() {
    	List<PropertyInfo> infos = new ArrayList<PropertyInfo>();
    	try {
    		// create a new beanInfo !
			BeanInfo beanInfo = Introspector.getBeanInfo(getClazz(), Introspector.IGNORE_IMMEDIATE_BEANINFO);
			for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
	    		if(!propertyDescriptor.getName().equals("class")) {
	    			infos.add(new PropertyInfo(propertyDescriptor.getName(), propertyDescriptor.getName(), null));
	    		}				
			}
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return infos.toArray(new PropertyInfo[infos.size()]);
    }
    
    public static String capitalize(String name) { 
    	if (name == null || name.length() == 0) { 
    	    return name; 
            }
    	return name.substring(0, 1).toUpperCase() + name.substring(1);
        }    
}
