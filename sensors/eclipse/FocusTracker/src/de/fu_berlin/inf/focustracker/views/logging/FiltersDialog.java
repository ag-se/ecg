package de.fu_berlin.inf.focustracker.views.logging;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class FiltersDialog extends TrayDialog {

	private ChartFilter filter;
	private List list = null;
    private IJavaElement[] javaElements = InteractionRepository.getInstance().getElements().values().toArray(new IJavaElement[0]);
	
	protected FiltersDialog(Shell aShell) {
		super(aShell);
	}

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        composite.setSize(new Point(128, 8));
        list = new List(composite, SWT.MULTI);
        updateUIFromFilter(getFilter());

        for (IJavaElement javaElement : javaElements) {
        	list.add(javaElement.getElementName());
		}
        
        return composite;
    }
    
    private void updateUIFromFilter(ChartFilter aFilter) {
		
	}

	public ChartFilter getFilter() {
        if (filter == null) {
            filter = new ChartFilter();
        }
        return filter;
    }
    
    /**
     * Sets the filter which this dialog is to configure.
     *
     * @param filter the filter
     */
    public void setFilter(ChartFilter aFilter) {
        this.filter = aFilter;
    }

    @Override
    protected void okPressed() {
    	java.util.List<IJavaElement> elementList = new ArrayList<IJavaElement>();
    	for (int selectedIndex : list.getSelectionIndices()) {
			elementList.add(javaElements[selectedIndex]);
			System.err.println("setting " + javaElements[selectedIndex]);
		}
    	getFilter().setJavaElements(elementList);
    	
    	super.okPressed();
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Filter");
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
//				ITaskListHelpContextIds.FILTERS_DIALOG);
    }
    
}
