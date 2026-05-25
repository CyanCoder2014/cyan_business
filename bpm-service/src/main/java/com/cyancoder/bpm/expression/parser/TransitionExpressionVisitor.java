// Generated from com/Cyan/bpm/dynamicflow/expression/parser/TransitionExpression.g4 by ANTLR 4.13.1
package com.cyancoder.bpm.expression.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TransitionExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TransitionExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(TransitionExpressionParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#logicalOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOr(TransitionExpressionParser.LogicalOrContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#logicalAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAnd(TransitionExpressionParser.LogicalAndContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#equality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(TransitionExpressionParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#relational}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelational(TransitionExpressionParser.RelationalContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#addition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddition(TransitionExpressionParser.AdditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#multiplication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplication(TransitionExpressionParser.MultiplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(TransitionExpressionParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#dottedIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDottedIdentifier(TransitionExpressionParser.DottedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(TransitionExpressionParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TransitionExpressionParser#elements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElements(TransitionExpressionParser.ElementsContext ctx);
}