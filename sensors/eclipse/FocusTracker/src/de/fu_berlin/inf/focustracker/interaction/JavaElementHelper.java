package de.fu_berlin.inf.focustracker.interaction;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;

public class JavaElementHelper {

	
	public static JavaElementResourceAndName getRepresentation(IJavaElement aJavaElement) {
		JavaElementInformation elementInfo = getPackage(aJavaElement);
		String path = aJavaElement.getPath().toString();
		// remove the project name
		System.err.println("path1: " + path + " projectname: " + aJavaElement.getJavaProject().getProject().getName());
		path = path.replaceFirst("/" + aJavaElement.getJavaProject().getProject().getName(), "");
		System.err.println("path2: " + path);
		
		return new JavaElementResourceAndName(path, getNameForElement(aJavaElement, elementInfo));
	}
	
	public static String toString(IJavaElement aJavaElement) {
		
		StringBuffer ret = new StringBuffer();
		JavaElementInformation elementInfo = getPackage(aJavaElement);
		ret.append(elementInfo.getPackageName() + "." + elementInfo.getFileName());
		ret.append("#");
		try {
			ret.append(getNameForElement(aJavaElement, elementInfo));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		ret.append(" in \"");
		ret.append(aJavaElement.getPath());
		ret.append("\"");
		System.err.println(ret.toString());
		return ret.toString();
	}
	
	private static String getNameForElement(IJavaElement aJavaElement, JavaElementInformation aElementInfo) {
		StringBuffer ret = new StringBuffer();
		if (aJavaElement instanceof SourceMethod) {
			SourceMethod method = (SourceMethod) aJavaElement;
			ret.append(method.getElementName());
			ret.append("(");
			addParameters(ret, method.getParameterTypes());
			ret.append(")");
		} else if (aJavaElement instanceof SourceField){
			SourceField field = (SourceField) aJavaElement;
			ret.append(field.getElementName());
		} else if (aJavaElement instanceof CompilationUnit) {
			// remove #
//			ret.deleteCharAt(ret.length()-1);
		} else if (aJavaElement instanceof IPackageDeclaration) {
			ret = new StringBuffer(aElementInfo.getPackageName());
		}
		return ret.toString();
	}
	
	public static IJavaElement getCompilationUnit(IJavaElement aJavaElement) {
		if(aJavaElement instanceof ICompilationUnit || aJavaElement == null) {
			return aJavaElement;
		}
		IJavaElement parent = null;
		for (parent = aJavaElement.getParent(); !(parent instanceof ICompilationUnit) && !(parent instanceof IClassFile) && parent != null; parent = parent.getParent());
		
		return parent;
	}
	
	
	
	
	private static JavaElementInformation getPackage(IJavaElement aJavaElement) {
		
		if(aJavaElement == null) {
			return new JavaElementInformation();
		}

		if (aJavaElement instanceof CompilationUnit) {
			CompilationUnit unit = (CompilationUnit) aJavaElement;
//			return new String(unit.getPackageName()[0]) + "." + unit.getElementName();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < unit.getPackageName().length; i++) {
				sb.append(unit.getPackageName()[i]);
				if(i<unit.getPackageName().length-1) {
					sb.append('.');
				}
			}
//			for (char[] chars : unit.getPackageName()) {
//				sb.append(chars);
//				sb.append('.');
//			}
			return new JavaElementInformation(sb.toString(), unit.getElementName());
		}
		return getPackage(aJavaElement.getParent());
	}
	
	private static void addParameters(StringBuffer aBuffer, String[] aParameters) {
		boolean isFirst = true;
		for (String param : aParameters) {
			if(!isFirst) {
				aBuffer.append(",");
			} else {
				isFirst = false;
			}
			aBuffer.append(Signature.toString(param));
		}
	}
	
}
class JavaElementInformation {
	
	private String packageName = "unknown";
	private String fileName = "unknown";

	public JavaElementInformation() {}
	public JavaElementInformation(String aPackageName, String aFileName) {
		packageName = aPackageName;
		fileName = aFileName;
	}
	public String getFileName() {
		return fileName;
	}
	public String getPackageName() {
		return packageName;
	}
}

