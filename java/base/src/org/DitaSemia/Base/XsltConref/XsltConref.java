/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.Base.XsltConref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.DitaSemia.Base.DitaUtil;
import org.DitaSemia.Base.EmbeddedXPathResolver;
import org.DitaSemia.Base.FileUtil;
import org.DitaSemia.Base.NodeWrapper;
import org.DitaSemia.Base.SaxonNodeWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.BuildingStreamWriterImpl;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltExecutable.ParameterDetails;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.StringValue;

public class XsltConref {
	
	private static final Logger logger = Logger.getLogger(XsltConref.class.getName());
	
	public static final String 	ATTR_URI 					= "xsl";
	public static final String 	ATTR_SOURCE_URI				= "source";
	public static final String 	ATTR_SOURCE_TYPE			= "source-type";
	public static final String 	ATTR_START_TEMPLATE			= "start-template";
	public static final String 	ATTR_STAGE					= "stage";
	public static final String 	ATTR_FLAGS					= "flags";
	

	public static final String 	FLAG_REPARSE				= "reparse";
	public static final String 	FLAG_SINGLE_SOURCE			= "single-source";
	public static final String 	FLAG_COPY					= "copy";
	
	public static final int		STAGE_DISPLAY				= -1;
	public static final int		STAGE_IMMEDIATELY			= 0;
	public static final int		STAGE_DELAYED				= 1;
	
	public static final String	YES							= "yes";
	
	public static final String 	PARAM_CURRENT_NODE			= "current";
	public static final String 	PARAM_CURRENT_URI			= "current-uri";
	public static final String 	PARAM_CURRENT_NAME			= "current-name";
	public static final String 	PARAM_CURRENT_CLASS			= "current-class";
	public static final String 	PARAM_CURRENT_XPATH			= "current-xpath";
	public static final String 	PARAM_FLAGS					= "flags";
	public static final String 	PARAM_CURRENT_MAP_URI		= "current-map-uri";
	public static final String 	NAMESPACE_PARAMETER_URI		= "http://www.dita-semia.org/xslt-conref/custom-parameter";
	public static final String 	NAMESPACE_URI				= "http://www.dita-semia.org/xslt-conref";
	public static final String 	NAMESPACE_PARAMETER_PREFIX	= "xcp";
	public static final String 	NAMESPACE_PREFIX			= "xcr";

	public static final String 	NAME_NO_CONTENT				= "no-content";
	
	public static final String 	EMPTY_SOURCE_XML			= "<?xml version=\"1.0\"?><dummy/>";
	
	public static final String  XML_TAG_ROOT				= "root";
	public static final String  XML_TAG_ROW					= "row";
	public static final String  XML_TAG_COLUMN				= "column";
	
	public static final String 	SOURCE_TYPE_XML				= "xml";
	public static final String 	SOURCE_TYPE_EXCEL			= "excel";
	public static final String 	SOURCE_TYPE_TEXT			= "text";
	public static final String 	SOURCE_TYPE_CSV				= "csv";
	
	protected final NodeWrapper 			node;
	protected final XsltConrefCache			xsltConrefCache;
	protected final	URL 					baseUrl;				

	
	public static class Parameter {
		
		protected final QName 		name;
		protected final Sequence 	value;
		
		public Parameter(QName name, Sequence value) {
			this.name	= name;
			this.value	= value;
		}
	}

	public static XsltConref fromNode(NodeWrapper node, XsltConrefCache xsltConrefCache, boolean acceptCopy)	{
		if (isXsltConref(node, acceptCopy)) {
			return new XsltConref(node, xsltConrefCache);
		}
		return null;
	}

	
	public static boolean isXsltConref(NodeWrapper node, boolean acceptCopy) {
		if ((node != null) && (node.isElement())) {
			final String uriAttr =  node.getAttribute(ATTR_URI, NAMESPACE_URI);
			if ((uriAttr != null) && (!uriAttr.isEmpty())) {
				if (acceptCopy) {
					return true;
				} else {
					final String flagsAttr = node.getAttribute(ATTR_FLAGS, NAMESPACE_URI);
					return ((flagsAttr == null) || (!flagsAttr.contains(FLAG_COPY)));
				}
			}
		}
		return false;
	}
	
	
	private XsltConref(NodeWrapper node, XsltConrefCache xsltConrefCache) {
		this.node 				= node;
		this.xsltConrefCache	= xsltConrefCache;
		this.baseUrl			= FileUtil.getFixedBaseUrl(node, xsltConrefCache.getBaseDir());
	}
	
	
	/*public void setBaseUrl(final URL baseUrl) {
		this.baseUrl = baseUrl;
	}*/
	
