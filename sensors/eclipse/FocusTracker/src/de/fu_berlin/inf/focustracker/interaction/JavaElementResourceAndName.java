package de.fu_berlin.inf.focustracker.interaction;

public class JavaElementResourceAndName {
	
	private String resource = "";
	private String name = "";
	
	public JavaElementResourceAndName(String aResource, String aName) {
		super();
		resource = aResource;
		name = aName;
	}
	
	public String getName() {
		return name;
	}
	public String getResource() {
		return resource;
	}
}
