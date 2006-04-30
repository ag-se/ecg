package de.fu_berlin.inf.focustracker.monitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.InteractionListener;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;
import de.fu_berlin.inf.focustracker.test.AbstractPluginTestCase;
import de.fu_berlin.inf.focustracker.test.TestJavaProject;

public class TestJavaEditorMonitor extends AbstractPluginTestCase {

	private TestJavaProject project;
	private IPackageFragment p1;
	private IType type1;
	private IMethod javaMethod;
//	private IType javaType;
//	private ICompilationUnit javaCu;
//	private IPackageFragment javaPackage;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		project = new TestJavaProject(this.getClass().getSimpleName());// + "-"
		
		p1 = project.createPackage("p1");
		type1 = project.createType(p1, "Type1.java", "public class Type1 { }");
		
		javaMethod = type1.createMethod("void testDecrement() { }", null, true, null);
//		javaType = (IType) javaMethod.getParent();
//		javaCu = (ICompilationUnit) javaType.getParent();
//		javaPackage = (IPackageFragment) javaCu.getParent();
		
		
	}
	
	public void testInit() throws Exception {
		TestListener testListener = new TestListener();
		EventDispatcher.getInstance().addListener(testListener);
		PackageExplorerPart packageExplorer = PackageExplorerPart.openInActivePerspective();
		packageExplorer.getTreeViewer().collapseAll();
		packageExplorer.getTreeViewer().expandAll();
//		new CompilationUnitEditor().
//		ICompilationUnit cu = member.getCompilationUnit();
//        IEditorPart javaEditor = JavaUI.openInEditor(cu);
//		de.fu_berlin.inf.focustracker.test.TestProject project = new de.fu_berlin.inf.focustracker.test.TestProject(this.getClass().getName());
		
		IEditorPart editorPart = JavaUI.openInEditor(javaMethod);
//		JavaUI.openInEditor(type1);
		JavaUI.revealInEditor(editorPart, (IJavaElement)javaMethod);
		
//		editorPart.getEditorInput().
		
		for (IJavaElement javaElement : InteractionRepository.getInstance().getElements().keySet()) {
			System.err.println(javaElement);
			for (JavaInteraction interaction : InteractionRepository.getInstance().getElements().get(javaElement).getInteractions()) {
				System.err.println(interaction);
			}
		}
		
		System.err.println(testListener.getReceivedInteractions().size());
		
	}
	
	class TestListener implements InteractionListener {

		private List<List<? extends Interaction>> receivedInteractions = new ArrayList<List<? extends Interaction>>();
		
		public void notifyInteractionObserved(List<? extends Interaction> aInteractions) {
			receivedInteractions.add(aInteractions);
		}

		public List<List<? extends Interaction>> getReceivedInteractions() {
			return receivedInteractions;
		}
		
	}
	
}
