package org.DitaSemia.Base;

import java.util.StringTokenizer;

import net.sf.saxon.trans.XPathException;

public class EmbeddedXPathResolver {
	
	public static String resolve(String value, NodeWrapper contextNode) throws XPathException {

		StringTokenizer tokenizer 	= new StringTokenizer(value, "{}", true);
		StringBuilder 	result		= new StringBuilder();
		String 			nextToken;
		try {
			while (tokenizer.hasMoreTokens()) {
				nextToken = tokenizer.nextToken();
				if (nextToken.equals("}"))
				{
					result.append(nextToken);
					if (tokenizer.hasMoreTokens()) {
						nextToken = tokenizer.nextToken();
						if (!nextToken.equals("}")) {
							throw new XPathException("Invalid character in parameter ('" + nextToken + "'), '}' expected after '" + result.toString() + "'.");
						}
					} else {
						throw new XPathException("Character missing in parameter. '}' expected afer '" + value + "'.");
					}
				} else if (nextToken.equals("{")) {
					if (tokenizer.hasMoreTokens()) {
						nextToken = tokenizer.nextToken();
						if (nextToken.equals("{")) {
							result.append(nextToken);
						} else if (!nextToken.equals("}")){
							result.append(contextNode.evaluateXPathToString(nextToken));
							if (tokenizer.hasMoreTokens()) {
								nextToken = tokenizer.nextToken();
								if(!nextToken.equals("}")) {
									throw new XPathException("Invalid character in parameter ('" + nextToken + "'), '}' expected after '" + result.toString() + "'.");
								} 
							} else {
								throw new XPathException("Character missing in parameter. '}' expected after '" + value + "'.");
							}
						} else {
							throw new XPathException("Invalid character in parameter ('" + nextToken + "'), missing XPath Expression after '" + result.toString() + "'.");
						}
					} else {
						throw new XPathException("Character missing in parameter. '{' or XPath expression expected afer '" + value + "'.");
					}
				} else {
					result.append(nextToken);
				}
			}
		} catch (XPathNotAvaliableException e) {
			throw new XPathException("XPath cannot be evaluated on this node.");
		}
		//logger.info(sb.toString());
		return result.toString();
	}
}
