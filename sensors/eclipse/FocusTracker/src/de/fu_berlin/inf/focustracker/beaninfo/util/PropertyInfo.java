/*
 * Created on 30.12.2004
 *
 */
package de.fu_berlin.inf.focustracker.beaninfo.util;

/**
 * @author wenzlaff
 */
public class PropertyInfo {

	private String property;
	private String displayName;
	private Class editorClass;
	private boolean readOnly;
	
	/**
	 * @param aProperty
	 * @param aDisplayName
	 * @param aEditorClass
	 */
	public PropertyInfo(String aProperty, String aDisplayName,
			Class aEditorClass) {
		property = aProperty;
		displayName = aDisplayName;
		editorClass = aEditorClass;
	}
	
	/**
	 * @param aProperty
	 * @param aDisplayName
	 * @param aEditorClass
	 */
	public PropertyInfo(String aProperty, String aDisplayName,
			Class aEditorClass, boolean aIsReadOnly) {
		property = aProperty;
		displayName = aDisplayName;
		editorClass = aEditorClass;
		readOnly = aIsReadOnly;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public Class getEditorClass() {
		return editorClass;
	}
	public String getProperty() {
		return property;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
}
