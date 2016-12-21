package org.DitaSemia.Oxygen.XPathTokens;

public interface XPathToken {
	
	public enum Type {
		EXPRESSION, IDENTIFIER, STRING_CONSTANT, LEFT_BRACE, RIGHT_BRACE, COMMA
	}
	
	Type getType();
}
