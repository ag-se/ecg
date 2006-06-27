package de.fu_berlin.inf.focustracker.repository;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ResolvedSourceField;

import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.test.AbstractPluginTestCase;
import de.fu_berlin.inf.focustracker.test.TestJavaProject;

public class TestInteractionRepository extends AbstractPluginTestCase {

	private IType type1;
	private IPackageFragment p1;
	private IMethod javaMethod;

	public void testGetAll() throws Exception {
		
		InteractionRepository rep = InteractionRepository.getInstance();
		final ECGExporter ecgExporter = new ECGExporter();
		TestJavaProject project = new TestJavaProject("test");
		p1 = project.createPackage("p1");
		type1 = project.createType(p1, "Type1.java", "public class Type1 { }");
		javaMethod = type1.createMethod("void testDecrement() { }", null, true, null);
		
		
		ResolvedSourceField resolvedSourceField = new ResolvedSourceField((JavaElement)javaMethod, "name", "key");
		//		for (int i = 0; i < 1000; i++) {
		JavaInteraction javaInteraction = new JavaInteraction(Action.COLLAPSED, resolvedSourceField, 1d, Origin.CONSOLE);
		rep.add(javaInteraction);
//		}
		
		System.err.println(rep.getAll(IMember.class).size());
		
		new Thread(new Runnable() {
			public void run() {
				ecgExporter.exportCurrentInteractions();
				System.err.println("export finished");
			};
		}).start();
		for (int i = 0; i < 100; i++) {
			JavaInteraction javaInteraction2 = new JavaInteraction(Action.COLLAPSED, resolvedSourceField, 1d, Origin.CONSOLE);
			rep.add(javaInteraction2);
			System.err.print(".");
		}
		
		
		
	}
}
