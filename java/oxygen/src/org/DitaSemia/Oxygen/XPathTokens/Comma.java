package org.DitaSemia.Oxygen.XPathTokens;

public class Comma implements XPathToken {

	private final String comma = ",";
	
	@Override
	public Type getType() {
		return XPathToken.Type.COMMA;
	}

	@Override
	public String toString() {
		return comma;
	}
}
