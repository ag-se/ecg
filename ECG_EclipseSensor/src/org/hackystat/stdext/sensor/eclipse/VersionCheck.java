package org.hackystat.stdext.sensor.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.update.internal.ui.UpdateUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides the version check function. Clients may call <code>processUpdateDialog(String)</code>
 * to check new version on the server, whose url is specified in the preference page, and
 * bring update info dialog if the new version is available. Supoorts for both 2x and 3x stream
 * version information.
 * 
 * @author Takuya Yamashita
 * @version $Id: VersionCheck.java,v 1.7 2004/07/22 09:54:39 takuyay Exp $
 *
 */
public class VersionCheck {
  
  /**
   * Processes update dialog to the user. The current version and new version appear in the dialog.
   * The element of the messages array is follows:
   * <ul>
   * <li>The element 0: first message.
   * <li>The element 1: the message between local version and server version.
   * <li>The element 2: last message.
   * </ul>  
   * @param updateUrl the url for the update site, which ends with "site.xml".
   * @param title the title to be shown in the update window page.
   * @param messages the messages to be shown in the update window page. 
   */
  public static void processUpdateDialog(String updateUrl, final String title, 
                                         final String[] messages) {
    if (updateUrl != null) {
      try {
        // Gets current version info.
        PluginVersionIdentifier localVerIdentifier = getLocalPluginVersionIdentifier();
        final String localVersionId = localVerIdentifier.toString();
        String qualifierVersion = localVerIdentifier.getQualifierComponent();

        // Gets new version info.
        URL url = new URL(updateUrl);
        Document serverDocument = parseXml(url.openStream());
        final PluginVersionIdentifier serverVerIdentifier = getVersionIdentifier(serverDocument,
            qualifierVersion);
        // Check if current version is not new version, then show pop up udate dialog.
        if ((serverVerIdentifier != null) 
             && serverVerIdentifier.isGreaterThan(localVerIdentifier)) {
          IWorkbench workbench = EclipseSensorPlugin.getInstance().getWorkbench();
          final IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
          final String HACKYSTAT_ICON = EclipseSensorI18n.getString("VersionCheck.hackystatIcon");
          Display.getDefault().asyncExec(new Runnable() {
              public void run() {
                MessageDialog dialog = new MessageDialog(window.getShell(),
                    title,
                    EclipseSensorPlugin.createImageDescriptor(HACKYSTAT_ICON).createImage(),
                    messages[0] + localVersionId + messages[1] + serverVerIdentifier.toString()
                    + messages[2],
                  MessageDialog.QUESTION,
                  new String[] {
                    EclipseSensorI18n.getString("VersionCheck.messageDialogButtonUpdate"),
                    EclipseSensorI18n.getString("VersionCheck.messageDialogButtonCancel")
                    },
                  0);
                int result = dialog.open();
                if (result == 0) {
                  openNewUpdatesWizard();
                }
              }
            });
        }
      }
      catch (NullPointerException e) {
//        EclipseSensorPlugin.getInstance().log(e);
        e.printStackTrace();
        // couldn't parse xml file, or even though xml is parsed, version attribute is not found.
      }
      catch (IOException e) {
        // URL connection was not established.
      }
    }
  }
  
  /**
   * Gets the local plugin version identifier.
   * @return the local plugin version identifier.
   */
  private static PluginVersionIdentifier getLocalPluginVersionIdentifier() {
    return EclipseSensorPlugin.getInstance().getDescriptor().getVersionIdentifier();
  }

  /**
   * Gets version information from the given Document object, checking the qualifier version. The
   * qualifier version is the last token of the version identifier. For example, if the version
   * identifier is 1.4.204.2x, the qualifier version is 2x.
   *
   * @param document The Document object parsed from XML file.
   * @param qualifierVersion The qualifier version. e.g 2x for the 1.4.204.2x
   *
   * @return The PluginVersionIdentifier instance if it's found.Returns null if the version
   *         information is not found.
   */
  private static PluginVersionIdentifier getVersionIdentifier(Document document,
    String qualifierVersion) {
    Element root = document.getDocumentElement();
    NodeList list = root.getChildNodes();
    final String FEATURE = "feature";
    final String VERSION = "version";
    for (int i = 0; i < list.getLength(); i++) {
      if ((list.item(i).getNodeType() == Node.ELEMENT_NODE)
          && list.item(i).getNodeName().equalsIgnoreCase(FEATURE)) {
        Element element = (Element) list.item(i);
        if (element.hasAttribute(VERSION)) {
          String version = element.getAttribute(VERSION);
          PluginVersionIdentifier identifier = new PluginVersionIdentifier(version);
          if (identifier.getQualifierComponent().equals(qualifierVersion)) {
            return identifier;
          }
        }
      }
    }
    return null;
  }

