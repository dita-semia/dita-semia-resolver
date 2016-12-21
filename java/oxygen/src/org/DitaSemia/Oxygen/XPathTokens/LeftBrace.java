package org.DitaSemia.Oxygen.XPathTokens;

public class LeftBrace implements XPathToken {

	private final String leftBrace = "(";
	
	@Override
	public Type getType() {
		return XPathToken.Type.LEFT_BRACE;
	}

	@Override
	public String toString() {
		return leftBrace;
	}

}
