package org.electrocodeogram.cpc.similarity.strategy.parsers;


import org.antlr.runtime.Lexer;


public class ParserUtils
{
	public static boolean isCommentOrWhitespace(Lexer lexer, int type)
	{
		return isComment(lexer, type) || isWhitespace(lexer, type);
	}

	public static boolean isComment(Lexer lexer, int type)
	{
		assert (lexer != null);

		//TODO: for new we only support a JavaLexer
		if (type == JavaLexer.COMMENT || type == JavaLexer.LINE_COMMENT)
			return true;
		else
			return false;
	}

	public static boolean isWhitespace(Lexer lexer, int type)
	{
		assert (lexer != null);

		//TODO: for new we only support a JavaLexer
		if (type == JavaLexer.WS)
			return true;
		else
			return false;
	}

	public static boolean isIdentifier(JavaLexer lexer, int type)
	{
		assert (lexer != null);

		//TODO: for new we only support a JavaLexer
		if (type == JavaLexer.Identifier)
			return true;
		else
			return false;
	}

	public static boolean isLiteral(JavaLexer lexer, int type)
	{
		assert (lexer != null);

		//TODO: for new we only support a JavaLexer
		if (type == JavaLexer.CharacterLiteral || type == JavaLexer.DecimalLiteral
				|| type == JavaLexer.FloatingPointLiteral || type == JavaLexer.HexLiteral
				|| type == JavaLexer.OctalLiteral || type == JavaLexer.StringLiteral)
			return true;
		else
			return false;
	}
}
