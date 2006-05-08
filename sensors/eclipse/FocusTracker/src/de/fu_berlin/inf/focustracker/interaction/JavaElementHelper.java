package de.fu_berlin.inf.focustracker.interaction;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;

public class JavaElementHelper {

	public static String toString(IJavaElement aJavaElement) {
		
		StringBuffer ret = new StringBuffer();
		
		ret.append(getPackage(aJavaElement));
		ret.append("#");
		try {
			if (aJavaElement instanceof SourceMethod) {
				SourceMethod method = (SourceMethod) aJavaElement;
				ret.append(method.getElementName());
				ret.append("(");
				addParameters(ret, method.getParameterTypes());
				ret.append(")");
			} else if (aJavaElement instanceof SourceField){
				SourceField field = (SourceField) aJavaElement;
				ret.append(field.getElementName());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		ret.append(" in \"");
		ret.append(aJavaElement.getPath());
		ret.append("\"");
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
	
	
	
	
	private static String getPackage(IJavaElement aJavaElement) {
		
		if(aJavaElement == null) {
			return "unkown package";
		}

		if (aJavaElement instanceof CompilationUnit) {
			CompilationUnit unit = (CompilationUnit) aJavaElement;
			return new String(unit.getPackageName()[0]) + "." + unit.getElementName();
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
