package org.hackystat.stdext.sensor.eclipse.helper2x;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.update.internal.ui.search.DefaultUpdatesSearchObject;
import org.eclipse.update.internal.ui.search.ISearchCategory;
import org.eclipse.update.internal.ui.search.SearchCategoryDescriptor;
import org.eclipse.update.internal.ui.search.SearchCategoryRegistryReader;
import org.eclipse.update.internal.ui.search.SearchObject;
import org.eclipse.update.internal.ui.wizards.NewUpdatesWizard;

import org.hackystat.stdext.sensor.eclipse.EclipseSensorPlugin;
/**
 * Provides an install wizard helper class for the Eclipse 2x (until 3.0 M4) in order to
 * be prepared for the Installation wizard. The main purpose of this class is to help
 * processing the creation of the wizard dialog with progress monitor in 2x stream. 
 * Clients should call:
 * <pre>
 * NewUpdatesWizard wizard = new InstallWizardHelper().getNewUpdateWizard();
 * </pre>
 * 
 * Should be included in the 2x package on the build process. In other words, 
 * should not be included in the 3x package.
 * @author Takuya Yamashita
 * @version $Id: InstallWizardHelper.java,v 1.2 2004/03/29 01:38:02 takuyay Exp $
 */
public class InstallWizardHelper {
  /** The <code>SearchObject</code> to be used in the <code>NewUpdateWizard</code> instantiation. */
  private SearchObject searchObject;
  /**
   * Sets up progress monitor dialog.
   */
  public InstallWizardHelper() {
    searchObject = new DefaultUpdatesSearchObject();
    String categoryId = searchObject.getCategoryId();
    SearchCategoryDescriptor searchCategoryDescriptor = 
                   SearchCategoryRegistryReader.getDefault().getDescriptor(categoryId);
    ISearchCategory category = searchCategoryDescriptor.createCategory();
    IWorkbench workbench = EclipseSensorPlugin.getInstance().getWorkbench();
    Shell shell = workbench.getActiveWorkbenchWindow().getShell();
    ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(shell);
    IRunnableWithProgress runnableWithProgress = searchObject.getSearchOperation(
                                                 shell.getDisplay(), category.getQueries());
    try {
      progressMonitorDialog.run(true, true, runnableWithProgress);
    }
    catch (InvocationTargetException e) {
      EclipseSensorPlugin.getInstance().log(e);
    }
    catch (InterruptedException e) {
      EclipseSensorPlugin.getInstance().log(e);
    }
  }
  
  /**
   * Returns the <code>NewUpdatesWizard</code> instance with the searched categories. Namely
   * the update wizard contains the information of the searched categories in the page.
   * @return <code>NewUpdatesWizard</code> instance.
   */
  public NewUpdatesWizard getNewUpdateWizard() {
    return new NewUpdatesWizard(searchObject);
  }
}

