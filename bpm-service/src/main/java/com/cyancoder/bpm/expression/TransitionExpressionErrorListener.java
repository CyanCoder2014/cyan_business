package com.cyancoder.bpm.expression;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class TransitionExpressionErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        throw new IllegalArgumentException(
                "Invalid transition ANTLR condition at line " + line + ":" + charPositionInLine + " - " + msg,
                e
        );
    }
}

