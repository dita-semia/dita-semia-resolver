package org.DitaSemia.Base.AdvancedKeyref;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

public class RefBasedKeyspec implements KeyspecInterface {
	
	private final String refString;

	protected boolean		isInitDone		= false;
	protected String 		type			= null;
	protected String 		key				= null;
	protected List<String> 	namespace		= null;

	
	private final static String COLON 					= ":";
	private final static String PERIOD 					= ".";
	private final static String SLASH 					= "/";
	private final static String BACKSLASH 				= "\\";
	
	private final static int 	NO_STATE 				= 0;
	private final static int 	STRING					= 1;
	private final static int	ESCAPE					= 2;
	
	
	public RefBasedKeyspec(final String refString) {
		this.refString = refString;
	}
	
	protected void init() {
		isInitDone = true;
		final String 	refString 	= getRefString();
		List<String> 	ref			= new ArrayList<>();
		Stack<String> 	tokenStack 	= new Stack<>();
		Stack<Integer>	stateStack 	= new Stack<>();		
		StringTokenizer tok 		= new StringTokenizer(refString, ":./\\", true);
		
		stateStack.push(NO_STATE);
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if (token.equals(COLON) || token.equals(PERIOD) || token.equals(SLASH)) {
				addToken(tokenStack, stateStack, token);
			} else if (token.equals(BACKSLASH)) {
				stateStack.push(ESCAPE);
			} else {
				if (stateStack.peek() == STRING) {
					String str = tokenStack.pop();
					str = str + token;
					tokenStack.push(str);
				} else {
					tokenStack.push(token);
					stateStack.push(STRING);
				}
			}
		}
		ref.addAll(tokenStack);
		String[] list = ref.toArray(new String[ref.size()]);
		
		if (list.length > 0) {
			type = list[0];
			if (list.length > 1) {
				key = list[list.length -1];
				if (list.length > 2) {
					namespace = new LinkedList<>();
					for (int i = 1; i < list.length - 1; ++i) {
						namespace.add(list[i]);
					}
				}
			}
		}
	}

	protected static void addToken(Stack<String> tokenStack, Stack<Integer> stateStack, String token) {
		if (stateStack.peek() == ESCAPE) {
			stateStack.pop();
			if (stateStack.peek() == STRING) {
				String str = tokenStack.pop();
				str = str + "\\" + token;
				tokenStack.push(str);
			} else {
				stateStack.push(STRING);
				tokenStack.push("\\" + token);
			}
		} else {
			stateStack.push(NO_STATE);
		}
	}
	
	@Override
	public String getKey() {
		if (!isInitDone) {
			init();
		}
		return key;
	}

	@Override
	public String getType() {
		if (!isInitDone) {
			init();
		}
		return type;
	}

	@Override
	public String getNamespace() {
		if (!isInitDone) {
			init();
		}
		return ((namespace == null) ? null : String.join(PATH_DELIMITER, namespace));
	}

	@Override
	public List<String> getNamespaceList() {
		if (!isInitDone) {
			init();
		}
		return namespace;
	}

	@Override
	public String getRefString() {
		return refString;
	}


	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("key = '");
		stringBuilder.append(getKey());
		stringBuilder.append("', type = '");  
		stringBuilder.append(getType());
		stringBuilder.append("' , namespace = '");
		stringBuilder.append(getNamespace());
		stringBuilder.append("'");
		return stringBuilder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof KeyspecInterface) {
			return (getRefString().equals(((KeyspecInterface)o).getRefString()));
		} else {
			return false;
		}
	}
}