  /**
   * Parses xml file to generate Document object from a given input stream.
   *
   * @param input The given input stream from which xml file is read.
   *
   * @return The Documentation object which contains the content parsed from the XML file. Returns
   *         null if parse error occurs.
   */
  private static Document parseXml(InputStream input) {
    try {
      // Create a builder factory
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      // Create the builder and parse the file
      Document document = factory.newDocumentBuilder().parse(input);
      input.close();
      return document;
    }
    catch (SAXException e) {
      // A parsing error occurred; the xml input is not valid
    }
    catch (ParserConfigurationException e) {
      // ignored.
    }
    catch (IOException e) {
      // ignored.
    }
    return null;
  }
  
  /**
   * Openes new update wizard. Uses reflection depending upon eclipse verison stream.
   * So that each stream can implement different class to open new updat wizard. 
   */
  private static void openNewUpdatesWizard() {
    String qualifier = getLocalPluginVersionIdentifier().getQualifierComponent();
    Class wizardClass = null;
    Class updateUiClass = null;
    Constructor constructor = null;
    Object wizardObject = null;
    try {
      if (qualifier.equals("2x")) {
        wizardClass = Class.forName("org.eclipse.update.internal.ui.wizards.NewUpdatesWizard");
        String installWizard = "org.hackystat.stdext.sensor.eclipse.helper2x.InstallWizardHelper";
        Class installWizardHelperClass = Class.forName(installWizard);
        Object installWizardHelperObject = installWizardHelperClass.newInstance();
        Method method = installWizardHelperClass.getMethod("getNewUpdateWizard", new Class[] {});
        wizardObject = method.invoke(installWizardHelperObject, new Object[] {});
      }
      else if (qualifier.equals("3x")) {
        wizardClass = Class.forName("org.eclipse.update.internal.ui.wizards.InstallWizard");
        wizardObject = wizardClass.newInstance();
      }
      // Creates new resizable wizard dialog with the relected wizard class.
      IWorkbench workbench = EclipseSensorPlugin.getInstance().getWorkbench();
      Shell shell = workbench.getActiveWorkbenchWindow().getShell();
      // :RESOLVED: 5/24/04 from 3.0 M9
      Object wizardDialogObject = null;
      String wizardDialog = "";
      try {
        wizardDialog = "org.eclipse.update.internal.ui.wizards.ResizableInstallWizardDialog";
        Class wizardDialogClass = Class.forName(wizardDialog);
        Class[] classArgs = new Class[] {Shell.class, IWizard.class, String.class};
        Constructor wizardDialogCon = wizardDialogClass.getConstructor(classArgs);
        Object[] objectArgs = new Object[] {shell, (IWizard) wizardObject, ""};
        wizardDialogObject = wizardDialogCon.newInstance(objectArgs);
      }
      // :RESOLVED: 5/24/04 until 3.0 M8 
      catch (ClassNotFoundException e) {
        wizardDialog = "org.eclipse.update.internal.ui.wizards.ResizableWizardDialog";
        Class wizardDialogClass = Class.forName(wizardDialog);
        Class[] classArgs = new Class[] {Shell.class, IWizard.class};
        Constructor wizardDialogCon = wizardDialogClass.getConstructor(classArgs);
        Object[] objectArgs = new Object[] {shell, (IWizard) wizardObject};
        wizardDialogObject = wizardDialogCon.newInstance(objectArgs);
      }
      WizardDialog dialog = (WizardDialog) wizardDialogObject;
      dialog.create();
      //dialog.getShell().setText(UpdateUI.getString("InstallWizardAction.title")); //$NON-NLS-1$
      dialog.getShell().setSize(600, 500);
      dialog.open();
      
      // Checks if install is successful, then show restart popup dialog if so.
      Method method = wizardClass.getMethod("isSuccessfulInstall", new Class[] {});
      Boolean isSuccess = (Boolean) method.invoke(wizardObject, new Object[] {});
      Method restartMethod = null;
      if (isSuccess.booleanValue()) {
        updateUiClass = Class.forName("org.eclipse.update.internal.ui.UpdateUI");
        if (qualifier.equals("2x")) {
          restartMethod = updateUiClass.getMethod("informRestartNeeded", new Class[] {});
          restartMethod.invoke(updateUiClass, new Object[] {});
        }
        else if (qualifier.equals("3x")) {
          restartMethod = updateUiClass.getMethod("requestRestart", new Class[] {});
          restartMethod.invoke(updateUiClass, new Object[] {});
        }
      }
    }
    catch (InvocationTargetException e) {
      EclipseSensorPlugin.getInstance().log(e);
    }
    catch (Exception e) {
      EclipseSensorPlugin.getInstance().log(e);
    }
  }
}
