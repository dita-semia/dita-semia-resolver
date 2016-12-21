package org.DitaSemia.Oxygen.XPathTokens;

public class StringConstant implements XPathToken {

	private String string;
	
	public StringConstant(String string) {
		this.string = string;
	}
	
	@Override
	public Type getType() {
		return XPathToken.Type.STRING_CONSTANT;
	}

	@Override
	public String toString() {
		return "'" + string + "'";
	}
	
	public String getStringValue() {
		return string;
	}
}
