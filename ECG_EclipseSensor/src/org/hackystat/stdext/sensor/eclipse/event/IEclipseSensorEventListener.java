package org.hackystat.stdext.sensor.eclipse.event;


/**
 * Provides an even listener for the information Eclipse sensor gathers. A client 
 * must implement this class and override the notify method in order to be notified 
 * when a event of action (such as opened
 * project, closed project, saved file, and so forth) is triggered. the client can know
 * what kind of even is triggered, checking the even flag of the EclipseSensorEvent instance.
 * For, example, to check if the event is notified when project is opened, the client 
 * can override the notify method such as:
 * 
 * <pre>
 * public void notify (EclispeSensorEvent event) {
 *   if (event.getEvent() != EclipseSensorEvent.PROJECT_OPEN) {
 *     return;
 *   }
 *   String workspacePath = event.getResource().getWorkspace().getRoot().getLocation().toString();
 *   IFolder cvsFolder = ((IProject) event.getResource()).getFolder("CVS");
 *   ...
 * }
 * </pre>
 * 
 * After implementing the sub class, the client need to add this class to the 
 * <code>org.hackystat.stdext.sensor.eclipse.EclipseSensor</code>. To add the implementing class to
 * the <code>EclipseSensor</code>, use <code>EclipseSensor#addEclipseSensorEventListener()</code>
 * method such as:
 * 
 * <pre>
 * EclipseSensor.getInstance().addEclipseSensorEventListener(this);
 * </pre>
 * 
 * Note that the "this" points to the implementing sub class.
 * <p>Whenever a new sensor properties is supposed to be read in Eclipse sensor, you must set the 
 * new sensor properties in such a way that :
 * 
 * <pre>
 * File sensorPropertiesFile = new File(...);
 * SensorProperties newProperties = new SensorProperties(EclipseSensor.EclipseSensor.SENSOR_ECLIPSE,
 *                                                       sensorPropertiesFile);
 * EclipseSensor.getInstance().setSensorProperties(newProperties);
 * </pre>
 *
 * @author Takuya Yamashita
 * @version $Id: IEclipseSensorEventListener.java,v 1.4 2004/01/27 02:02:37 takuyay Exp $
 */
public interface IEclipseSensorEventListener {
  /**
   * Provides a notification method to be called when the implementing class is notified.
   *
   * @param event The EclipseSensorEvent instance to hold an event information (flag).
   */
  void notify(EclipseSensorEvent event);
}
