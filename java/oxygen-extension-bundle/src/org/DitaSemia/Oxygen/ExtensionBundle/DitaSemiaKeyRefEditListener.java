package org.DitaSemia.Oxygen.ExtensionBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.text.BadLocationException;

import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyRef;
import org.DitaSemia.Base.DocumentCaching.BookCache;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.DitaSemia.Oxygen.OxyUtil;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorDocumentFilter;
import ro.sync.ecss.extensions.api.AuthorDocumentFilterBypass;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class DitaSemiaKeyRefEditListener extends AuthorDocumentFilter {

	private static final Logger logger = Logger.getLogger(DitaSemiaKeyRefEditListener.class.getName());
	
	private final AuthorAccess 				authorAccess;
	private final AuthorDocumentController 	documentController;

	private final static int PRIORITY_MATCHING_KEY					= 20;
	private final static int PRIORITY_MATCHING_TYPE					= 10;
	private final static int PRIORITY_MATCHING_NAMESPACE_ELEMENT	= 2;
	
	private final static int 	NO_STATE 				= 0;
	private final static int 	STRING					= 1;
	
	private final static String PERIOD 					= ".";
	private final static String SLASH 					= "/";
	
	private String 			previousText 	= null;
	private String 			delimiter		= null;
	private AuthorElement 	keyRefElement	= null;
	private KeyRef			keyRef			= null;
	
	public DitaSemiaKeyRefEditListener(AuthorAccess authorAccess) {
		this.authorAccess = authorAccess;
		this.documentController = authorAccess.getDocumentController();
	}
	
	@Override
	public boolean delete(AuthorDocumentFilterBypass filterBypass, int startOffset, int endOffset, boolean backspace) {
		documentController.beginCompoundEdit();
		try {
			AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			AuthorNode					nodeAtOffset		= documentController.getNodeAtOffset(startOffset);
			AuthorElement 				keyRefElement 		= (AuthorElement) nodeAtOffset;
			final KeyRef 				keyRef 				= KeyRef.fromNode(new AuthorNodeWrapper(nodeAtOffset, authorAccess));
			if (keyRef != null) {
				previousText = keyRef.getText();
			}
			boolean result = super.delete(filterBypass, startOffset, endOffset, backspace);
			
			if(result){
				if (keyRef != null) {
					String deletedText = documentController.getFilteredText(startOffset, (endOffset - startOffset));
					updateKeyRef(keyRef, keyRefElement, false, deletedText);			
				}
				reset();
			}
			return true;
		} catch (Exception e) {
			logger.error(e, e);
			return false;
		} finally {
			documentController.endCompoundEdit();
		}
	}

	@Override
	public void insertFragment(AuthorDocumentFilterBypass filterBypass, int offset, AuthorDocumentFragment frag) {
		documentController.beginCompoundEdit();
		// for copy & paste 
		try {
			String toInsert	= frag.getContent().getString(0, frag.getContent().getLength());
			checkConditions(offset);
			
			super.insertFragment(filterBypass, offset, frag);
			
			updateKeyRef(toInsert);
		} catch (BadLocationException e) {
			logger.error(e, e);
		} finally {
			documentController.endCompoundEdit();
		}
	}
	
	@Override
	public void surroundInFragment(AuthorDocumentFilterBypass filterBypass, AuthorDocumentFragment frag, int startOffset, int endOffset)
			throws AuthorOperationException {
		documentController.beginCompoundEdit();
		//for wrapping selected text with key-xref element
		try {
			String toInsert	= frag.getContent().getString(0, frag.getContent().getLength());
			super.surroundInFragment(filterBypass, frag, startOffset, endOffset);
			AuthorNode nodeAtOffset	= authorAccess.getDocumentController().getNodeAtOffset(startOffset + 1);
			keyRefElement 			= (AuthorElement) nodeAtOffset;
			AuthorNodeWrapper node  = new AuthorNodeWrapper(nodeAtOffset, authorAccess);
			keyRef 					= KeyRef.fromNode(node);
			
			if (keyRef!= null) {
				previousText = keyRef.getText();
			}
			updateKeyRef(toInsert);
		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			documentController.endCompoundEdit();
		}
	}
	
	@Override
	public void insertText(AuthorDocumentFilterBypass filterBypass, int offset, String toInsert) {
		documentController.beginCompoundEdit();
		try {
			checkConditions(offset);
			
			super.insertText(filterBypass, offset, toInsert);
			
			updateKeyRef(toInsert);
		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			documentController.endCompoundEdit();
		}
	}
	
	private void checkConditions(int offset) {
		try {
			AuthorNode nodeAtOffset	= documentController.getNodeAtOffset(offset);
			keyRefElement 			= (AuthorElement) nodeAtOffset;
			AuthorNodeWrapper node  = new AuthorNodeWrapper(nodeAtOffset, authorAccess);
			keyRef 					= KeyRef.fromNode(node);
			
			if (keyRef!= null) {
				previousText = keyRef.getText();
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	private void updateKeyRef(String toInsert) {
		if (keyRef != null) {
			updateKeyRef(keyRef, keyRefElement, true, toInsert);			
		}
		reset();
	}
	
	private void updateKeyRef(KeyRef keyRef, AuthorElement keyRefElement, boolean isInserted, String fragment) {
		final List<KeyDefInterface> keyDefs = findMatchingKeyDefs(keyRef, keyRefElement);
		String ref = null;
		//logger.info("matching keyDefs: " + keyDefs.size());
		if (keyDefs.size() == 1) {
			ref = keyDefs.get(0).getRefString();
		} else {
			String type = keyRef.getType();

			final Set<String> typeFilter = keyRef.getTypeFilter();
			if ((typeFilter != null) && (typeFilter.size() == 1)) {
				type = typeFilter.iterator().next();
			} else if ((type == null) || (type.isEmpty())) {
				type = KeyRef.TYPE_UNDEFINED;
			}
			
			final KeyDefListInterface keyDefList = BookCacheHandler.getInstance().getBookCache(keyRefElement.getXMLBaseURL());
			if (keyDefList != null) {
				delimiter = keyDefList.getKeyTypeDef(type).getPathDelimiter();
			} 
			
			final List<String>	invisibleNS = saveInvisibleNamespace(keyRef.getNamespaceList(), previousText);
			final List<String>	splitText	= splitText(keyRef.getText());
			final List<String> 	namespace 	= handleNamespace(invisibleNS, splitText);
			final String		key			= "*";//(splitText.isEmpty() ? "" : splitText.get(splitText.size() - 1));
			ref = KeyDef.createRefString(type, namespace, key);
		}
		
		final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
		OxyUtil.setAttribute(documentController, keyRefElement, KeyRef.NAMESPACE_URI, KeyRef.NAMESPACE_PREFIX, KeyRef.ATTR_REF, ref);
	}
	
	private List<String> splitText(String text) {
		List<String> 	splitText 	= new ArrayList<>();
		Stack<String> 	tokenStack 	= new Stack<>();
		Stack<Integer>	stateStack 	= new Stack<>();
		StringTokenizer tok 		= new StringTokenizer(text, "./", true);
		stateStack.push(NO_STATE);
		
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if (token.equals(delimiter)) {
				stateStack.push(NO_STATE);
			} else if (token.equals(PERIOD) || token.equals(SLASH)) {
				if (!delimiter.equals(PERIOD) && token.equals(PERIOD)) {
					addToken(tokenStack, stateStack, token);
				} else if (!delimiter.equals(SLASH) && token.equals(SLASH)) {
					addToken(tokenStack, stateStack, token);
				} 
			} else {
				addToken(tokenStack, stateStack, token);
			}
		}
		splitText.addAll(tokenStack);
		return splitText;
	}
	
	private static void addToken(Stack<String> tokenStack, Stack<Integer> stateStack, String token) {
		if (stateStack.peek() == STRING) {
			String str = tokenStack.pop();
			str = str + token;
			tokenStack.push(str);
		} else {
			tokenStack.push(token);
			stateStack.push(STRING);
		}
	}
	
	private List<String> saveInvisibleNamespace(List<String> namespaceList, String previousText) {
		List<String> split = splitText(previousText);
		int i = 0;
		if (namespaceList != null && namespaceList.size() > 0) {
			for (String s : namespaceList) {
				if (split != null && split.size() > 0 && s.equals(split.get(0))) {
					break;
				}
				i++;
			}
			while (namespaceList.size() > i) {
				namespaceList.remove(i);
			}
		}
		return namespaceList;
	}

	private List<String> handleNamespace(List<String> invisibleNS, List<String> textSplit) {
		List<String> namespace = new ArrayList<>();
		if (invisibleNS != null) {
			namespace.addAll(invisibleNS);
		}
		namespace.addAll(textSplit);
		//remove key
		if (!namespace.isEmpty() && !textSplit.isEmpty()) {
			namespace.remove(namespace.size() - 1);
		}
		return namespace;
	}

	private List<KeyDefInterface> findMatchingKeyDefs(KeyRef keyRef, AuthorNode node) {
		final BookCacheHandler 				cacheHandler 	= BookCacheHandler.getInstance();
		final BookCache 					bookCache 		= cacheHandler.getBookCache(node.getXMLBaseURL());
		final LinkedList<KeyDefInterface> 	matchingList	= new LinkedList<>();
		
		final List<String> 					namespace 	= keyRef.getNamespaceList();
		final String						type		= keyRef.getType();
		
		Collection<KeyDefInterface> 		keyDefList 	= null;
		if (bookCache != null) {
			keyDefList 	= bookCache.getKeyDefListByText(keyRef.getText());
			//logger.info("findMatchingKeyDefs keyDefList-size: " + ((keyDefList != null) ? keyDefList.size() : 0));
		}
		
		if ((keyDefList != null) && (!keyDefList.isEmpty())) {
			int maxPriority = -1;
			for (KeyDefInterface k : keyDefList) {
				int priority = 0;
				if ((k.matchesNamespaceFilter(keyRef.getNamespaceFilter())) && (k.matchesTypeFilter(keyRef.getTypeFilter()))) {
					if (k.getKey().equals(keyRef.getKey())) {
						priority += PRIORITY_MATCHING_KEY;
					}
					
					if (k.getType().equals(type)) {
						priority += PRIORITY_MATCHING_TYPE;
					}
					
					int i = k.getMatchingElementsCount(namespace);
					priority += (i * PRIORITY_MATCHING_NAMESPACE_ELEMENT);
					
					if (priority > maxPriority) {
						matchingList.clear();
						matchingList.add(k);
						maxPriority = priority;
					} else if (priority == maxPriority) {
						// ambiguous
						matchingList.add(k);
					}
					
				}
			}
		}
		return matchingList;
	}
	
	private void reset() {
		previousText	= null;
		delimiter 		= null;
	}
}
