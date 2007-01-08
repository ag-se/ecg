package org.electrocodeogram.codereplay.userInterface;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;;

/**
 * The ReplayPerspective puts the two views used in this plugin together in one eclipse perspective.
 * 
 * @author marco kranz
 */
public class ReplayPerspective implements IPerspectiveFactory {

	public ReplayPerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout layout) {
//		 Get the editor area.
		String editorArea = layout.getEditorArea();
		//layout.setEditorAreaVisible(true);
        layout.setFixed(true);
        layout.setEditorAreaVisible(false);
//      Top left: Resource Navigator view and Bookmarks view placeholder
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,
			editorArea);
		topLeft.addView("org.electrocodeogram.codereplay.userInterface.ReplayTreeView");
        //-----
		//topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);

		// Bottom left: Outline view and Property Sheet view
		//IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f,
		//	"topLeft");
		//bottomLeft.addView(IPageLayout.ID_OUTLINE);
        //-------
		//bottomLeft.addView(IPageLayout.ID_PROP_SHEET);

		// Bottom right: Task List view
		layout.addView("org.electrocodeogram.codereplay.userInterface.ReplayView", IPageLayout.BOTTOM, 0.66f, editorArea);

        /*layout.addStandaloneView(
           );
        IViewLayout view = layout.getViewLayout(
           RecipePlugin.VIEW_CATEGORIES);
        view.setCloseable(false);
        view.setMoveable(false);*/
	}

}
