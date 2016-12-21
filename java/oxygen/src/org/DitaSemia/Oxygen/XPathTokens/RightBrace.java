package org.DitaSemia.Oxygen.XPathTokens;

public class RightBrace implements XPathToken {

	private final String rightBrace = ")";
	
	@Override
	public Type getType() {
		return XPathToken.Type.RIGHT_BRACE;
	}

	@Override
	public String toString() {
		return rightBrace;
	}

}
