package org.electrocodeogram.sensor.eclipse.listener;

import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class ECGElementChangedListener implements IElementChangedListener, IPartListener{


    	// contains the last known source code of all methods
	    private Hashtable methodtable = new Hashtable(1000);
	    // contains filenames of all opened files
	    private HashSet allfiles = new HashSet(1000);
	    
	    private ECGEclipseSensor sensor;
	    
	    
	    public ECGElementChangedListener(){
	    	sensor = ECGEclipseSensor.getInstance();
	    }
	    
	    // this constructor is used when there is an open file in the editor at startup
	    public ECGElementChangedListener(IEditorPart editorpart){
	    	sensor = ECGEclipseSensor.getInstance();
	    	updateMethodTable(editorpart);
	    }
	    
	    // --- IPartListener implementation ---
	    // in order to get the code of all methods before they can
	    // be modified, we get the code of every method of a file 
	    // when the file is activated(opened) in the editor for the first time.
		public void partActivated(IWorkbenchPart part){updateMethodTable(part);}
		public void partBroughtToTop(IWorkbenchPart part){updateMethodTable(part);}
		public void partClosed(IWorkbenchPart part){}
		public void partDeactivated(IWorkbenchPart part){}
		public void partOpened(IWorkbenchPart part){updateMethodTable(part);}
		
		// update the 'methodtable'
		private synchronized void updateMethodTable(IWorkbenchPart page){
			//System.out.println(page);
			if(page instanceof IEditorPart){
				IEditorPart editor = (IEditorPart)page;
				IEditorInput input = editor.getEditorInput();
				IFile file = null;
				if(input instanceof IFileEditorInput)
					file = ((IFileEditorInput)input).getFile();
				else return;
				
//				if file opened for the first time...
				if(!allfiles.contains(file.getName())){
					
					String methodhandle = null;	
					IType[] types = null;
					IMethod[] methods = null;
					
					IJavaElement element = JavaCore.create(file);
					ICompilationUnit compunit = null;
					// should be a compunit normally...
					if(element instanceof ICompilationUnit){
						
						compunit = (ICompilationUnit)element;
						
						try{
							types = compunit.getAllTypes();
					
							// iterate over all types(classes in this case)
							for(int i = 0; i < types.length ; i++){
								
								methods = types[i].getMethods();
								// iterate over all methods
								for(int j=0 ;j < methods.length ; j++){
									methodhandle = methods[j].getHandleIdentifier();
									// if the method is not yet in the methodtable,
									// add it to known methods
									if(!methodtable.containsKey(methodhandle))
										methodtable.put(methodhandle, methods[j].getSource());
										// ... and send an event.
										sensor.processActivity("msdt.exactcodechange.xsd",
                                            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
				    	                    + sensor.getUsername()
				    	                    + "</username><projectname>"
				    	                    + sensor.getProjectname()
				    	                    + "</projectname></commonData><exactCodeChange>"+"<path>"
				    	                    + methods[j].getAncestor(IType.TYPE).getElementName()+"</path>"
				    						+ "<change><typeOfChange>ADDED</typeOfChange>"
				    						+ "<elementName><![CDATA["+replace(methods[j].getElementName())+"]]></elementName>"
				    						+ "<identifier><![CDATA["+replace(methodhandle)+ "]]></identifier>"
				    						+ "<codeOrIdentifier><![CDATA["+replace(methods[j].getSource())+ "]]>"
				    						+ "</codeOrIdentifier></change>"
				    	                    + "</exactCodeChange></microActivity>");
								}
							}
						}catch(JavaModelException e){}
					} // end of if
					// add file to known files
					allfiles.add(file.getName());
				} // end of if
			}// end of if
		} // end of initialize
		

		// called whenever a change to the java model occures(only for POST_RECONCILE, see registration)
		public void elementChanged(ElementChangedEvent event) {
			
			 // we are only interested in changes to compilation units(equals .java files), because they
	    	 // indicate a source code change.
	         if(event.getDelta().getElement().getElementType() == IJavaElement.COMPILATION_UNIT){
	        	 //System.out.println("event.getdelta.getelement is a compilation unit.");
	        	 
	        	 // if there are no children, this indicates that there was a change to a method body.
	        	 // since the ElementChangedEvent in this case only keeps track of the changes to the 
	        	 // java model, it does NOT provide any information about what exactly was changed in the source code. :(
	        	 // NOTE: the get..children() methods return an empty array(.length == 0) instead of 'null'
	        	 // when there are no children!
	        	 if((event.getDelta().getChangedChildren().length + event.getDelta().getAddedChildren().length +
	        			 event.getDelta().getAffectedChildren().length + event.getDelta().getRemovedChildren().length == 0) 
	        			 && (event.getDelta().getFlags() == IJavaElementDelta.F_FINE_GRAINED + IJavaElementDelta.F_CONTENT)){
	        		 //System.out.println("event.getdelta.getflags is : F_CONTENT + F_FINE_GRAINED");
	        		 //	try to get all methods of all classes retrieved from the compilation unit
	        		 ICompilationUnit compunit = (ICompilationUnit)event.getDelta().getElement();
	        		 findModifiedMethod(compunit);
	        	 }
	        	 // if there is at least one modified child, there was a change to the java model
	        	 // (e.g. field/method has been removed/added)
	        	 else findModifiedChildren(event.getDelta());	
	         }
		}
		
		// in case of a method body change:
		// compare the old and new source code of all methods of one compilation unit(file)
		// in order to find the method that has changed
		private void findModifiedMethod(ICompilationUnit compunit) {
	    	IMethod[] methods = null;
	    	String methodhandle = null;
	    	String ecg_event = "";
	    	IType[] types = null;
	    	try{
	    		types = compunit.getAllTypes();
	    	}catch(Exception e){}
	    	
	        try{
	        	// iterate over all types(classes in this case)
	        	for(int i = 0; i < types.length ; i++){
	        		//System.out.println("----- type no. "+i+" ----");
		        	 
	        		methods = types[i].getMethods();
	        		//System.out.println("type ["+i+"] is class: "+ types[i].getElementName());
	        		// iterate over all methods
		        	for(int j=0 ;j < methods.length ; j++){
		        		 
		        		// the handle indentifier is unique for every method
		        		// even if there are two signature-identical methods(in case of a mistake on programmers side)
		        		methodhandle = methods[j].getHandleIdentifier();
		        		
		        		// test if method was changed(compare complete source code with old version)
		        		String oldmethod = (String)methodtable.get(methodhandle);
		        		// if the changed method is located, switch old and new code and send an event.   
		        		if(!oldmethod.equals(methods[j].getSource())){
		        			methodtable.remove(methodhandle);
		        			methodtable.put(methodhandle,methods[j].getSource());
		        			//System.out.println("method ["+j+"] has been changed: "+ methods[j].getHandleIdentifier());
		        			sensor.processActivity("msdt.exactcodechange.xsd",
                                    "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
		        	                + sensor.getUsername()
		        	                + "</username><projectname>"
		        	                + sensor.getProjectname()
		        	                + "</projectname></commonData><exactCodeChange>"+"<path>"
		        	                + methods[j].getAncestor(IType.TYPE).getElementName()+"</path>"
		        					+ "<change><typeOfChange>CHANGED</typeOfChange>"
		        					+ "<elementName><![CDATA["+replace(methods[j].getElementName())+"]]></elementName>"
		        					+ "<identifier><![CDATA["+replace(methodhandle)+ "]]>"
		        					+ "</identifier><codeOrIdentifier><![CDATA["+replace(methods[j].getSource())+ "]]>"
		        					+ "</codeOrIdentifier></change>"
		        	                + "</exactCodeChange></microActivity>");
		        		}
		        	}
		        }
	        }catch(JavaModelException e){}
		} // end of findModifiedMethod
		
		
		// in case of a signature change(e.g. public void foo() became private void foo(int i))
		// or a removed or added method, this method is called.
		private void findModifiedChildren(IJavaElementDelta delta) {
			//System.out.println("--- start findModifiedChildren ---");
			IJavaElementDelta children[] = delta.getAffectedChildren();
			// temporarily holds event notifications for all changes until verification
			// needed to ensure 'change' events in case of declaration change instead of 'add' + 'delete' events
			Hashtable all_events = new Hashtable(50);
			Enumeration keys = null;
			String ecg_event = "";
			// 'delta.getelement' is a compilation unit(check above)
			// therefor the fields, methods etc. are two 'levels' below
			for(int j = 0; j < children.length; j++){
				IJavaElementDelta children_2[] = children[j].getAffectedChildren();
				for(int i=0; i<children_2.length; i++ ){
					
					int elementtype = children_2[i].getElement().getElementType();
					
					if(elementtype == IJavaElement.METHOD){
						
		    		 	IMethod method = (IMethod)children_2[i].getElement();
		    		 	
		    		 	if(children_2[i].getKind() == IJavaElementDelta.ADDED){ // if method was added
		    		 		try{
		    		 		ecg_event =                                                                 "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
		                        + sensor.getUsername()
		                        + "</username><projectname>"
		                        + sensor.getProjectname()
		                        + "</projectname></commonData><exactCodeChange>"+"<path>"
		                        + method.getAncestor(IType.TYPE).getElementName()+"</path>"
		    					+ "<change><typeOfChange>ADDED</typeOfChange>"
		    					+ "<elementName><![CDATA["+replace(method.getElementName())+"]]></elementName>"
		    					+ "<identifier><![CDATA["+replace(method.getHandleIdentifier())+ "]]>"
		    					+ "</identifier><codeOrIdentifier><![CDATA["+replace(method.getSource())+ "]]>"
		    					+ "</codeOrIdentifier></change>"
		                        + "</exactCodeChange></microActivity>";
		    		 		// add to methods
		    		 		methodtable.put(method.getHandleIdentifier(), method.getSource());
		    		 		// add event to event queue
		    		 		all_events.put(method.getHandleIdentifier(), ecg_event);
		    		 		//System.out.println("the following method has been ADDED: "+method.getHandleIdentifier());
		    		 		}catch(Exception e){}
		    		 	}
		    		 	else if(children_2[i].getKind() == IJavaElementDelta.REMOVED){ // if method was removed or signature changed(name or no. of args)
					    	boolean changed = false;
					    	keys = all_events.keys();
					    	String identifier = "";
					    	while(keys.hasMoreElements()){
					    		// method body of the method to remove
					    		String old_method = (String)methodtable.get(method.getHandleIdentifier());
					    		// sub is the method body of the removed method
					    		String sub = old_method.substring(old_method.indexOf("{"));
					    		// the identifier of one of the added methods
					    		identifier = (String)keys.nextElement();
					    		// if there was a method added before with the same method body as the one to remove,
					    		// this indicates that this methods declaration changed in some way(either name or number of args)
					    		// therefor we have to compare the body of all added methods to the body of the removed method.
					    		// (ADDED methods are allways reported first, otherwise this would not work)
					    		if(((String)methodtable.get(identifier)).endsWith(sub)){
					    			// we need the new name of the method(in case it changed),
					    			// so we must recreate the IMethod
					    			String new_elementname = method.getElementName();
					    			IMethod clone = (IMethod)JavaCore.create(identifier);
					    			if(clone != null)
					    				new_elementname = clone.getElementName();
					    			
					    			// first, the identifier change
					    			ecg_event =                                                                                 "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
				                        + sensor.getUsername()
				                        + "</username><projectname>"
				                        + sensor.getProjectname()
				                        + "</projectname></commonData><exactCodeChange>"+"<path>"
				                        + method.getAncestor(IType.TYPE).getElementName()+"</path>"
				    					+ "<change><typeOfChange>IDENTIFIER_CHANGED</typeOfChange>"
				    					+ "<elementName><![CDATA["+replace(method.getElementName())+"]]></elementName>"
				    					+ "<identifier><![CDATA["+replace(method.getHandleIdentifier())+ "]]>"
				    					+ "</identifier><codeOrIdentifier><![CDATA["+replace(identifier)+ "]]>"
				    					+ "</codeOrIdentifier></change>"
				                        + "</exactCodeChange></microActivity>";
					    			// second, actual code change
					    			ecg_event +=                                                                                 "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
				                        + sensor.getUsername()
				                        + "</username><projectname>"
				                        + sensor.getProjectname()
				                        + "</projectname></commonData><exactCodeChange>"+"<path>"
				                        + method.getAncestor(IType.TYPE).getElementName()+"</path>"
				    					+ "<change><typeOfChange>CHANGED</typeOfChange>"
				    					+ "<elementName><![CDATA["+replace(new_elementname)+"]]></elementName>"
				    					+ "<identifier><![CDATA["+replace(identifier)+ "]]>"
				    					+ "</identifier><codeOrIdentifier><![CDATA["+replace((String)methodtable.get(identifier))+ "]]>"
				    					+ "</codeOrIdentifier></change>"
				                        + "</exactCodeChange></microActivity>";
					    			// remove the 'method added' ecg_event from the list of events to be sent... 
					    			all_events.remove(identifier);
					    			// ... and add 'identifier changed' events.
					    			all_events.put(method.getHandleIdentifier(), ecg_event);
					    			//System.out.println("method "+method.getHandleIdentifier()+" has been changed to "+identifier+".");
					    			methodtable.remove(method.getHandleIdentifier());
					    			changed = true;
					    			break;
					    		}
					    	}
					    	if(!changed){ // if the method was not changed but removed...
					    		methodtable.remove(method.getHandleIdentifier());
					    		ecg_event =                                                                         "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
			                        + sensor.getUsername()
			                        + "</username><projectname>"
			                        + sensor.getProjectname()
			                        + "</projectname></commonData><exactCodeChange>"+"<path>"
			                        + method.getAncestor(IType.TYPE).getElementName()+"</path>"
			    					+ "<change><typeOfChange>DELETED</typeOfChange>"
			    					+ "<elementName><![CDATA["+replace(method.getElementName())+"]]></elementName>"
			    					+ "<identifier><![CDATA["+replace(method.getHandleIdentifier())+ "]]>"
			    					+ "</identifier><codeOrIdentifier>"
				    				+ "</codeOrIdentifier></change>"
			                        + "</exactCodeChange></microActivity>";
					    		all_events.put(method.getHandleIdentifier(), ecg_event);
					    		//System.out.println("the following method has been REMOVED: "+method.getHandleIdentifier());
					    		changed = false;
					    	}
					    	
					    }
		    		 	else if(children_2[i].getKind() == IJavaElementDelta.CHANGED){ // if method signature has changed(all other sig. changes)
					    	methodtable.remove(method.getHandleIdentifier());
					    	try{
					    	methodtable.put(method.getHandleIdentifier(), method.getSource());
					    	ecg_event =                                                                 "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
		                        + sensor.getUsername()
		                        + "</username><projectname>"
		                        + sensor.getProjectname()
		                        + "</projectname></commonData><exactCodeChange>"+"<path>"
		                        + method.getAncestor(IType.TYPE).getElementName()+"</path>"
		    					+ "<change><typeOfChange>CHANGED</typeOfChange>"
		    					+ "<elementName><![CDATA["+replace(method.getElementName())+"]]></elementName>"
		    					+ "<identifier><![CDATA["+replace(method.getHandleIdentifier())+ "]]>"
		    					+ "</identifier><codeOrIdentifier><![CDATA["+replace(method.getSource())+ "]]>"
		    					+ "</codeOrIdentifier></change>"
		                        + "</exactCodeChange></microActivity>";
					    	all_events.put(method.getHandleIdentifier(), ecg_event);
					    	//System.out.println("the following method has been CHANGED: "+method.getHandleIdentifier());
					    	}catch(Exception e){}
					    } 
					}	
				}
			}
			keys = all_events.keys();
			String nextelement = null;
			// send the events
			while(keys.hasMoreElements()){
				nextelement = (String)all_events.get(keys.nextElement());
				//System.out.println(nextelement);
				sensor.processActivity("msdt.exactcodechange.xsd",
						nextelement);
			}
		} // end of findModifiedChildren


		private String replace(String element) {
			return element.replace("]]>", "]]]]><![CDATA[>");
		}
}
