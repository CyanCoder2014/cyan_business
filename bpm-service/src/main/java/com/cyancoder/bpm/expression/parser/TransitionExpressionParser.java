// Generated from com/vasl/bpm/dynamicflow/expression/parser/TransitionExpression.g4 by ANTLR 4.13.1
package com.cyancoder.bpm.expression.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class TransitionExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, LE=8, GE=9, EQ=10, 
		NEQ=11, GT=12, LT=13, PLUS=14, MINUS=15, MUL=16, DIV=17, AND=18, OR=19, 
		NOT=20, RANGE_AND=21, CONTAINS=22, NOTCONTAINS=23, BETWEEN=24, IS=25, 
		NULL=26, EMPTY=27, STARTSWITH=28, ENDSWITH=29, CONTAINSSTR=30, MATCHES=31, 
		BOOLEAN=32, NUMBER=33, STRING=34, IDENTIFIER=35, WS=36;
	public static final int
		RULE_expr = 0, RULE_logicalOr = 1, RULE_logicalAnd = 2, RULE_equality = 3, 
		RULE_relational = 4, RULE_addition = 5, RULE_multiplication = 6, RULE_atom = 7, 
		RULE_dottedIdentifier = 8, RULE_list = 9, RULE_elements = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"expr", "logicalOr", "logicalAnd", "equality", "relational", "addition", 
			"multiplication", "atom", "dottedIdentifier", "list", "elements"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'.'", "'?.'", "'['", "']'", "','", "'<='", "'>='", 
			"'=='", "'!='", "'>'", "'<'", "'+'", "'-'", "'*'", "'/'", "'&&'", "'||'", 
			"'not'", "'and'", "'contains'", "'notContains'", "'between'", "'is'", 
			"'null'", "'empty'", "'startsWith'", "'endsWith'", "'containsStr'", "'matches'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, "LE", "GE", "EQ", "NEQ", 
			"GT", "LT", "PLUS", "MINUS", "MUL", "DIV", "AND", "OR", "NOT", "RANGE_AND", 
			"CONTAINS", "NOTCONTAINS", "BETWEEN", "IS", "NULL", "EMPTY", "STARTSWITH", 
			"ENDSWITH", "CONTAINSSTR", "MATCHES", "BOOLEAN", "NUMBER", "STRING", 
			"IDENTIFIER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "TransitionExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TransitionExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public LogicalOrContext logicalOr() {
			return getRuleContext(LogicalOrContext.class,0);
		}
		public TerminalNode EOF() { return getToken(TransitionExpressionParser.EOF, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22);
			logicalOr();
			setState(23);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalOrContext extends ParserRuleContext {
		public List<LogicalAndContext> logicalAnd() {
			return getRuleContexts(LogicalAndContext.class);
		}
		public LogicalAndContext logicalAnd(int i) {
			return getRuleContext(LogicalAndContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(TransitionExpressionParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(TransitionExpressionParser.OR, i);
		}
		public LogicalOrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterLogicalOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitLogicalOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitLogicalOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOrContext logicalOr() throws RecognitionException {
		LogicalOrContext _localctx = new LogicalOrContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_logicalOr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			logicalAnd();
			setState(30);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(26);
				match(OR);
				setState(27);
				logicalAnd();
				}
				}
				setState(32);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalAndContext extends ParserRuleContext {
		public List<EqualityContext> equality() {
			return getRuleContexts(EqualityContext.class);
		}
		public EqualityContext equality(int i) {
			return getRuleContext(EqualityContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(TransitionExpressionParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(TransitionExpressionParser.AND, i);
		}
		public LogicalAndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalAnd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterLogicalAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitLogicalAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitLogicalAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalAndContext logicalAnd() throws RecognitionException {
		LogicalAndContext _localctx = new LogicalAndContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_logicalAnd);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33);
			equality();
			setState(38);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(34);
				match(AND);
				setState(35);
				equality();
				}
				}
				setState(40);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EqualityContext extends ParserRuleContext {
		public List<RelationalContext> relational() {
			return getRuleContexts(RelationalContext.class);
		}
		public RelationalContext relational(int i) {
			return getRuleContext(RelationalContext.class,i);
		}
		public List<TerminalNode> EQ() { return getTokens(TransitionExpressionParser.EQ); }
		public TerminalNode EQ(int i) {
			return getToken(TransitionExpressionParser.EQ, i);
		}
		public List<TerminalNode> NEQ() { return getTokens(TransitionExpressionParser.NEQ); }
		public TerminalNode NEQ(int i) {
			return getToken(TransitionExpressionParser.NEQ, i);
		}
		public EqualityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equality; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterEquality(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitEquality(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitEquality(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualityContext equality() throws RecognitionException {
		EqualityContext _localctx = new EqualityContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_equality);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			relational();
			setState(46);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EQ || _la==NEQ) {
				{
				{
				setState(42);
				_la = _input.LA(1);
				if ( !(_la==EQ || _la==NEQ) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(43);
				relational();
				}
				}
				setState(48);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RelationalContext extends ParserRuleContext {
		public List<AdditionContext> addition() {
			return getRuleContexts(AdditionContext.class);
		}
		public AdditionContext addition(int i) {
			return getRuleContext(AdditionContext.class,i);
		}
		public TerminalNode CONTAINS() { return getToken(TransitionExpressionParser.CONTAINS, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode NOTCONTAINS() { return getToken(TransitionExpressionParser.NOTCONTAINS, 0); }
		public TerminalNode BETWEEN() { return getToken(TransitionExpressionParser.BETWEEN, 0); }
		public TerminalNode RANGE_AND() { return getToken(TransitionExpressionParser.RANGE_AND, 0); }
		public TerminalNode IS() { return getToken(TransitionExpressionParser.IS, 0); }
		public TerminalNode NULL() { return getToken(TransitionExpressionParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(TransitionExpressionParser.NOT, 0); }
		public TerminalNode EMPTY() { return getToken(TransitionExpressionParser.EMPTY, 0); }
		public TerminalNode STARTSWITH() { return getToken(TransitionExpressionParser.STARTSWITH, 0); }
		public TerminalNode ENDSWITH() { return getToken(TransitionExpressionParser.ENDSWITH, 0); }
		public TerminalNode CONTAINSSTR() { return getToken(TransitionExpressionParser.CONTAINSSTR, 0); }
		public TerminalNode MATCHES() { return getToken(TransitionExpressionParser.MATCHES, 0); }
		public List<TerminalNode> GT() { return getTokens(TransitionExpressionParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(TransitionExpressionParser.GT, i);
		}
		public List<TerminalNode> GE() { return getTokens(TransitionExpressionParser.GE); }
		public TerminalNode GE(int i) {
			return getToken(TransitionExpressionParser.GE, i);
		}
		public List<TerminalNode> LT() { return getTokens(TransitionExpressionParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(TransitionExpressionParser.LT, i);
		}
		public List<TerminalNode> LE() { return getTokens(TransitionExpressionParser.LE); }
		public TerminalNode LE(int i) {
			return getToken(TransitionExpressionParser.LE, i);
		}
		public RelationalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relational; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterRelational(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitRelational(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitRelational(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationalContext relational() throws RecognitionException {
		RelationalContext _localctx = new RelationalContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_relational);
		int _la;
		try {
			setState(103);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(49);
				addition();
				setState(50);
				match(CONTAINS);
				setState(51);
				list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(53);
				addition();
				setState(54);
				match(NOTCONTAINS);
				setState(55);
				list();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(57);
				addition();
				setState(58);
				match(BETWEEN);
				setState(59);
				addition();
				setState(60);
				match(RANGE_AND);
				setState(61);
				addition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(63);
				addition();
				setState(64);
				match(IS);
				setState(65);
				match(NULL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(67);
				addition();
				setState(68);
				match(IS);
				setState(69);
				match(NOT);
				setState(70);
				match(NULL);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(72);
				addition();
				setState(73);
				match(EMPTY);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(75);
				addition();
				setState(76);
				match(NOT);
				setState(77);
				match(EMPTY);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(79);
				addition();
				setState(80);
				match(STARTSWITH);
				setState(81);
				addition();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(83);
				addition();
				setState(84);
				match(ENDSWITH);
				setState(85);
				addition();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(87);
				addition();
				setState(88);
				match(CONTAINSSTR);
				setState(89);
				addition();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(91);
				addition();
				setState(92);
				match(MATCHES);
				setState(93);
				addition();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(95);
				addition();
				setState(100);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 13056L) != 0)) {
					{
					{
					setState(96);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 13056L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(97);
					addition();
					}
					}
					setState(102);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AdditionContext extends ParserRuleContext {
		public List<MultiplicationContext> multiplication() {
			return getRuleContexts(MultiplicationContext.class);
		}
		public MultiplicationContext multiplication(int i) {
			return getRuleContext(MultiplicationContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(TransitionExpressionParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(TransitionExpressionParser.PLUS, i);
		}
		public List<TerminalNode> MINUS() { return getTokens(TransitionExpressionParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(TransitionExpressionParser.MINUS, i);
		}
		public AdditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_addition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterAddition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitAddition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitAddition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditionContext addition() throws RecognitionException {
		AdditionContext _localctx = new AdditionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_addition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			multiplication();
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(106);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(107);
				multiplication();
				}
				}
				setState(112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicationContext extends ParserRuleContext {
		public List<AtomContext> atom() {
			return getRuleContexts(AtomContext.class);
		}
		public AtomContext atom(int i) {
			return getRuleContext(AtomContext.class,i);
		}
		public List<TerminalNode> MUL() { return getTokens(TransitionExpressionParser.MUL); }
		public TerminalNode MUL(int i) {
			return getToken(TransitionExpressionParser.MUL, i);
		}
		public List<TerminalNode> DIV() { return getTokens(TransitionExpressionParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(TransitionExpressionParser.DIV, i);
		}
		public MultiplicationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplication; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterMultiplication(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitMultiplication(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitMultiplication(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicationContext multiplication() throws RecognitionException {
		MultiplicationContext _localctx = new MultiplicationContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_multiplication);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			atom();
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL || _la==DIV) {
				{
				{
				setState(114);
				_la = _input.LA(1);
				if ( !(_la==MUL || _la==DIV) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(115);
				atom();
				}
				}
				setState(120);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AtomContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(TransitionExpressionParser.NUMBER, 0); }
		public TerminalNode STRING() { return getToken(TransitionExpressionParser.STRING, 0); }
		public TerminalNode BOOLEAN() { return getToken(TransitionExpressionParser.BOOLEAN, 0); }
		public TerminalNode NULL() { return getToken(TransitionExpressionParser.NULL, 0); }
		public DottedIdentifierContext dottedIdentifier() {
			return getRuleContext(DottedIdentifierContext.class,0);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_atom);
		try {
			setState(131);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(121);
				match(NUMBER);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(122);
				match(STRING);
				}
				break;
			case BOOLEAN:
				enterOuterAlt(_localctx, 3);
				{
				setState(123);
				match(BOOLEAN);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 4);
				{
				setState(124);
				match(NULL);
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 5);
				{
				setState(125);
				dottedIdentifier();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 6);
				{
				setState(126);
				list();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 7);
				{
				setState(127);
				match(T__0);
				setState(128);
				expr();
				setState(129);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DottedIdentifierContext extends ParserRuleContext {
		public List<TerminalNode> IDENTIFIER() { return getTokens(TransitionExpressionParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(TransitionExpressionParser.IDENTIFIER, i);
		}
		public DottedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dottedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterDottedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitDottedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitDottedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DottedIdentifierContext dottedIdentifier() throws RecognitionException {
		DottedIdentifierContext _localctx = new DottedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_dottedIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			match(IDENTIFIER);
			setState(138);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2 || _la==T__3) {
				{
				{
				setState(134);
				_la = _input.LA(1);
				if ( !(_la==T__2 || _la==T__3) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(135);
				match(IDENTIFIER);
				}
				}
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ListContext extends ParserRuleContext {
		public ElementsContext elements() {
			return getRuleContext(ElementsContext.class,0);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			match(T__4);
			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 64491618338L) != 0)) {
				{
				setState(142);
				elements();
				}
			}

			setState(145);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementsContext extends ParserRuleContext {
		public List<AtomContext> atom() {
			return getRuleContexts(AtomContext.class);
		}
		public AtomContext atom(int i) {
			return getRuleContext(AtomContext.class,i);
		}
		public ElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).enterElements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TransitionExpressionListener ) ((TransitionExpressionListener)listener).exitElements(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TransitionExpressionVisitor ) return ((TransitionExpressionVisitor<? extends T>)visitor).visitElements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementsContext elements() throws RecognitionException {
		ElementsContext _localctx = new ElementsContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_elements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			atom();
			setState(152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__6) {
				{
				{
				setState(148);
				match(T__6);
				setState(149);
				atom();
				}
				}
				setState(154);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001$\u009c\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001\u001d\b\u0001\n"+
		"\u0001\f\u0001 \t\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002"+
		"%\b\u0002\n\u0002\f\u0002(\t\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0005\u0003-\b\u0003\n\u0003\f\u00030\t\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004"+
		"c\b\u0004\n\u0004\f\u0004f\t\u0004\u0003\u0004h\b\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0005\u0005m\b\u0005\n\u0005\f\u0005p\t\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0005\u0006u\b\u0006\n\u0006\f\u0006x\t"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0084"+
		"\b\u0007\u0001\b\u0001\b\u0001\b\u0005\b\u0089\b\b\n\b\f\b\u008c\t\b\u0001"+
		"\t\u0001\t\u0003\t\u0090\b\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0005"+
		"\n\u0097\b\n\n\n\f\n\u009a\t\n\u0001\n\u0000\u0000\u000b\u0000\u0002\u0004"+
		"\u0006\b\n\f\u000e\u0010\u0012\u0014\u0000\u0005\u0001\u0000\n\u000b\u0002"+
		"\u0000\b\t\f\r\u0001\u0000\u000e\u000f\u0001\u0000\u0010\u0011\u0001\u0000"+
		"\u0003\u0004\u00aa\u0000\u0016\u0001\u0000\u0000\u0000\u0002\u0019\u0001"+
		"\u0000\u0000\u0000\u0004!\u0001\u0000\u0000\u0000\u0006)\u0001\u0000\u0000"+
		"\u0000\bg\u0001\u0000\u0000\u0000\ni\u0001\u0000\u0000\u0000\fq\u0001"+
		"\u0000\u0000\u0000\u000e\u0083\u0001\u0000\u0000\u0000\u0010\u0085\u0001"+
		"\u0000\u0000\u0000\u0012\u008d\u0001\u0000\u0000\u0000\u0014\u0093\u0001"+
		"\u0000\u0000\u0000\u0016\u0017\u0003\u0002\u0001\u0000\u0017\u0018\u0005"+
		"\u0000\u0000\u0001\u0018\u0001\u0001\u0000\u0000\u0000\u0019\u001e\u0003"+
		"\u0004\u0002\u0000\u001a\u001b\u0005\u0013\u0000\u0000\u001b\u001d\u0003"+
		"\u0004\u0002\u0000\u001c\u001a\u0001\u0000\u0000\u0000\u001d \u0001\u0000"+
		"\u0000\u0000\u001e\u001c\u0001\u0000\u0000\u0000\u001e\u001f\u0001\u0000"+
		"\u0000\u0000\u001f\u0003\u0001\u0000\u0000\u0000 \u001e\u0001\u0000\u0000"+
		"\u0000!&\u0003\u0006\u0003\u0000\"#\u0005\u0012\u0000\u0000#%\u0003\u0006"+
		"\u0003\u0000$\"\u0001\u0000\u0000\u0000%(\u0001\u0000\u0000\u0000&$\u0001"+
		"\u0000\u0000\u0000&\'\u0001\u0000\u0000\u0000\'\u0005\u0001\u0000\u0000"+
		"\u0000(&\u0001\u0000\u0000\u0000).\u0003\b\u0004\u0000*+\u0007\u0000\u0000"+
		"\u0000+-\u0003\b\u0004\u0000,*\u0001\u0000\u0000\u0000-0\u0001\u0000\u0000"+
		"\u0000.,\u0001\u0000\u0000\u0000./\u0001\u0000\u0000\u0000/\u0007\u0001"+
		"\u0000\u0000\u00000.\u0001\u0000\u0000\u000012\u0003\n\u0005\u000023\u0005"+
		"\u0016\u0000\u000034\u0003\u0012\t\u00004h\u0001\u0000\u0000\u000056\u0003"+
		"\n\u0005\u000067\u0005\u0017\u0000\u000078\u0003\u0012\t\u00008h\u0001"+
		"\u0000\u0000\u00009:\u0003\n\u0005\u0000:;\u0005\u0018\u0000\u0000;<\u0003"+
		"\n\u0005\u0000<=\u0005\u0015\u0000\u0000=>\u0003\n\u0005\u0000>h\u0001"+
		"\u0000\u0000\u0000?@\u0003\n\u0005\u0000@A\u0005\u0019\u0000\u0000AB\u0005"+
		"\u001a\u0000\u0000Bh\u0001\u0000\u0000\u0000CD\u0003\n\u0005\u0000DE\u0005"+
		"\u0019\u0000\u0000EF\u0005\u0014\u0000\u0000FG\u0005\u001a\u0000\u0000"+
		"Gh\u0001\u0000\u0000\u0000HI\u0003\n\u0005\u0000IJ\u0005\u001b\u0000\u0000"+
		"Jh\u0001\u0000\u0000\u0000KL\u0003\n\u0005\u0000LM\u0005\u0014\u0000\u0000"+
		"MN\u0005\u001b\u0000\u0000Nh\u0001\u0000\u0000\u0000OP\u0003\n\u0005\u0000"+
		"PQ\u0005\u001c\u0000\u0000QR\u0003\n\u0005\u0000Rh\u0001\u0000\u0000\u0000"+
		"ST\u0003\n\u0005\u0000TU\u0005\u001d\u0000\u0000UV\u0003\n\u0005\u0000"+
		"Vh\u0001\u0000\u0000\u0000WX\u0003\n\u0005\u0000XY\u0005\u001e\u0000\u0000"+
		"YZ\u0003\n\u0005\u0000Zh\u0001\u0000\u0000\u0000[\\\u0003\n\u0005\u0000"+
		"\\]\u0005\u001f\u0000\u0000]^\u0003\n\u0005\u0000^h\u0001\u0000\u0000"+
		"\u0000_d\u0003\n\u0005\u0000`a\u0007\u0001\u0000\u0000ac\u0003\n\u0005"+
		"\u0000b`\u0001\u0000\u0000\u0000cf\u0001\u0000\u0000\u0000db\u0001\u0000"+
		"\u0000\u0000de\u0001\u0000\u0000\u0000eh\u0001\u0000\u0000\u0000fd\u0001"+
		"\u0000\u0000\u0000g1\u0001\u0000\u0000\u0000g5\u0001\u0000\u0000\u0000"+
		"g9\u0001\u0000\u0000\u0000g?\u0001\u0000\u0000\u0000gC\u0001\u0000\u0000"+
		"\u0000gH\u0001\u0000\u0000\u0000gK\u0001\u0000\u0000\u0000gO\u0001\u0000"+
		"\u0000\u0000gS\u0001\u0000\u0000\u0000gW\u0001\u0000\u0000\u0000g[\u0001"+
		"\u0000\u0000\u0000g_\u0001\u0000\u0000\u0000h\t\u0001\u0000\u0000\u0000"+
		"in\u0003\f\u0006\u0000jk\u0007\u0002\u0000\u0000km\u0003\f\u0006\u0000"+
		"lj\u0001\u0000\u0000\u0000mp\u0001\u0000\u0000\u0000nl\u0001\u0000\u0000"+
		"\u0000no\u0001\u0000\u0000\u0000o\u000b\u0001\u0000\u0000\u0000pn\u0001"+
		"\u0000\u0000\u0000qv\u0003\u000e\u0007\u0000rs\u0007\u0003\u0000\u0000"+
		"su\u0003\u000e\u0007\u0000tr\u0001\u0000\u0000\u0000ux\u0001\u0000\u0000"+
		"\u0000vt\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000w\r\u0001\u0000"+
		"\u0000\u0000xv\u0001\u0000\u0000\u0000y\u0084\u0005!\u0000\u0000z\u0084"+
		"\u0005\"\u0000\u0000{\u0084\u0005 \u0000\u0000|\u0084\u0005\u001a\u0000"+
		"\u0000}\u0084\u0003\u0010\b\u0000~\u0084\u0003\u0012\t\u0000\u007f\u0080"+
		"\u0005\u0001\u0000\u0000\u0080\u0081\u0003\u0000\u0000\u0000\u0081\u0082"+
		"\u0005\u0002\u0000\u0000\u0082\u0084\u0001\u0000\u0000\u0000\u0083y\u0001"+
		"\u0000\u0000\u0000\u0083z\u0001\u0000\u0000\u0000\u0083{\u0001\u0000\u0000"+
		"\u0000\u0083|\u0001\u0000\u0000\u0000\u0083}\u0001\u0000\u0000\u0000\u0083"+
		"~\u0001\u0000\u0000\u0000\u0083\u007f\u0001\u0000\u0000\u0000\u0084\u000f"+
		"\u0001\u0000\u0000\u0000\u0085\u008a\u0005#\u0000\u0000\u0086\u0087\u0007"+
		"\u0004\u0000\u0000\u0087\u0089\u0005#\u0000\u0000\u0088\u0086\u0001\u0000"+
		"\u0000\u0000\u0089\u008c\u0001\u0000\u0000\u0000\u008a\u0088\u0001\u0000"+
		"\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000\u008b\u0011\u0001\u0000"+
		"\u0000\u0000\u008c\u008a\u0001\u0000\u0000\u0000\u008d\u008f\u0005\u0005"+
		"\u0000\u0000\u008e\u0090\u0003\u0014\n\u0000\u008f\u008e\u0001\u0000\u0000"+
		"\u0000\u008f\u0090\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000"+
		"\u0000\u0091\u0092\u0005\u0006\u0000\u0000\u0092\u0013\u0001\u0000\u0000"+
		"\u0000\u0093\u0098\u0003\u000e\u0007\u0000\u0094\u0095\u0005\u0007\u0000"+
		"\u0000\u0095\u0097\u0003\u000e\u0007\u0000\u0096\u0094\u0001\u0000\u0000"+
		"\u0000\u0097\u009a\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000"+
		"\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u0015\u0001\u0000\u0000"+
		"\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u000b\u001e&.dgnv\u0083\u008a"+
		"\u008f\u0098";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}