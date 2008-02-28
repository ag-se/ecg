package org.electrocodeogram.cpc.similarity.strategy;


import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.similarity.CPCSimilarityPlugin;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask;
import org.electrocodeogram.cpc.similarity.preferences.CPCPreferenceConstants;
import org.electrocodeogram.cpc.similarity.strategy.parsers.JavaLexer;
import org.electrocodeogram.cpc.similarity.strategy.parsers.ParserUtils;


/**
 * This strategy tries to parse the contents of the given clones and tries to normalise their
 * representation in order for other strategies to yield better results.
 * <br> 
 * It does not modify the score of the similarity evaluation directly.
 * 
 * @author vw
 */
public class JavaCodeNormalisingStrategy implements ISimilarityStrategy
{
	private static final Log log = LogFactory.getLog(JavaCodeNormalisingStrategy.class);

	public JavaCodeNormalisingStrategy()
	{
		log.trace("JavaCodeNormalisingStrategy()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy#calculateSimilarity(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider, org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Status calculateSimilarity(IStoreProvider storeProvider, ISimilarityStrategyTask task)
	{
		if (log.isTraceEnabled())
			log.trace("calculateSimilarity() - task: " + task);
		assert (task != null);

		//make sure we're looking at a java file
		if (!ISimilarityProvider.LANGUAGE_JAVA.equals(task.getLanguage()))
		{
			log.trace("calculateSimilarity() - ignoring non-java file.");
			return Status.SKIPPED;
		}

		//make sure the file hasn't already been reformatted
		int processingStatus = task.getProcessingStatus();
		if ((processingStatus & ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_WHITESPACE) != 0)
		{
			log.trace("calculateSimilarity() - ignoring already normalised java file.");
			return Status.SKIPPED;
		}

		//normalise the content of each clone
		int i = 0;
		for (String content : new String[] { task.getProcessedContent1(), task.getProcessedContent2() })
		{
			//parse the clone content
			JavaLexer lexer = new JavaLexer(new ANTLRStringStream(content));
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			//JavaParser parser = new JavaParser(tokenStream);

			List<Token> tokens = tokenStream.getTokens();
			//build a new, normalised representation
			StringBuilder sb = new StringBuilder(content.length());
			for (Token token : tokens)
			{
				//if (log.isTraceEnabled())
				//log.trace("token: " + token);

				//ignore all whitespace characters and comments
				if (ParserUtils.isWhitespace(lexer, token.getType()))
					continue;

				if (CPCSimilarityPlugin.getDefault().getPluginPreferences().getBoolean(
						CPCPreferenceConstants.PREF_SIMILARITY_STRATEGY_JAVACODENORMALISING_STRIPCOMMENTS)
						&& ParserUtils.isComment(lexer, token.getType()))
					continue;

				/*
				 * TODO: This approach is probably too extreme.
				 * It would also replace class names and method names with an <<ID>> placeholder.
				 * Thus only basic command structures remain.
				 * 
				 * One possible improvement would be to use numbered place holders and reuse
				 * place holders for equal identifiers, i.e. "args" is replace with "<<ID21>>" everywhere
				 * But even that might still be too extreme.
				 */
				/*
				//replace variable and method names with a special placeholder
				if (ParserUtils.isIdentifier(lexer, token.getType()))
				{
					//ok, replace it with an identifier placeholder
					sb.append("<<ID>>");
				}
				else if (ParserUtils.isLiteral(lexer, token.getType()))
				{
					//ok, replace it with an identifier placeholder
					sb.append("<<LITERAL>>");
				}
				else
				{
					//leave this code part untouched
					sb.append(token.getText());
				}
				*/

				sb.append(token.getText());

				sb.append(' ');
			}

			//update the content
			String result = sb.toString();
			if (log.isTraceEnabled())
			{
				log.trace("calculateSimilarity() - old content: " + CoreStringUtils.truncateString(content));
				log.trace("calculateSimilarity() - new content: " + CoreStringUtils.truncateString(result));
			}

			if (i == 0)
				task
						.setProcessedContent1(result, ISimilarityStrategyTask.PROCESSING_STATUS_FILTERED
								+ ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_WHITESPACE /*+ ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_IDENTIFIERS*/);
			else
				task
						.setProcessedContent2(result, ISimilarityStrategyTask.PROCESSING_STATUS_FILTERED
								+ ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_WHITESPACE /*+ ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_IDENTIFIERS*/);

			++i;
		}

		return Status.CONTINUE;
	}
}
