package org.electrocodeogram.cpc.test.utils;


import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;


public class TestJavaUtils
{
	private static Log log = LogFactory.getLog(TestJavaUtils.class);

	public static IJavaProject getJavaProject(String projectName)
	{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.exists())
			return null;

		try
		{
			project.open(null /* IProgressMonitor */);
		}
		catch (CoreException e)
		{
			log.error("unable to open java project - " + projectName + " - " + e, e);
			return null;
		}
		IJavaProject javaProject = JavaCore.create(project);

		return javaProject;
	}

	public static ICompilationUnit getJavaCompilationUnit(String projectName, String packageName, String className)
	{
		IType lwType = getJavaClass(projectName, packageName, className);
		if (lwType == null)
			return null;

		ICompilationUnit lwCompilationUnit = lwType.getCompilationUnit();

		return lwCompilationUnit;
	}

	public static IType getJavaClass(String projectName, String packageName, String className)
	{
		IJavaProject javaProject = getJavaProject(projectName);
		if (javaProject == null)
			return null;

		IType lwType;
		try
		{
			lwType = javaProject.findType(packageName + "." + className);
		}
		catch (JavaModelException e)
		{
			log.error("unable to find java class - " + packageName + "." + className + " - " + e, e);
			return null;
		}

		return lwType;
	}

	public static IField getJavaField(String projectName, String packageName, String className, String fieldName)
	{
		IType lwType = getJavaClass(projectName, packageName, className);
		if (lwType == null)
			return null;

		IField field = lwType.getField(fieldName);

		return field;
	}

	@SuppressWarnings("unchecked")
	public static void reformatSource(IDocument document)
	{
		// take default Eclipse formatting options
		Map<String, String> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		// initialize the compiler settings to be able to format 1.5 code
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);

		// change the option to wrap each enum constant on a new line
		options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS, DefaultCodeFormatterConstants
				.createAlignmentValue(true, DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));

		// instanciate the default code formatter with the given options
		CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);

		// retrieve the source to format
		TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, // format a compilation unit
				document.get(), // source to format
				0, // starting position
				document.getLength(), // length
				0, // initial indentation
				System.getProperty("line.separator") // line separator
				);

		try
		{
			edit.apply(document);
		}
		catch (MalformedTreeException e)
		{
			e.printStackTrace();
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}

	}

	public static void refactorRenameField(String projectName, String packageName, String className,
			String oldFieldName, String newFieldName) throws CoreException
	{
		RefactoringContribution refactoringContrib = RefactoringCore
				.getRefactoringContribution(IJavaRefactorings.RENAME_FIELD);
		RefactoringDescriptor refactoringDescr = refactoringContrib.createDescriptor();
		//should be a RenameJavaElementDescriptor
		RenameJavaElementDescriptor javaRefDescr = (RenameJavaElementDescriptor) refactoringDescr;

		//select element to rename (the static "xyz" field)
		IField field = getJavaField(projectName, packageName, className, oldFieldName);
		javaRefDescr.setJavaElement(field);

		//configure refactoring
		javaRefDescr.setProject(projectName);
		javaRefDescr.setNewName(newFieldName);
		javaRefDescr.setRenameGetters(true);
		javaRefDescr.setRenameSetters(true);
		javaRefDescr.setUpdateReferences(true);
		javaRefDescr.setUpdateTextualOccurrences(false);
		//javaRefDescr.setUpdateSimilarDeclarations(true);
		//javaRefDescr.setUpdateHierarchy(true);
		//javaRefDescr.setUpdateQualifiedNames(true);

		Refactoring refactoring = javaRefDescr.createRefactoring(new RefactoringStatus());

		PerformRefactoringOperation refactoringOp = new PerformRefactoringOperation(refactoring,
				CheckConditionsOperation.ALL_CONDITIONS);

		//execute the refactoring
		ResourcesPlugin.getWorkspace().run(refactoringOp, null);

	}
}