	public String resolveToString(Collection<Parameter> frameworkParameters) throws XPathException, TempContextException {
		//logger.info("resolveToString: " + getScriptName());
		final NodeInfo 	resolvedNode 	= resolve(frameworkParameters);
		final String 	resolvedString 	= SaxonNodeWrapper.serializeNode(resolvedNode);
		//logger.info(resolvedString);
		return resolvedString;
	}

	public NodeInfo resolveToNode(Collection<Parameter> frameworkParameters) throws XPathException, TempContextException {
		//logger.info("resolveToNode: " + getScriptName());
		final NodeInfo 	resolvedNode 	= resolve(frameworkParameters);
		NodeInfo 		resolvedElement = resolvedNode.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
		//logger.info("needsReparse: " + XsltConref.needsReparse(resolvedElement) + ", class: " + resolvedElement.getAttributeValue("", DitaUtil.ATTR_CLASS) + ", name: " + resolvedElement.getDisplayName());
		if (XsltConref.needsReparse(resolvedElement)) {
			final String 	serialized 		= SaxonNodeWrapper.serializeNode(resolvedNode);
			try {
				//logger.info("reparsing...");
				//logger.info(serialized);
				final XdmNode 	reparsedNode 	= xsltConrefCache.getDocumentBuilder().buildFromString(serialized, true, true);
				
				resolvedElement = reparsedNode.getUnderlyingNode().iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT).next();
				//logger.info("---");
				//logger.info(SaxonNodeWrapper.serializeNode(reparsedElement));
			} catch (SaxonApiException e) {
				logger.info("------");
				logger.info("serialized node:");
				logger.info(serialized);
				logger.info("------");
				throw new XPathException("Failed to reparse resolved xslt-conref ('" + getScriptName() + "'): " + e.getMessage());
			}
		} 
		resolvedElement.setSystemId(node.getBaseUrl().toString());
		return resolvedElement;
	}
	
	protected NodeInfo resolve(Collection<Parameter> frameworkParameters) throws XPathException, TempContextException {
		//logger.info("resolve()");
		final Source 	scriptSource 	= getScriptSource();
		String 			sourceType		= getSourceType();
		final XsltExecutable 	xsltExecutable 	= xsltConrefCache.getTransformerCache().getExecutable(scriptSource);
		final XsltTransformer 	xsltTransformer = xsltExecutable.load();
		//logger.info("xsltTransformer: " + xsltTransformer);
		
		if (getStartTemplate() != null) {
			try {
				xsltTransformer.setInitialTemplate(new QName(getStartTemplate()));
			} catch (SaxonApiException e) {
				throw new XPathException(e.getMessage());
			}
		} else {
			switch (sourceType) {
				case SOURCE_TYPE_XML:
					initXmlSource(xsltTransformer);
					break;
				case SOURCE_TYPE_EXCEL:
					initExcelSource(xsltTransformer);
					break;
				case SOURCE_TYPE_TEXT:
					initTextSource(xsltTransformer);
					break;
				case SOURCE_TYPE_CSV:
					initCsvSource(xsltTransformer);
					break;
				default:
					throw new XPathException("invalid value for source-type attribute ('" + sourceType + "')");
			}
		}
		
		// set xcr:current-uri
		xsltTransformer.setParameter(
				new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_URI), 
				XdmValue.wrap(new AnyURIValue(baseUrl.toString())));
		
		// set xcr:current-name
		xsltTransformer.setParameter(
				new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_NAME), 
				XdmValue.wrap(new StringValue(node.getName())));
		
		// set xcr:current-class
		xsltTransformer.setParameter(
				new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_CLASS), 
				XdmValue.wrap(new StringValue(node.getAttribute(DitaUtil.ATTR_CLASS, null))));
		
		// set xcr:flags
		xsltTransformer.setParameter(
				new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_FLAGS), 
				XdmValue.wrap(new StringValue(node.getAttribute(ATTR_FLAGS, NAMESPACE_URI))));

		
		// set xcr:current-map-uri
		final URL mapUrl = xsltConrefCache.getBookCacheProvider().getBookCacheRootUrl(baseUrl);
		if (mapUrl != null) {
			xsltTransformer.setParameter(
					new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_MAP_URI), 
					XdmValue.wrap(new AnyURIValue(mapUrl.toString())));
		}
		
		setCustomParameters(xsltTransformer);
		
		// set framework parameters
		if (frameworkParameters != null) {
			for (Parameter parameter : frameworkParameters) {
				xsltTransformer.setParameter(parameter.name, XdmValue.wrap(parameter.value));	
			}
		}

		//logger.info("scriptUrl: " + scriptUrl);
		try {
			
			final XdmDestination destination = new XdmDestination();
			xsltTransformer.setDestination(destination);
			xsltTransformer.transform();
			final NodeInfo resolved = destination.getXdmNode().getUnderlyingNode();
			resolved.setSystemId(FileUtil.decodeUrl(node.getBaseUrl()));
			return resolved;
			
		} catch (SaxonApiException e) {
			throw new XPathException("Runtime Error. " + e.getMessage());
		} 
	}
	
	private void initXmlSource(XsltTransformer xsltTransformer) throws XPathException, TempContextException {
		Source 			xmlSource 	= getXmlSource();
		final NodeInfo 	nodeInfo 	= (node instanceof SaxonNodeWrapper) ? ((SaxonNodeWrapper)node).getNodeInfo() : null; 
		if ((xmlSource == null) && 
				(nodeInfo != null) &&
				(nodeInfo.getConfiguration().isCompatible(xsltConrefCache.getConfiguration())) &&
				(nodeInfo.getConfiguration().getClass().equals(xsltConrefCache.getConfiguration().getClass()))) {
			//logger.info("Source is root.");
			
			// use current root document as input
			xsltTransformer.setInitialContextNode(new XdmNode(nodeInfo.getRoot()));
			
			// set "xcr:current"
			xsltTransformer.setParameter(new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_NODE), new XdmNode(nodeInfo));
			
			// set "xcr:current-xpath"
			xsltTransformer.setParameter(new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_XPATH), XdmValue.wrap(new StringValue(createXPathToElement(node))));
			
		} else {
			boolean xmlSourceIsBaseUrl	= false;
			if (xmlSource == null) {
				//logger.info("Source is base uri.");
				
				// 	use current base uri as input
				final String baseUrl = node.getBaseUrl().toExternalForm();
				try {
					xmlSource 			= xsltConrefCache.getUriResolver().resolve(baseUrl, "");
					xmlSourceIsBaseUrl 	= true;
				} catch (TransformerException e) {
					throw new XPathException("Error resolving the source URL: '" + FileUtil.decodeUrl(baseUrl) + "': " + e.getMessage());
				}
			}/*
				// don't check file existence here since the source might be not from file...
			 else if ((xmlSource.getSystemId() != null) && (!FileUtil.fileExists(xmlSource.getSystemId()))) {
				// dedicated error message for this scenario
				throw new XPathException("Input source could not be found. (URL: '" + FileUtil.decodeUrl(xmlSource.getSystemId()) + "')");
			}*/
			
			try {
				final XdmNode context = xsltConrefCache.getDocumentBuilder().build(xmlSource, true, true);
	
				if (xmlSourceIsBaseUrl) {
					// set "xcr:current"
					final String xPathString	= createXPathToElement(node);
					
					xsltTransformer.setParameter(new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_XPATH), XdmValue.wrap(new StringValue(xPathString)));
					
					//logger.info("XPath to Element: " + xPathString);
					final XPathSelector  	xPathSel	= xsltConrefCache.getXPathCache().getXPathSelector(xPathString, context);
					try {
						xPathSel.setContextItem(context);
						final XdmItem contentNode = xPathSel.evaluateSingle();
						xsltTransformer.setParameter(new QName(NAMESPACE_PREFIX, NAMESPACE_URI, PARAM_CURRENT_NODE), contentNode);
						//logger.info("Element: " + contentNode);
					} catch (SaxonApiException e) {
						throw new XPathException(e.getMessage(), e);
					}
				}
				
				xsltTransformer.setInitialContextNode(context);
			} catch (SaxonApiException e) {
				/* try to read input with standard configuration to print it */ 
				final Processor 		processor 	= new Processor(new Configuration());
				final DocumentBuilder 	builder 	= processor.newDocumentBuilder();
				try {
					final XdmNode node	= builder.build(xmlSource);
					//logger.error("Input: " + SaxonNodeWrapper.serializeNode(node.getUnderlyingNode()));
				} catch (SaxonApiException e1) {
					// keep original message
				}
				Throwable[] suppressed =  e.getSuppressed();
				if (suppressed.length > 0) {
					logger.error("Suppresses exceptions:");
					for (int i = 0; i < suppressed.length; ++i) {
						logger.error(" - " + suppressed[i].getMessage());
					}
				}
				logger.error(e, e);
				throw new XPathException("Error reading input source ('" + FileUtil.decodeUrl(xmlSource.getSystemId()) + "'): " + e.getMessage());
			}
		}
	}
	
	private void initExcelSource(XsltTransformer xsltTransformer) throws XPathException {
		File excelFile = null;
		try {
			excelFile = new File(new URI(getXmlSourceUrl().toExternalForm()));
		} catch (URISyntaxException e1) {
			throw new XPathException("Input source could not be found. (URL: '" + getXmlSourceUrl().toExternalForm() + "')");
		}
		if (!excelFile.exists()) {
			throw new XPathException("Input source could not be found. (URL: '" + excelFile.getAbsolutePath() + "')");
		} else {
			final Processor 				processor 	= new Processor(xsltConrefCache.getConfiguration());
			final DocumentBuilder 			builder 	= processor.newDocumentBuilder();
			Workbook						workbook	= null;
			FormulaEvaluator 				evaluator	= null;
			DataFormatter 					formatter 	= null;
			BuildingStreamWriterImpl 		writer 		= null;
			
			try {
				
				writer = builder.newBuildingStreamWriter();
				writer.writeStartDocument();
				writer.writeStartElement("root");
				
		        try (FileInputStream fis = new FileInputStream(excelFile)) {
		            workbook 	= WorkbookFactory.create(fis);
		            evaluator 	= workbook.getCreationHelper().createFormulaEvaluator();
		            formatter 	= new DataFormatter(true);
		        } catch (Exception e) {
		        	logger.error(e, e);
		        	throw new XPathException("Error reading input source ('" + FileUtil.decodeUrl(excelFile.getAbsolutePath()) + "'): " + e.getMessage());
		        }
		        
		        Sheet 	sheet 		= null;
		        Row 	row 		= null;
		        int 	lastRowNum 	= 0;

		        int numSheets = (workbook != null ? workbook.getNumberOfSheets() : 0);

		        for(int i = 0; i < numSheets; i++) {

		            sheet = workbook.getSheetAt(i);
		            
		            writer.writeStartElement("sheet");
		            writer.writeAttribute("name", sheet.getSheetName());
		            
		            if(sheet.getPhysicalNumberOfRows() > 0) {

		                lastRowNum = sheet.getLastRowNum();
		                for(int j = 0; j <= lastRowNum; j++) {
		                    row = sheet.getRow(j);
		                    writer.writeStartElement("row");
		                    Cell cell = null;
		                    int maxCellNum = getMaxCellNum(sheet);

		                    if(row != null) {
		                    	
		                        for(int k = 0; k < maxCellNum; k++) {
		                            cell = row.getCell(k);
		                            if(cell == null) {
		                                writer.writeEmptyElement("column");
		                            }
		                            else {
		                                if(cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
		                                	createElement(writer, "column", formatter.formatCellValue(cell));
		                                }
		                                else {
		                                	createElement(writer, "column", formatter.formatCellValue(cell, evaluator));
		                                }
		                            }
		                        }
		                    }
		                    writer.writeEndElement();
		                }
		            }
		            writer.writeEndElement();
		        }
		        writer.writeEndDocument();
				final XdmNode context = writer.getDocumentNode();

				xsltTransformer.setInitialContextNode(context);
			} catch (Exception e) {
				throw new XPathException("");
			} finally {
				try {
					writer.close();
				} catch (XMLStreamException e) {
					logger.error(e, e);
				}
			}
		}
	}
		
	private int getMaxCellNum(Sheet sheet) {
		int maxCellNum = 0;
		Row row;
		for (int i = 0; i < sheet.getLastRowNum(); i++) {
			row = sheet.getRow(i);
			maxCellNum = Math.max(maxCellNum, row.getLastCellNum());
		}
		return maxCellNum;
	}
	
	private void createElement(BuildingStreamWriterImpl writer, String name, String value) throws XPathException, XMLStreamException {
		writer.writeStartElement(name);
		writer.writeCharacters(value);
		writer.writeEndElement();
	}
	
	private void initTextSource(XsltTransformer xsltTransformer) throws XPathException {
		try {
		File file = null;
		try {
			file = new File(new URI(getXmlSourceUrl().toExternalForm()));
		} catch (URISyntaxException e1) {
			throw new XPathException("Input source could not be found. (URL: '" + getXmlSourceUrl().toExternalForm() + "')");
		}
		if (!file.exists()) {
			throw new XPathException("Input source could not be found. (URL: '" + file.getAbsolutePath() + "')");
		} else {
			String string = FileUtil.readFileToString(file);
			
			Orphan 	node 	= new Orphan(xsltConrefCache.getConfiguration());
			node.setNodeKind(Type.TEXT);
			node.setStringValue(string);
			XdmNode textNode = new XdmNode(node);
			xsltTransformer.setInitialContextNode(textNode);
		}
		} catch(Exception e) {
			logger.error(e, e);
		}
	}
	
	private void initCsvSource(XsltTransformer xsltTransformer) {
		try {
			final Processor 				processor 		= new Processor(xsltConrefCache.getConfiguration());
			final DocumentBuilder 			builder 		= processor.newDocumentBuilder();
			final BuildingStreamWriterImpl 	writer 			= builder.newBuildingStreamWriter();
			File 							file			= null; 	
			try {
				file = new File(new URI(getXmlSourceUrl().toExternalForm()));
			} catch (URISyntaxException e1) {
				throw new XPathException("Input source could not be found. (URL: '" + getXmlSourceUrl().toExternalForm() + "')");
			}
			if (!file.exists()) {
				throw new XPathException("Input source could not be found. (URL: '" + file.getAbsolutePath() + "')");
			} else {
				final Reader 					reader 			= new FileReader(file);
		        CSVParser 						parser 			= CSVFormat.EXCEL
														            .withIgnoreEmptyLines()
														            .withIgnoreHeaderCase()
														            .withRecordSeparator('\n').withQuote('"')
														            .withEscape('\\').withDelimiter(';').withTrim()
														            .parse(reader);
				final List<CSVRecord> 			records 		= parser.getRecords();
				final int 						maxCellCount 	= getMaxCellCount(records);
				
				writer.writeStartDocument();
				writer.writeStartElement(XML_TAG_ROOT);
				
				for (CSVRecord record : records) {
					
					writer.writeStartElement(XML_TAG_ROW);
					
					for (int i = 0; i < maxCellCount; i++) {
						if (record.size() > i) {
							createElement(writer, XML_TAG_COLUMN, record.get(i));
						} else {
							writer.writeEmptyElement(XML_TAG_COLUMN);
						}
					}
					
					writer.writeEndElement();
				}
				
				writer.writeEndElement();
				writer.writeEndDocument();
				
				final XdmNode context = writer.getDocumentNode();
	
				xsltTransformer.setInitialContextNode(context);
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	private static int getMaxCellCount(List<CSVRecord> records) {
		int maxCellNum = 0;
		for (CSVRecord record : records) {
			maxCellNum = Math.max(maxCellNum, record.size());
		}
		return maxCellNum;
	}
	
	public Source getScriptSource() throws XPathException {
		final URIResolver 	uriResolver 	= xsltConrefCache.getUriResolver();
		final String 		scriptString 	= node.getAttribute(ATTR_URI, NAMESPACE_URI);
		final String 		baseUrlString	= baseUrl.toExternalForm();
		//logger.info("getScriptUrl:");
		//logger.info("  uriResolver: " + uriResolver);
		//logger.info("  scriptString:" + scriptString);
		//logger.info("  baseUrlString: " + baseUrlString);
		try {
			return uriResolver.resolve(scriptString, baseUrlString);
		} catch (TransformerException e) {
			throw new XPathException("Failed to resolve script URI '" + scriptString + "' with base-url '" + baseUrlString + "': " + e.getMessage());
		}
	}
	
	public String getUniqueId() {
		Source scriptSource = null;
		try {
			scriptSource = getScriptSource();
		} catch (XPathException e) {
			logger.error(e, e);
		}
		return (scriptSource == null) ? node.getAttribute(ATTR_URI, NAMESPACE_URI) : scriptSource.getSystemId();
	}

	public String getScriptName() {
		return node.getAttribute(ATTR_URI, NAMESPACE_URI);
	}


	public String getScriptSystemId() {
		final URIResolver 	uriResolver 	= xsltConrefCache.getUriResolver();
		final String 		scriptString 	= node.getAttribute(ATTR_URI, NAMESPACE_URI);
		final String 		baseUrlString	= baseUrl.toExternalForm();
		try {
			return uriResolver.resolve(scriptString, baseUrlString).getSystemId();
		} catch (TransformerException e) {
			return null;
		}
	}
	
	public Source getXmlSource() {
		final String attrValue = node.getAttribute(ATTR_SOURCE_URI, NAMESPACE_URI);
		if (attrValue == null) {
			return null;
		} else if (attrValue.isEmpty()) {
			return new StreamSource(new StringReader(EMPTY_SOURCE_XML));
		} else {
			final URIResolver uriResolver = xsltConrefCache.getUriResolver();
			try {
				//logger.info("baseUrl: " + baseUrl);
				return uriResolver.resolve(attrValue, baseUrl.toExternalForm());
			} catch (TransformerException e) {
				logger.error(e, e);
				return null;
			}
		}
	}
	
	public URL getXmlSourceUrl() {
		final Source source = getXmlSource();
		if (source != null) {
			try {
				return new URL(source.getSystemId());
			} catch (MalformedURLException e) {
				logger.error(e, e);
			}
		}
		return null;
	}


	public URL getScriptUrl() {
		try {
			return new URL(getScriptSource().getSystemId());
		} catch (Exception e) {
			logger.error(e, e);
			return null;
		}
	}

	public String getStartTemplate() {
		return node.getAttribute(ATTR_START_TEMPLATE, NAMESPACE_URI);
	}
	
	private static boolean needsReparse(final NodeInfo resolvedElement) {
		if (resolvedElement.getDisplayName() == NAME_NO_CONTENT) {
			return false;
		} else {
			final String classAttr = resolvedElement.getAttributeValue("", DitaUtil.ATTR_CLASS);
			return ((classAttr == null) || (classAttr.isEmpty()));
		}
		//final String reparseString = resolvedElement.getAttributeValue(NAMESPACE_URI, ATTR_REPARSE);
		//logger.info("reparseString: " + reparseString);
		//return ((reparseString != null) && (reparseString.equals(YES)));
	}
	
	private void setCustomParameters(XsltTransformer xsltTransformer) throws XPathException {
		//logger.info("setCustomParameters()");
		final Source						scriptSource	= getScriptSource();
		final XsltExecutable 				xsltExecutable 	= xsltConrefCache.getTransformerCache().getExecutable(scriptSource);
		final Map<QName, ParameterDetails>	xslParamMap 	= xsltExecutable.getGlobalParameters();
		final Map<String, String>			paramMap 		= getParamMap();
		
		for (Entry<String, String> entry: paramMap.entrySet()) {
			//logger.info("  entry: " + entry);
			if (entry.getValue() != null) {
				final QName paramName = new QName(entry.getKey());
				//logger.info("  paramName: " + paramName);
				if (xslParamMap.containsKey(paramName)) {
					try {
						xsltTransformer.setParameter(paramName, new XdmAtomicValue(EmbeddedXPathResolver.resolve(entry.getValue(), node), ItemType.UNTYPED_ATOMIC));
						//logger.info("  set parameters set: " + entry.getValue());
					} catch (SaxonApiException e) {
						throw new XPathException("Failed to set parameter '" + paramName + "' to value '" + entry.getValue()+ "'");
					}
				} else {
					//logger.error("Parameter '" + paramName + "' not defined in script.");
				}
			}
		}
	}
	
	private static String createXPathToElement(NodeWrapper node) throws TempContextException {
		final URL baseUrl = node.getBaseUrl();
		String createXPathToElement = "";
		
		while ((node != null) && (node.getParent() != null) && (node.getParent().getBaseUrl().equals(baseUrl))) {
			final int index = node.getChildElementIndexWithinParent();
			if (index <= 0) {
				throw new TempContextException();
			}
			createXPathToElement = "/*[" + index + "]" + createXPathToElement;
			node = node.getParent();
		}
		
		if ((node != null) && (node.getParent() != null)) {
			// local context is based on @xml:base, thus, add the xpath to the root element for the target context.
			createXPathToElement = "/*[1]" + createXPathToElement;
		}
		
		//logger.info("createXPathToElement: result = " + createXPathToElement);
		return createXPathToElement;
	}


	public int getStage() {
		final String stageString = node.getAttribute(ATTR_STAGE, NAMESPACE_URI);
		if ((stageString != null) && (!stageString.isEmpty())) {
			try {
				return Integer.parseInt(stageString);
			} catch (NumberFormatException e) {
				// no special handling
			}
		}
		return STAGE_DISPLAY;
	}


	public boolean isSingleSource() {
		final String flags = node.getAttribute(ATTR_FLAGS, NAMESPACE_URI);
		if (flags != null) {
			return flags.contains(FLAG_SINGLE_SOURCE);
		} else {
			return false;
		}
	}


	public String getSourceSystemId() {
		final Source source = getXmlSource();
		if (source != null) {
			return source.getSystemId();
		} else {
			return null;
		}
	}
	
	public String getSourceType() {
		String sourceType = node.getAttribute(ATTR_SOURCE_TYPE, NAMESPACE_URI);
		return (sourceType == null ? SOURCE_TYPE_XML : sourceType);
	}

	public boolean isCopy() {
		final String flags = node.getAttribute(ATTR_FLAGS, NAMESPACE_URI);
		if (flags != null) {
			return flags.contains(FLAG_COPY);
		} else {
			return false;
		}
	}


	public Map<String, String> getParamMap() {
		final List<String> 			attrNameList 	= node.getAttributeNamesOfNamespace(NAMESPACE_PARAMETER_URI);
		final Map<String, String>	paramMap		= new HashMap<>();
		for (String attrName : attrNameList) {
			final String	paramName 	= attrName.replaceAll("(^[^\\{\\}]*:)|(^\\{.*\\})", "");
			final String 	paramValue	= node.getAttribute(attrName, NAMESPACE_PARAMETER_URI);
			paramMap.put(paramName, paramValue);
		}
		return paramMap;
	}


	public NodeWrapper getNode() {
		return node;
	}
}
