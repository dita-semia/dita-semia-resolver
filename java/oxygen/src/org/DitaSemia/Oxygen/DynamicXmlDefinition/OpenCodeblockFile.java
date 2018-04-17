package org.DitaSemia.Oxygen.DynamicXmlDefinition;

import java.awt.Frame;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.XPathNotAvaliableException;
import org.DitaSemia.Base.AdvancedKeyref.KeyDef;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Oxygen.AuthorNodeWrapper;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.xml.sax.InputSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.StringValue;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.editor.xmleditor.operations.context.RelativeInsertPosition;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.TextDocumentController;
import ro.sync.exml.workspace.api.editor.page.text.xml.TextOperationException;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextNodeRange;

public class OpenCodeblockFile implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(OpenCodeblockFile.class.getName());
	
	public static final String CODEBLOCK_FILE_SUFFIX 	= "-code.xml";
	public static final String CODEBLOCK_ID_PREFIX		= "cb";

	public static final String ATTR_ROOT_TYPE	= "root-type";
	public static final String ATTR_ROOT_NAME	= "root-name";
	

	public static final QName PARAM_ROOT_TYPE	= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "rootType");
	public static final QName PARAM_ROOT_NAME	= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "rootName");
	public static final QName PARAM_NEW_ID		= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "newId");
	public static final QName PARAM_SCHEMA		= new QName(KeyDefInterface.DXD_NAMESPACE_PREFIX, KeyDefInterface.DXD_NAMESPACE_URI, "schema");
	
	protected static final String 	XPATH_GET_ID_LIST			= "/list/codeblock/@id";	
	protected static final String 	CREATE_CODEBLOCK_XSL		= "plugin:org.dita-semia.resolver:xsl/dxd/create-codeblock.xsl";
	protected static final QName 	CREATE_CODEBLOCK_TEMPLATE	= new QName("create-codeblock");
	
	@Override
	public String getDescription() {
		return "Open a DXD codeblock file.";
	}
	

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws IllegalArgumentException, AuthorOperationException {

		boolean isModified 	= authorAccess.getEditorAccess().isModified();
		
		try {
			final AuthorNode dxdCodeblockNode = getDxdCodeblockNode(authorAccess);
			if (dxdCodeblockNode != null) {

				final AuthorNodeWrapper nodeWrapper = new AuthorNodeWrapper(dxdCodeblockNode, authorAccess);
				final String rootType = nodeWrapper.evaluateXPathToString(nodeWrapper.getAttribute(ATTR_ROOT_TYPE, KeyDef.DXD_NAMESPACE_URI));
				final String rootName = nodeWrapper.evaluateXPathToString(nodeWrapper.getAttribute(ATTR_ROOT_NAME, KeyDef.DXD_NAMESPACE_URI));
				
				final PluginWorkspace 				workspace 	= PluginWorkspaceProvider.getPluginWorkspace();
				final OxygenDxdCodeblockResolver 	resolver 	= OxygenDxdCodeblockResolver.getInstance();
				
				String filename	= null;
				String id		= null;
				
				final String oldCoderef = OxygenDxdCodeblockResolver.getCoderef(dxdCodeblockNode);
				if ((oldCoderef != null) && (oldCoderef.contains(DitaUtil.HREF_URL_ID_DELIMITER))) {
					final int splitPos = oldCoderef.indexOf(DitaUtil.HREF_URL_ID_DELIMITER);
					filename 	= oldCoderef.substring(0, splitPos);
					id			= oldCoderef.substring(splitPos + 1);
				} else {
					filename = FilenameUtils.getBaseName(dxdCodeblockNode.getXMLBaseURL().getPath()) + CODEBLOCK_FILE_SUFFIX;
				}
				//logger.info("filename: '" + filename + "'");
				
				Source source = resolver.getURIResolver().resolve(filename, dxdCodeblockNode.getXMLBaseURL().toExternalForm());
				final URL url = new URL(source.getSystemId());
				//logger.info("url: '" + url + "'");
				
				
				WSEditor editor = workspace.getEditorAccess(url, PluginWorkspace.MAIN_EDITING_AREA);	
				if (editor == null) {
					// file is not opened, yet
					if (!FileUtil.fileUrlExists(url)) {
						if (id == null) {
							id = CODEBLOCK_ID_PREFIX + "1";
						}
						createNewCodeFile(url, dxdCodeblockNode, authorAccess, rootType, rootName);
					}
				}

				// open in in any case to ensure the it is visible
				PluginWorkspaceProvider.getPluginWorkspace().open(url, EditorPageConstants.PAGE_TEXT);
				
				if (editor == null) {
					editor = workspace.getEditorAccess(url, PluginWorkspace.MAIN_EDITING_AREA);
					if (editor == null) {
						throw new Exception("Failed to get editor access");
					}
					
					source = new SAXSource(new InputSource(editor.createContentInputStream()));
					//logger.info("create source from editor.");
				}
				
				id = handleExistingFile(source, dxdCodeblockNode, authorAccess, resolver, rootType, rootName, id, editor);
				
				final String newCoderef = filename + DitaUtil.HREF_URL_ID_DELIMITER + id;
				if (!newCoderef.equals(oldCoderef)) {
					//logger.info("set coderef attribute: '" + newCoderef + "'");
					authorAccess.getDocumentController().setAttribute(OxygenDxdCodeblockResolver.ATTR_CODEREF, new AttrValue(newCoderef), (AuthorElement)dxdCodeblockNode);
					isModified = true;
				}
				
				authorAccess.getDocumentController().refreshNodeReferences(dxdCodeblockNode);

				jumpToCodeblock(editor, id);
			}

		} catch (Exception e) {
			logger.error(e, e);

            JOptionPane.showMessageDialog(
            		(Frame)PluginWorkspaceProvider.getPluginWorkspace().getParentFrame(),
            		e.getMessage(),
            	    "Open DXD codeblock file",
            	    JOptionPane.ERROR_MESSAGE);
		}
		if (!isModified) {
			authorAccess.getEditorAccess().setModified(false);
		}
	}


	protected AuthorNode getDxdCodeblockNode(AuthorAccess authorAccess) throws BadLocationException {
		final AuthorDocumentController	documentController	= authorAccess.getDocumentController();
		final int 						caretOffset 		= authorAccess.getEditorAccess().getCaretOffset();
		final AuthorNode				nodeAtCaret 		= documentController.getNodeAtOffset(caretOffset);

		AuthorNode 	dxdCodeblockNode 	= nodeAtCaret;
		if (!OxygenDxdCodeblockResolver.isDxdCodeblock(dxdCodeblockNode)) {
			// check if the parent node is a DXD codeblock
			dxdCodeblockNode 	= nodeAtCaret.getParent();
			if (!OxygenDxdCodeblockResolver.isDxdCodeblock(dxdCodeblockNode)) { 
				dxdCodeblockNode = null;
			}
		}
		return dxdCodeblockNode;
	}
	
	
	protected void createNewCodeFile(URL url, AuthorNode dxdCodeblockNode, AuthorAccess authorAccess, String rootType, String rootName) throws FileNotFoundException {
		final PrintWriter writer = new PrintWriter(url.getFile());
		
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		writer.println("      xsi:noNamespaceSchemaLocation=\"urn:ds-dxd;rootType=" + rootType + ";rootName=" + rootName + "!/urn:dxd-dummy\">");
		writer.println("</list>");
		
		writer.close();
	}
	
	
	protected String handleExistingFile(
			Source 						source, 
			AuthorNode 					dxdCodeblockNode, 
			AuthorAccess 				authorAccess, 
			OxygenDxdCodeblockResolver 	resolver, 
			String 						rootType, 
			String 						rootName, 
			String 						id, 
			WSEditor 					editor) throws ro.sync.exml.workspace.api.editor.page.text.xml.XPathException, TextOperationException, XPathNotAvaliableException, SaxonApiException, TransformerException  {

		final WSEditorPage currentPage = editor.getCurrentPage();
		if (currentPage instanceof WSXMLTextEditorPage) {
			final WSXMLTextEditorPage 		xmlTP 	= (WSXMLTextEditorPage)currentPage;
			final TextDocumentController	dc		= xmlTP.getDocumentController();
			
			final Object[] 		idObjList 	= xmlTP.evaluateXPath(XPATH_GET_ID_LIST);
			//logger.info("idObjList: " + idObjList + ", " + idObjList.length);
			final Set<String>	idSet		= new HashSet<>();
			if (idObjList != null) {
				for (int i = 0; i < idObjList.length; ++i) {
					final Attr idAttr = (Attr)idObjList[i];
					//logger.info("existing codeblock id: '" + idAttr.getValue() + "'");
					idSet.add(idAttr.getValue());
				}
			}

			//logger.info("id: '" + id + "', existent: " + idSet.contains(id));
			if (!idSet.contains(id)) {

				if (id == null) {
					// find 1st id not already existing...
					int counter = 1;
					do {
						id = CODEBLOCK_ID_PREFIX + Integer.toString(counter++);
					} while (idSet.contains(id));
				}
				
				final String codeblock = createNewCodeblock(dxdCodeblockNode, resolver, rootType, rootName, id);
				//logger.info("codeblock: '" + codeblock + "'");
				dc.insertXMLFragment(codeblock, "/list", RelativeInsertPosition.INSERT_LOCATION_AS_LAST_CHILD);
			}
		}
		return id;
	}
	
	
	protected String createNewCodeblock(
			AuthorNode 					dxdCodeblockNode, 
			OxygenDxdCodeblockResolver 	resolver, 
			String 						rootType, 
			String 						rootName, 
			String 						newId) throws XPathNotAvaliableException, SaxonApiException, TransformerException {

		final XsltExecutable 	executable 	= resolver.getTransformerCache().getExecutable(CREATE_CODEBLOCK_XSL);
		final XsltTransformer 	transformer = executable.load();
		
		transformer.setParameter(PARAM_ROOT_TYPE, 	XdmValue.wrap(new StringValue(rootType)));
		transformer.setParameter(PARAM_ROOT_NAME,	XdmValue.wrap(new StringValue(rootName)));
		transformer.setParameter(PARAM_NEW_ID,		XdmValue.wrap(new StringValue(newId)));
		transformer.setParameter(PARAM_SCHEMA,		createSchema(dxdCodeblockNode, resolver, rootType, rootName));

		transformer.setInitialTemplate(CREATE_CODEBLOCK_TEMPLATE);

		final Processor		processor	= new Processor(resolver.getTransformerCache().getConfiguration());
		final StringWriter 	writer 		= new StringWriter();
		final Serializer 	serializer	= processor.newSerializer(writer);
		
		serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, 	"yes");
		serializer.setOutputProperty(Serializer.Property.INDENT, 				"yes");
		serializer.setOutputProperty(Serializer.Property.SAXON_LINE_LENGTH, 	"1000");
		
		transformer.setDestination(serializer);
		transformer.transform();
		writer.flush();
		
		//logger.info("new codeblock: ");
		//logger.info(writer.toString());
		
		return writer.toString();//.replaceAll("\\s+$", "");	// remove white spaces at the end
	}
	
	protected XdmNode createSchema(AuthorNode dxdCodeblockNode, OxygenDxdCodeblockResolver resolver, String rootType, String rootName) throws TransformerException, SaxonApiException {
		
		final XsltExecutable 		executable 			= resolver.getTransformerCache().getExecutable(CreateXsd.XSL_URI);
		final XsltTransformer 		xslTransformer 		= executable.load();

		xslTransformer.setParameter(CreateXsd.PARAM_ROOT_TYPE, 	XdmValue.wrap(new StringValue(rootType)));
		xslTransformer.setParameter(CreateXsd.PARAM_ROOT_NAME, 	XdmValue.wrap(new StringValue(rootName)));
		final URL mapUrl = BookCacheHandler.getInstance().getCurrMapUrl();
		if (mapUrl != null) {
			xslTransformer.setParameter(CreateXsd.PARAM_MAP_URL, 		XdmValue.wrap(new AnyURIValue(mapUrl.toString())));
		}

		final Source 	inputSource = resolver.getURIResolver().resolve(CreateXsd.INPUT_URI, dxdCodeblockNode.getXMLBaseURL().getFile());
		final XdmNode 	inputNode	= BookCacheHandler.getInstance().getDocumentBuilder().build(inputSource, false, false);		
		xslTransformer.setInitialContextNode(inputNode);

		final XdmDestination destination = new XdmDestination();
		xslTransformer.setDestination(destination);

		xslTransformer.transform();
		
		return destination.getXdmNode();
	}
	
	protected void jumpToCodeblock(WSEditor editor, String id) {
		final WSEditorPage currentPage = editor.getCurrentPage();
		if (currentPage instanceof WSXMLTextEditorPage) {
			try {
				final WSXMLTextEditorPage 	xmlTP 	= (WSXMLTextEditorPage)currentPage;
				
				// look for element within codeblock
				WSXMLTextNodeRange[] ranges = xmlTP.findElementsByXPath("/list/codeblock[@id='" + id + "']/*");
				if ((ranges != null) && (ranges.length > 0)) {
					
					// select content element
					final int startOffset 	= xmlTP.getOffsetOfLineStart(ranges[0].getStartLine()) 	+ ranges[0].getStartColumn() 	- 1;
					final int endOffset 	= xmlTP.getOffsetOfLineStart(ranges[0].getEndLine()) 	+ ranges[0].getEndColumn() 		- 1;

					xmlTP.setCaretPosition(startOffset);
					xmlTP.select(startOffset, endOffset);
					xmlTP.scrollCaretToVisible();
					
				} else {
					
					// look for codeblock itself
					ranges = xmlTP.findElementsByXPath("/list/codeblock[@id='" + id + "']");
					if ((ranges != null) && (ranges.length > 0)) {
						// place cursor after codeblock start-tag
						final int startOffset 	= xmlTP.getOffsetOfLineStart(ranges[0].getStartLine()) 	+ ranges[0].getStartColumn();
						xmlTP.setCaretPosition(startOffset + ("<codeblock id='" + id + "'>").length() - 1);
						xmlTP.scrollCaretToVisible();
					}
					
				}
			} catch (ro.sync.exml.workspace.api.editor.page.text.xml.XPathException | BadLocationException e) {
				logger.error(e, e);
			}
		}
	}
	
	protected void selectTextRange(WSXMLTextEditorPage 	xmlTP, WSXMLTextNodeRange range) throws BadLocationException {
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
