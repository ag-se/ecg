package org.hackystat.stdext.sensor.eclipse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

import org.eclipse.jdt.core.dom.AST;

/**
 * Listens to the java element change events to get incremental work on java objects and collect 
 * refactoring informaiton for test-driven development analysis purpose. It's declared as package
 * private so that it can only be instantiated by Eclise sensor.
 * 
 * @author Hongbing Kou
 * @version $Id: JavaStructureChangeDetector.java,v 1.3 2004/11/19 02:15:14 hongbing Exp $
 */
class JavaStructureChangeDetector implements IElementChangedListener {
  /** Eclipse sensor which is used to send out hackystat data. */
  private EclipseSensor sensor;

  /**
   * Instantiates the JavaStructureDetector instance with Eclipse sensor.
   * 
   * @param sensor Eclipse sensor.
   */
  JavaStructureChangeDetector(EclipseSensor sensor) {
    this.sensor = sensor;
  }

  /**
   * Implements the element change response.
   * 
   * @param event Element change event.
   */
  public void elementChanged(ElementChangedEvent event) {
    //IJavaElementDelta jed = event.getDelta().getAffectedChildren()[0];
    IJavaElementDelta[] childrenChanges = event.getDelta().getAffectedChildren(); 
    
    if (childrenChanges != null && childrenChanges.length > 0) {
      javaObjectChange(childrenChanges[0]);      
    }

  }
 
  
  /**
   * Process the editng on java element changes.
   * 
   * @param jed Java element delta change.
   */
  private void javaObjectChange(IJavaElementDelta jed) {
    List additions = new ArrayList();
    List deletions = new ArrayList();
    
    // Traverse the delta change tree for refactoring activity
    traverse(jed, additions, deletions);
    
    //  Gets the location of java file.
    IPath javaFile = jed.getElement().getResource().getLocation();
        
    // No java structure change
    if (additions.isEmpty() && deletions.isEmpty()) {
      return;      
    }
    // Addition, deletion, renaming activity.
    else if (additions.size() == 1 || deletions.size() == 1) {
      if (deletions.size() == 0) {
        process(javaFile, "Add", (IJavaElementDelta) additions.get(0));
      }
      else if (additions.size() == 0) {
        process(javaFile, "Remove", (IJavaElementDelta) deletions.get(0));
      }
      else if (deletions.size() == 1) {
        IJavaElementDelta fromDelta = (IJavaElementDelta) deletions.get(0);
        IJavaElementDelta toDelta = (IJavaElementDelta) additions.get(0);
        if (fromDelta.getElement().getParent().equals(toDelta.getElement().getParent())) { 
          process(javaFile, "Rename", fromDelta, toDelta); 
        }
        else {
          javaFile = fromDelta.getElement().getResource().getLocation();
          process(javaFile, "Move", fromDelta.getElement().getParent(), 
              toDelta.getElement().getParent());
        }
      }
    }    
    // Massive addition by copying
    else if (additions.size() > 1) {
      for (Iterator i = additions.iterator(); i.hasNext();) {
        process(javaFile, "Add", (IJavaElementDelta) i.next());
      }
    }
    // Massive block deletion
    else if (deletions.size() > 1) {
      for (Iterator i = deletions.iterator(); i.hasNext();) {
        process(javaFile, "Remove", (IJavaElementDelta) i.next());
      }
    }    
  }

  /**
   * Constructs and send off the java element change data.
   * 
   * @param javaFile Associated file.
   * @param op Operation
   * @param delta Delta change element
   */
  private void process(IPath javaFile, String op, IJavaElementDelta delta) {
    IJavaElement element = delta.getElement();
    String typeName = retrieveType(element);
    
    if ("Class".equals(typeName)) {
      javaFile = element.getResource().getLocation();
    }  
    
    // Only deal with java file.
    if (!"java".equals(javaFile.getFileExtension())) {
      return;
    }
    
    String name = retrieveName(element);
    
    if (name != null && !"".equals(name)) {
      StringBuffer javaEditData = new StringBuffer();
      javaEditData.append(javaFile + "#" + op + "#" + typeName);
      javaEditData.append("#" + name);
    }    
  }


