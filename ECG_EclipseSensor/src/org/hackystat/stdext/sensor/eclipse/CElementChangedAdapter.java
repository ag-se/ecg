package org.hackystat.stdext.sensor.eclipse;

import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.runtime.IPath;

/**
 * @author tkeskinpala
 * @version $Id: CElementChangedAdapter.java,v 1.1 2004/10/29 02:02:23 hongbing Exp $
 */
public class CElementChangedAdapter implements IElementChangedListener {

  /** Eclipse sensor which is used to send out data */
  private EclipseSensor sensor;

  /**
   * Instantiates the ElementChangedAdapter instance with Eclipse sensor.
   * 
   * @param sensor Eclipse sensor.
   */
  CElementChangedAdapter(EclipseSensor sensor) {
    this.sensor = sensor;
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.eclipse.cdt.core.model.IElementChangedListener#
   *      elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
   */
  public void elementChanged(ElementChangedEvent event) {
    // TODO: Implement methods of IElementChangedListener of CDT
    ICElementDelta ced = event.getDelta();

    //Changes on C Element must be on children
    if ((ced.getFlags() & ICElementDelta.F_CHILDREN) == 0) {
      return;
    }

    // Gets the location of C++ file.
    IPath cppFile = ced.getElement().getResource().getLocation();
    ICElementDelta affectedDelta = ced.getAffectedChildren()[0];

    //System.out.println(affectedDelta);

    // Ignore if there is no affected children
    /*
     * if (affectedDelta.getAffectedChildren().length == 0) { return; }
     */

    //If the changes are on class includes
    if (ICElement.C_INCLUDE == affectedDelta.getElement().getElementType()) {
      // Adds or delete imports.
      if (affectedDelta.getAffectedChildren().length != 0) {
        ICElementDelta imports[] = affectedDelta.getAffectedChildren();
        for (int i = 0; i < imports.length; i++) {
          //Operation type
          String op = null;
          if (imports[i].getKind() == ICElementDelta.ADDED) {
            op = "Add";
          }
          else if (imports[i].getKind() == ICElementDelta.REMOVED) {
            op = "Remove";
          }
          else if (imports[i].getKind() == ICElementDelta.CHANGED) {
            op = "Change";
          }
          else {
            return;
          }

          String cppEditData = cppFile.toString() + "#" + op + "#"
              + trimName(imports[i].toString());
          this.sensor.processActivity("C/C++ Edit", cppEditData);
          //System.out.println(cppEditData);
        }
      }
      else if (affectedDelta.getAffectedChildren().length == 0) {
        String op = null;
        if (affectedDelta.getKind() == ICElementDelta.ADDED) {
          op = "Add";
        }
        else if (affectedDelta.getKind() == ICElementDelta.REMOVED) {
          op = "Remove";
        }
        else if (affectedDelta.getKind() == ICElementDelta.CHANGED) {
          op = "Change";
        }
        else {
          return;
        }
        String cppEditData = cppFile.toString() + "#" + op + "#"
            + trimName(affectedDelta.toString());
        this.sensor.processActivity("C/C++ Edit", cppEditData);
        //System.out.println(cppEditData);
      }
    }
    //If the changes are on C_FUNCTION
    else if (ICElement.C_FUNCTION == affectedDelta.getElement().getElementType()) {
      // Adds or delete imports.
      if (affectedDelta.getAffectedChildren().length != 0) {
        ICElementDelta imports[] = affectedDelta.getAffectedChildren();
        for (int i = 0; i < imports.length; i++) {
          //Operation type
          String op = null;
          if (imports[i].getKind() == ICElementDelta.ADDED) {
            op = "Add";
          }
          else if (imports[i].getKind() == ICElementDelta.REMOVED) {
            op = "Remove";
          }
          else if (affectedDelta.getKind() == ICElementDelta.REMOVED) {
            op = "Change";
          }
          else {
            return;
          }

          String cppEditData = cppFile.toString() + "#" + op + "#"
              + trimName(imports[i].toString());
          this.sensor.processActivity("C/C++ Edit", cppEditData);
          //System.out.println(cppEditData);
        }
      }
      else if (affectedDelta.getAffectedChildren().length == 0) {
        String op = null;
        if (affectedDelta.getKind() == ICElementDelta.ADDED) {
          op = "Add";
        }
        else if (affectedDelta.getKind() == ICElementDelta.REMOVED) {
          op = "Remove";
        }
        else if (affectedDelta.getKind() == ICElementDelta.REMOVED) {
          op = "Change";
        }
        else {
          return;
        }
        String cppEditData = cppFile.toString() + "#" + op + "#"
            + trimName(affectedDelta.toString());
        this.sensor.processActivity("C/C++ Edit", cppEditData);
        //System.out.println(cppEditData);
      }
    }
    // If the changes are on class level.
    else if (ICElement.C_PROJECT == affectedDelta.getElement().getElementType()) {
      // Move the class from one package to another.
      ICElementDelta projectDeltas = affectedDelta.getAffectedChildren()[0];
      ICElementDelta deltas[] = projectDeltas.getAffectedChildren();

      if (deltas.length != 0) {
        // Added or deleted class
        //	        if (deltas[0].getAffectedChildren().length == 1) {
        //	          ICElementDelta classDelta = deltas[0].getAffectedChildren()[0];
        //	          cppFile = classDelta.getElement().getResource().getLocation();
        cppFile = deltas[0].getElement().getResource().getLocation();

        String op = null;
        if (ICElementDelta.ADDED == deltas[0].getKind()) {
          op = "Add";
        }
        else if (ICElementDelta.CHANGED == deltas[0].getKind()) {
          op = "Change";
        }
        else if (ICElementDelta.REMOVED == deltas[0].getKind()) {
          op = "Remove";
        }
        else {
          return;
        }
        String cppEditData = null;
        String trimmed = trimName(deltas[0].toString());
        cppEditData = cppFile.toString() + "#" + op + "#Class#" + trimmed;
        this.sensor.processActivity("C/C++ Edit", cppEditData);
      }
      //	      }
    }
  }

  /**
   * Handle single add or delete refactoring operation.
   * 
   * @param fileName The associated file name.
   * @param delta Element change delta.
   */
  private void handleSingleDelta(String fileName, ICElementDelta delta) {

    // Delta change type
    String type = null;
    if (delta.getElement().getElementType() == ICElement.C_METHOD) {
      type = "Method";
    }
    else if (delta.getElement().getElementType() == ICElement.C_FIELD) {
      type = "Field";
    }

    // delta change operation.
    String op = null;
    if (delta.getKind() == ICElementDelta.ADDED) {
      op = "Add";
    }
    else if (delta.getKind() == ICElementDelta.REMOVED) {
      op = "Remove";
    }

    // Unknow refactoring operation
    if (type == null || op == null) {
      return;
    }

    String cppEditData = fileName + "#" + op + "#" + type + "#" + trimName(delta.toString());

    this.sensor.processActivity("C++ Edit", cppEditData);
    //System.out.println(cppEditData);
  }

  /*************************************************************************************************
   * Trims a long and complex name
   * 
   * @param complexName File Name to be trimmed
   * @return nameNoTail Trimmed name
   */
  private String trimName(String complexName) {
    if (complexName == null) {
      return null;
    }

    // If it start with [
    if (complexName.startsWith("[")) {
      complexName = complexName.substring(complexName.indexOf(']') + 2);
    }

    String nameNoTail = complexName.substring(0, complexName.indexOf('['));

    // Cut off the "(not open)" string.
    int pos = nameNoTail.indexOf("(not open)");
    if (pos > 0) {
      nameNoTail = nameNoTail.substring(0, pos);
    }

    return nameNoTail;
  }

}