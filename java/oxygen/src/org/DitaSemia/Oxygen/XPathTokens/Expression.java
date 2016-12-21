package org.DitaSemia.Oxygen.XPathTokens;

public class Expression implements XPathToken {

	private String expression;

	public Expression(String expression) {
		this.expression = expression;
	}

	@Override
	public Type getType() {
		return XPathToken.Type.EXPRESSION;
	}

	@Override
	public String toString() {
		return expression;
	}
}