  /**
   * Constructs and send of the java element change data.
   * 
   * @param javaFile Associated file.
   * @param op Operation
   * @param fromDelta Change from delta.
   * @param toDelta Change to delta.
   */
  private void process(IPath javaFile, String op, 
                       IJavaElementDelta fromDelta, IJavaElementDelta toDelta) {
    StringBuffer javaEditData = new StringBuffer();
    String typeName = retrieveType(toDelta.getElement());
    
    if ("Class".equals(typeName)) {
      javaFile = fromDelta.getElement().getResource().getLocation();
    }

    // Only deal with java file.
    if (!"java".equals(javaFile.getFileExtension())) {
      return;
    }
    
    String fromName = retrieveName(fromDelta.getElement());
    String toName = retrieveName(toDelta.getElement());
    
    if (fromName != null && !"".equals(fromName) && toName != null && !"".equals(toName)) {
      javaEditData.append(javaFile + "#" + op + "#" + typeName);
      javaEditData.append("#" + fromName);
      javaEditData.append("#" + toName);
      this.sensor.processActivity("Java Edit",  javaEditData.toString());
    }
  }
  
  /**
   * Constructs and send of the java element change data.
   * 
   * @param javaFile Associated file.
   * @param op Operation.
   * @param from Change from element.
   * @param to Change to element.
   */
  private void process(IPath javaFile, String op, IJavaElement from, IJavaElement to) {
    String typeName = retrieveType(to);
    
    // Only deal with java file.
    if (!"java".equals(javaFile.getFileExtension())) {
      return;
    }

    String fromName = retrieveName(from);
    String toName = retrieveName(to);
    
    if (fromName != null && !"".equals(fromName) && toName != null && !"".equals(toName)) {
      StringBuffer javaEditData = new StringBuffer();
      javaEditData.append(javaFile + "#" + op + "#" + typeName);
      javaEditData.append("#" + fromName);
      javaEditData.append("#" + retrieveName(to));

      this.sensor.processActivity("Java Edit",  javaEditData.toString());    
    }    
  }
  
  /**
   * Gets the element type .
   * 
   * @param element Java element object
   * @return Element type string (class, method, field or import).
   */
  private String retrieveType(IJavaElement element) {
    String type = "Unknown";
    if (element.getElementType() == IJavaElement.FIELD) {
      type = "Field";
    }
    else if (element.getElementType() == IJavaElement.METHOD) {
      type = "Method";
    }
    else if (element.getElementType() == IJavaElement.IMPORT_DECLARATION ||
             element.getElementType() == IJavaElement.IMPORT_CONTAINER) {
      type = "Import";
    }
    else {
      type = "Class";
    }
    
    return type;
  }
  
  /**
   * Gets the element name with signature.
   * 
   * @param element Java element, which could be class, method, field or import.
   * @return Brief element name.
   */
  private String retrieveName(IJavaElement element) {
    String name = element.toString();
    name = name.substring(0, name.indexOf('['));
    // Trim off the meaningless "(not open)" string
    int pos = name.indexOf("(not open)");
    if (pos > 0) {
      name = name.substring(0, pos);
    }
    
    // take off the '#' if it exists
    name = name.replace('#', '/');
    
    return name.trim(); 
  }
  
  /**
   * Traverses the delta change tree on java element to look for addition and deletion on
   * java element.
   * 
   * @param delta Delta element change.
   * @param additions Added element holder.
   * @param deletions Deleted element holder.
   */
  private void traverse(IJavaElementDelta delta, List additions, List deletions) {
    IJavaElement element = delta.getElement();
    
    // Saves the addition and deletion.
    if (delta.getKind() == IJavaElementDelta.ADDED) {
       additions.add(delta);
    }
    else if (delta.getKind() == IJavaElementDelta.REMOVED) {
      deletions.add(delta);
    }
    
    // Recursively look for changes on children elements.
    IJavaElementDelta[] children = delta.getAffectedChildren();
    for (int i = 0; i < children.length; i++) {
      traverse(children[i], additions, deletions);
    }
  }
}