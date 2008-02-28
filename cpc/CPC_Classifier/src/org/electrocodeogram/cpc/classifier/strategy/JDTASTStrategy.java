package org.electrocodeogram.cpc.classifier.strategy;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider.Type;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * A Java only strategy which tries to obtain an JDT AST for the underlying file in order
 * to add some classifications based on the AST element types which are located within
 * the code range of the clone.
 * 
 * @author vw
 */
public class JDTASTStrategy implements IClassificationStrategy
{
	private static final Log log = LogFactory.getLog(JDTASTStrategy.class);

	/**
	 * Specifies the complexity threshold from which on a clone should be
	 * tagged as {@link IClassificationProvider#CLASSIFICATION_COMPLEX}.
	 * 
	 * @see JDTASTStrategy.MyComplexity#getComplexity()
	 */
	//TODO: add a configuration page for this value
	protected static final double COMPLEXITY_THRESHOLD = 50.0;

	public JDTASTStrategy()
	{
		log.trace("JDTASTStrategy()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy#classify(org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider.Type, org.electrocodeogram.cpc.core.api.data.ICloneFile, org.electrocodeogram.cpc.core.api.data.IClone, java.lang.String, org.electrocodeogram.cpc.core.api.data.IClone, java.util.Map)
	 */
	@Override
	public Status classify(Type type, ICloneFile cloneFile, IClone clone, String fileContent, IClone originClone,
			Map<String, Double> result)
	{
		if (log.isTraceEnabled())
			log.trace("classify() - type: " + type + ", cloneFile: " + cloneFile + ", clone: " + clone
					+ ", fileContent: " + CoreUtils.objectToLength(fileContent) + ", originClone: " + originClone
					+ ", result: " + result);
		assert (type != null && cloneFile != null && clone != null && fileContent != null && result != null);

		/*
		 * Check preconditions.
		 */
		if (!cloneFile.getPath().toLowerCase().endsWith(".java"))
		{
			log.debug("classify() - skipping non-java file.");
			return Status.SKIPPED;
		}

		//get a reference to the ICompilationUnit
		IFile file = CoreFileUtils.getFileForCloneFile(cloneFile, false);
		if (file == null)
		{
			log.debug("classify() - unable to obtain file handle for clone file, skipping.");
			return Status.SKIPPED;
		}

		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(file);
		if (cu == null)
		{
			log.warn("classify() - unable to obtain compilation unit for file: " + cloneFile + " - " + file,
					new Throwable());
			return Status.SKIPPED;
		}

		//try to get the AST
		CompilationUnit ast = parse(cu);

		//collect clone contents from the ast
		//the nodes which are completely enclosed within the clone
		List<ASTNode> astContents = new LinkedList<ASTNode>();
		//the nodes which intersect with the clones range
		List<ASTNode> astContext = new LinkedList<ASTNode>();
		ast.accept(new MyASTVisitor(clone, astContents, astContext));

		if (log.isTraceEnabled())
			log.trace("classify() - collected AST nodes: " + astContents);

		boolean modified = false;

		/*
		 * Calculate complexity.
		 */
		MyComplexity complexity = processNodes(astContents);
		MyComplexity contextCompl = processNodes(astContext);

		if (log.isDebugEnabled())
		{
			log.debug("classify() - complexity: " + complexity);
			log.debug("classify() - context   : " + contextCompl);
		}

		/*
		 * Add classifications if needed.
		 * 
		 * TODO: should we add only the topmost classification or all classifications
		 * which apply? I.e. only mark a clone as "CLASS" or also "METHOD" if the class
		 * isn't empty?
		 */
		if (complexity.getClassCount() > 0)
			addClassification(result, IClassificationProvider.CLASSIFICATION_CLASS);
		if (complexity.getMethodCount() > 0)
			addClassification(result, IClassificationProvider.CLASSIFICATION_METHOD);
		if (complexity.getLoopsCount() > 0)
			addClassification(result, IClassificationProvider.CLASSIFICATION_LOOP);
		if (complexity.getConditionCount() > 0)
			addClassification(result, IClassificationProvider.CLASSIFICATION_CONDITION);
		//only comments
		if (complexity.getNodeCount() == 0 || complexity.getNodeCount() == complexity.getCommentCount())
		{
			//also ensure that there aren't any non-comment nodes partly included
			if (contextCompl.getNodeCount() == 0 || contextCompl.getNodeCount() == contextCompl.getCommentCount())
				addClassification(result, IClassificationProvider.CLASSIFICATION_COMMENT);
			/*
			 * TODO:/FIXME: this approach does not mark clones as comments if they contain a complete method
			 * javadoc block.
			 * 
			 */
		}
		//only an identifier
		if (complexity.getNodeCount() == 1 && complexity.getIdentifierCount() == 1 && contextCompl.getNodeCount() == 0)
		{
			addClassification(result, IClassificationProvider.CLASSIFICATION_IDENTIFIER);
		}

		//TODO: this value will need lots of tweaking
		if (complexity.getComplexity() > COMPLEXITY_THRESHOLD)
		{
			log.debug("classify() - marking clone as complex.");

			addClassification(result, IClassificationProvider.CLASSIFICATION_COMPLEX);
		}

		if (!modified)
			return Status.SKIPPED;
		else
			return Status.MODIFIED;
	}

	/**
	 * Either adds the given classification with value 1.0 if it doesn't exist yet
	 * or adds 1.0 to the weight of an existing classification.
	 */
	protected void addClassification(Map<String, Double> result, String classification)
	{
		if (log.isDebugEnabled())
			log.debug("addClassification() - classification: " + classification);

		if (!result.containsKey(classification))
			result.put(classification, new Double(1.0));
		else
			result.put(classification, new Double(result.get(classification) + 1.0));
	}

	protected MyComplexity processNodes(List<ASTNode> astContents)
	{
		MyComplexity result = new MyComplexity();

		for (ASTNode node : astContents)
		{
			if (log.isTraceEnabled())
				log.trace("classify() - NODE: " + node);

			result.count(node);
		}

		return result;
	}

	/**
	 * Taken from: http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
	 */
	protected CompilationUnit parse(ICompilationUnit unit)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}

	/**
	 * Simple AST visitor which collects all {@link ASTNode}s which are located within the
	 * range of the given clone.
	 */
	protected class MyASTVisitor extends ASTVisitor
	{
		private IClone clone;
		private List<ASTNode> contents;
		private List<ASTNode> context;

		protected MyASTVisitor(IClone clone, List<ASTNode> contents, List<ASTNode> context)
		{
			assert (clone != null && contents != null);

			this.clone = clone;
			this.contents = contents;
			this.context = context;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
		 */
		@Override
		public void preVisit(ASTNode node)
		{
			int nodeStart = node.getStartPosition();
			int nodeEnd = node.getStartPosition() + node.getLength() - 1;

			//check if this node is completely contained within the clone
			if (clone.getOffset() <= nodeStart && nodeEnd <= clone.getEndOffset())
			{
				if (log.isTraceEnabled())
					log.trace("MyASTVisitor.preVisit() - clone contains ast node: " + node);

				contents.add(node);
			}
			//check if the node intersects with the clone
			else if ((clone.getOffset() <= nodeStart && nodeStart <= clone.getEndOffset())
					|| (clone.getOffset() <= nodeEnd && nodeEnd <= clone.getEndOffset()))
			{
				if (log.isTraceEnabled())
					log.trace("MyASTVisitor.preVisit() - clone intersects ast node: " + node);

				context.add(node);
			}
		}
	}

	/**
	 * Simple counter class for clone complexity based on node type and count. 
	 */
	protected class MyComplexity
	{
		private int classCount = 0;
		private int methodCount = 0;
		private int loopCount = 0;
		private int blocksCount = 0;
		private int conditionCount = 0;
		private int condExpressionCount = 0;
		private int commentCount = 0;
		private int identifierCount = 0;
		private int nodeCount = 0;

		public int getClassCount()
		{
			return classCount;
		}

		public void count(ASTNode node)
		{
			++nodeCount;

			switch (node.getNodeType())
			{
				case ASTNode.COMPILATION_UNIT:
					log.trace("MyComplexity.count() - a class.");
					++classCount;
					break;

				case ASTNode.TYPE_DECLARATION:
					log.trace("MyComplexity.count() - a (sub)class.");
					++classCount;
					break;

				case ASTNode.METHOD_DECLARATION:
					log.trace("MyComplexity.count() - a method.");
					++methodCount;
					break;

				case ASTNode.WHILE_STATEMENT:
				case ASTNode.FOR_STATEMENT:
				case ASTNode.ENHANCED_FOR_STATEMENT:
				case ASTNode.DO_STATEMENT:
					log.trace("MyComplexity.count() - a loop.");
					++loopCount;
					break;

				case ASTNode.BLOCK:
					log.trace("MyComplexity.count() - a block.");
					++blocksCount;
					break;

				case ASTNode.IF_STATEMENT:
				case ASTNode.SWITCH_STATEMENT:
					log.trace("MyComplexity.count() - a condition.");
					++conditionCount;
					break;

				case ASTNode.CONDITIONAL_EXPRESSION:
					log.trace("MyComplexity.count() - a conditional expression.");
					++condExpressionCount;
					break;

				case ASTNode.BLOCK_COMMENT:
				case ASTNode.LINE_COMMENT:
				case ASTNode.JAVADOC:
					log.trace("MyComplexity.count() - a comment.");
					++commentCount;
					break;

				case ASTNode.SIMPLE_NAME:
				case ASTNode.QUALIFIED_NAME:
					log.trace("MyComplexity.count() - an identifier.");
					++identifierCount;
					break;

				default:
					log.trace("unknown - class: " + node.getClass() + " - type: " + node.getNodeType());
					break;
			}

		}

		public int getMethodCount()
		{
			return methodCount;
		}

		public int getLoopsCount()
		{
			return loopCount;
		}

		public int getBlocksCount()
		{
			return blocksCount;
		}

		public int getConditionCount()
		{
			return conditionCount;
		}

		public int getCondExpressionCount()
		{
			return condExpressionCount;
		}

		public int getCommentCount()
		{
			return commentCount;
		}

		public int getIdentifierCount()
		{
			return identifierCount;
		}

		public int getNodeCount()
		{
			return nodeCount;
		}

		/**
		 * TODO: this will need lots of tweaking.
		 * 
		 * @return the weighted complexity of the clone.
		 */
		//TODO: add a configuration page for the values
		public double getComplexity()
		{
			return classCount * 10 + methodCount * 3 + loopCount * 2 + blocksCount + conditionCount
					+ condExpressionCount + identifierCount * 0.2 + nodeCount * 0.1;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "MyComplexity[complexity: " + getComplexity() + " - classes: " + classCount + ", methods: "
					+ methodCount + ", loops: " + loopCount + ", blocks: " + blocksCount + ", cond: " + conditionCount
					+ ", condExpr: " + condExpressionCount + ", comments: " + commentCount + ", identifiers: "
					+ identifierCount + ", nodes: " + nodeCount + "]";
		}
	}
}
