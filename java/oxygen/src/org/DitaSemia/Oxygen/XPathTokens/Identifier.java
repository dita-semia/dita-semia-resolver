package org.DitaSemia.Oxygen.XPathTokens;

public class Identifier implements XPathToken {

	private String identifier;
	
	public Identifier(String identifier) {
		this.identifier = identifier;
	}
	
	@Override
	public Type getType() {
		return XPathToken.Type.IDENTIFIER;
	}

	@Override
	public String toString() {
		return identifier;
	}
}
