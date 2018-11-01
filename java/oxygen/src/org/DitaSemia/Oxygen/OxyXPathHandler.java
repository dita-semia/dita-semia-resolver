package org.DitaSemia.Oxygen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import net.sf.saxon.trans.XPathException;

import org.DitaSemia.Oxygen.XPathTokens.Comma;
import org.DitaSemia.Oxygen.XPathTokens.Expression;
import org.DitaSemia.Oxygen.XPathTokens.Identifier;
import org.DitaSemia.Oxygen.XPathTokens.LeftBrace;
import org.DitaSemia.Oxygen.XPathTokens.RightBrace;
import org.DitaSemia.Oxygen.XPathTokens.StringConstant;
import org.DitaSemia.Oxygen.XPathTokens.XPathToken;

import org.DitaSemia.Base.XPathNotAvaliableException;

import org.apache.log4j.Logger;

public class OxyXPathHandler {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OxyXPathHandler.class.getName());

	private static final int STATE_SEQUENCE 	= 1;
	private static final int STATE_EXPRESSION 	= 2;
	private static final int STATE_PARAMETER 	= 3;
	private static final int STATE_CONST 		= 4;
	private static final int STATE_CONST_EMPTY	= 5;

	private static OxyXPathHandler instance;

	private Map<String, CustomFunction> customFunctionMap = new HashMap<>();

	public static OxyXPathHandler getInstance() {
		if (instance == null) {
			instance = new OxyXPathHandler();
		}
		return instance;
	}

	public void registerCustomFunction(CustomFunction customFunction) {
		customFunctionMap.put(customFunction.getName(), customFunction);
	}

	public String preprocessXPath(String xPath, AuthorNodeWrapper context) throws XPathException, XPathNotAvaliableException {
		Stack<XPathToken> 	xPathTokenStack = new Stack<>();
		Stack<Integer> 		stateStack 		= new Stack<>();
		StringTokenizer 	tok 			= new StringTokenizer(xPath, "'(), ", true);
		
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if (token.equals("(")) {
				// Function or Sequence
				if (stateStack.empty()) {
					stateStack.push(STATE_SEQUENCE);
					xPathTokenStack.push(new LeftBrace());
				} else if (stateStack.peek() == STATE_EXPRESSION) {
					stateStack.push(STATE_PARAMETER);
					xPathTokenStack.push(new LeftBrace());
				} else if (stateStack.peek() == STATE_CONST) {
					addString(xPathTokenStack, "(");
				} else if (stateStack.peek() == STATE_CONST_EMPTY) {
					stateStack.pop();
					stateStack.push(STATE_CONST);
					addString(xPathTokenStack, "(");
				} else if ((stateStack.peek() == STATE_SEQUENCE) || (stateStack.peek() == STATE_PARAMETER)) {
					stateStack.push(STATE_SEQUENCE);
					xPathTokenStack.push(new LeftBrace());
				}
			} else if (token.equals(")")) {
				// Function end
				// identifier + leftBrace + parameter + rightBrace = expression
				if (stateStack.isEmpty()) {
					throw new XPathException("Invalid XPath Expression ('" + xPath + "'). Unexpected token: " + token.toString());
				}
				if (stateStack.peek() == STATE_CONST) {
					addString(xPathTokenStack, ")");
					continue;
				} else if (stateStack.peek() == STATE_CONST_EMPTY) {
					stateStack.pop();
					stateStack.push(STATE_CONST);
					addString(xPathTokenStack, ")");
					continue;
				}
				ArrayList<XPathToken> expression;
				if (stateStack.peek() == STATE_EXPRESSION) {
					expression = new ArrayList<>();
					stateStack.pop();
					expression.add(xPathTokenStack.pop());
					xPathTokenStack.push(new Expression(evaluate(expression, context)));
				}
				expression = new ArrayList<>();
				if (stateStack.peek() == STATE_PARAMETER) {
					stateStack.pop();
					Stack<XPathToken> paramStack = new Stack<>();
					while (xPathTokenStack.peek().getType() != XPathToken.Type.LEFT_BRACE) {
						paramStack.push(xPathTokenStack.pop());
					}
					xPathTokenStack.pop(); // remove left brace
					if (xPathTokenStack.peek().getType() == XPathToken.Type.IDENTIFIER) {
						stateStack.pop(); // expression will be evaluated, remove state expression
						expression.add(xPathTokenStack.pop());
						expression.add(new LeftBrace()); 
						int paramCount = paramStack.size();
						for (int i = 1; i <= paramCount; i++) {
							expression.add(paramStack.pop());
						}
						expression.add(new RightBrace());
						xPathTokenStack.add(new Expression(evaluate(expression, context)));
					} else {
						String expressionString = "(";
						while (!paramStack.empty()) {
							expressionString = expressionString.concat(paramStack.pop().toString());
						}
						expressionString = expressionString.concat(")");
						xPathTokenStack.push(new Expression(expressionString));
					}
				} else if (stateStack.peek() == STATE_SEQUENCE) {
					xPathTokenStack.push(new RightBrace());
				}
			} else if (token.equals("'")) {
				// String constant
				if (stateStack.empty()) {
					stateStack.push(STATE_CONST_EMPTY);
				} else if (stateStack.peek() == STATE_CONST_EMPTY) {
					xPathTokenStack.push(new Expression("''"));
					stateStack.pop();
				} else if (stateStack.peek() == STATE_CONST) {
					stateStack.pop();
				} else {
					stateStack.push(STATE_CONST_EMPTY);
				}
			} else if (token.equals(",")) {
				// next Parameter
				if (stateStack.peek() == STATE_EXPRESSION) {
					stateStack.pop();
					ArrayList<XPathToken> expression = new ArrayList<>();
					expression.add(xPathTokenStack.pop());
					xPathTokenStack.push(new Expression(evaluate(expression, context)));
					xPathTokenStack.push(new Comma());
				} else if ((xPathTokenStack.peek().getType() == XPathToken.Type.STRING_CONSTANT) || (stateStack.peek() == STATE_SEQUENCE)){
					xPathTokenStack.push(new Comma());
				} else {
					return xPath;
				}
			} else if (token.equals(" ")) {
				if (!stateStack.empty() && stateStack.peek() == STATE_CONST) {
					addString(xPathTokenStack, token);
				} else if (!stateStack.empty() && stateStack.peek() == STATE_CONST_EMPTY) {
					addString(xPathTokenStack, token);
					stateStack.pop();
					stateStack.push(STATE_CONST);
				} else if (!xPathTokenStack.empty() && xPathTokenStack.peek().getType() == XPathToken.Type.IDENTIFIER) {
					xPathTokenStack.push(new Identifier(xPathTokenStack.pop().toString() + " "));
				} else if (!xPathTokenStack.empty() && xPathTokenStack.peek().getType() == XPathToken.Type.EXPRESSION) {
					xPathTokenStack.push(new Expression(xPathTokenStack.pop().toString() + " "));
				}
			} else {
				// Identifier/Expression or String Constant
				if (!stateStack.empty() && stateStack.peek() == STATE_CONST) {
					addString(xPathTokenStack, token);
				} else if (!stateStack.empty() && stateStack.peek() == STATE_CONST_EMPTY) {
					stateStack.pop();
					stateStack.push(STATE_CONST);
					addString(xPathTokenStack, token);
				} else if (!stateStack.empty() && stateStack.peek() == STATE_EXPRESSION) {
					// Part of expression (e.g. if .. then .. else .. )
					String expression = xPathTokenStack.pop().toString();
					expression = expression.concat(token);
					xPathTokenStack.push(new Expression(expression));
				} else {
					stateStack.push(STATE_EXPRESSION);
					xPathTokenStack.push(new Identifier(token));
				}
			}
		}
		String ergebnis = "";
		for (int i = 0; i < xPathTokenStack.size(); i++) {
			ergebnis = ergebnis + xPathTokenStack.get(i);
		}
		return ergebnis;
	}

	private static void addString(Stack<XPathToken> xPathTokenStack, String token) {
		if (!xPathTokenStack.empty() && xPathTokenStack.peek().getType() == XPathToken.Type.STRING_CONSTANT) {
			String constant = ((StringConstant) xPathTokenStack.pop()).getStringValue();
			constant = constant + token;
			xPathTokenStack.push(new StringConstant(constant));
		} else {
			xPathTokenStack.push(new StringConstant(token));
		}
	}

	private String evaluate(List<XPathToken> xPath, AuthorNodeWrapper context) throws XPathException, XPathNotAvaliableException {
		if (xPath.size() == 1) {
			return xPath.get(0).toString();
		} else if (xPath.size() > 1) {
			if (xPath.get(0).getType() == XPathToken.Type.IDENTIFIER) {
				String funcName = xPath.get(0).toString();
				CustomFunction customFunction = customFunctionMap.get(funcName);
				if (customFunction != null) {
					final ArgumentType[] argumentTypes = customFunction.getArgumentTypes();
					
					final List<Argument> arguments = new ArrayList<>();
					int index = 0;
					for (XPathToken t : xPath) {
						if ((t.getType() == XPathToken.Type.IDENTIFIER) || 
								  (t.getType() == XPathToken.Type.COMMA) || 
								  (t.getType() == XPathToken.Type.LEFT_BRACE) ||
								  (t.getType() == XPathToken.Type.RIGHT_BRACE)) {
							// ignore
						} else {
							if (index >= argumentTypes.length) {
								throw new XPathException("Too many arguments for custom Function '" + funcName + "'. " + 
										"Expected " + argumentTypes.length + " argument(s).");
							}
							final ArgumentType argumentType = argumentTypes[index];
							if (t.getType() == XPathToken.Type.STRING_CONSTANT) {
								final String string = ((StringConstant)t).getStringValue();
								if (argumentType == ArgumentType.STRING) {
									arguments.add(new Argument(string));
								} else if (argumentType == ArgumentType.STRING_LIST) {
									final List<String> list = new ArrayList<>();
									list.add(string);
									arguments.add(new Argument(list));
								} else {
									throw new XPathException("Wrong argument type for custom function '" + funcName + "'. " + 
											"String constant '" + string + "' cannot be convertedt to expected node.");
								}
							} else if (t.getType() == XPathToken.Type.EXPRESSION) {
								if (argumentType == ArgumentType.STRING) {
									arguments.add(new Argument(context.evaluateXPathToString(t.toString())));
								} else if (argumentType == ArgumentType.STRING_LIST) {
									arguments.add(new Argument(context.evaluateXPathToStringList(t.toString())));
								} else {
									final AuthorNodeWrapper node = (AuthorNodeWrapper)context.evaluateXPathToNode(t.toString());
									arguments.add(new Argument(node));
								}
							}
							++index;
						}
					}
					if (argumentTypes.length != arguments.size()) {
						throw new XPathException("Too few arguments for custom Function '" + funcName + "'. " + 
								"Expected " + argumentTypes.length + " argument(s).");
					}
					String result = customFunction.evaluate(arguments, context);
					return result;
				}
			}
		}
		String expression = "";
		for (XPathToken t : xPath) {
			expression += t.toString();
		}
		return expression;
	}
	
	public enum ArgumentType {
		STRING, STRING_LIST, NODE
	};

	public static interface CustomFunction {
		String getName();

		String evaluate(List<Argument> arguments, AuthorNodeWrapper context);

		ArgumentType[] getArgumentTypes();
	}
	
	public static class Argument {
		public final ArgumentType		type;
		public final String 			string;
		public final List<String> 		stringList;
		public final AuthorNodeWrapper	node;
		
		public Argument(String string) {
			this.type		= ArgumentType.STRING;
			this.string 	= string;
			this.stringList	= null;
			this.node		= null;		
		}
		
		public Argument(List<String> stringList) {
			this.type		= ArgumentType.STRING_LIST;
			this.string 	= null;
			this.stringList	= stringList;
			this.node		= null;		
		}
		
		public Argument(AuthorNodeWrapper node) {
			this.type		= ArgumentType.NODE;
			this.string	 	= null;
			this.stringList	= null;
			this.node		= node;		
		}
	}
}
