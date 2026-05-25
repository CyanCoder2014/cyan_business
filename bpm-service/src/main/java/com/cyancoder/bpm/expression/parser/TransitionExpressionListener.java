// Generated from com/Cyan/bpm/dynamicflow/expression/parser/TransitionExpression.g4 by ANTLR 4.13.1
package com.cyancoder.bpm.expression.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TransitionExpressionParser}.
 */
public interface TransitionExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(TransitionExpressionParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(TransitionExpressionParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOr(TransitionExpressionParser.LogicalOrContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOr(TransitionExpressionParser.LogicalOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAnd(TransitionExpressionParser.LogicalAndContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAnd(TransitionExpressionParser.LogicalAndContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#equality}.
	 * @param ctx the parse tree
	 */
	void enterEquality(TransitionExpressionParser.EqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#equality}.
	 * @param ctx the parse tree
	 */
	void exitEquality(TransitionExpressionParser.EqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#relational}.
	 * @param ctx the parse tree
	 */
	void enterRelational(TransitionExpressionParser.RelationalContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#relational}.
	 * @param ctx the parse tree
	 */
	void exitRelational(TransitionExpressionParser.RelationalContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#addition}.
	 * @param ctx the parse tree
	 */
	void enterAddition(TransitionExpressionParser.AdditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#addition}.
	 * @param ctx the parse tree
	 */
	void exitAddition(TransitionExpressionParser.AdditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#multiplication}.
	 * @param ctx the parse tree
	 */
	void enterMultiplication(TransitionExpressionParser.MultiplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#multiplication}.
	 * @param ctx the parse tree
	 */
	void exitMultiplication(TransitionExpressionParser.MultiplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(TransitionExpressionParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(TransitionExpressionParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#dottedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterDottedIdentifier(TransitionExpressionParser.DottedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#dottedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitDottedIdentifier(TransitionExpressionParser.DottedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(TransitionExpressionParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(TransitionExpressionParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TransitionExpressionParser#elements}.
	 * @param ctx the parse tree
	 */
	void enterElements(TransitionExpressionParser.ElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TransitionExpressionParser#elements}.
	 * @param ctx the parse tree
	 */
	void exitElements(TransitionExpressionParser.ElementsContext ctx);
}