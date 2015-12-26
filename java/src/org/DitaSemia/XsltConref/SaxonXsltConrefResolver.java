/*
 * This file is part of the DITA-SEMIA project (www.dita-semia.org).
 * See the accompanying LICENSE file for applicable licenses.
 */

package org.DitaSemia.XsltConref;

import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SimpleExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.ExtensionInstruction;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;

import org.DitaSemia.JavaBase.SaxonNodeWrapper;
import org.apache.log4j.Logger;

public class SaxonXsltConrefResolver extends ExtensionInstruction {

	private static final Logger logger = Logger.getLogger(SaxonXsltConrefResolver.class.getName());
	
	Expression select;

	@Override
    public void prepareAttributes() throws XPathException {
    	
    	// notwendiges select-Attribut
    	final String selectAtt = getAttributeValue("", "select");
        if (selectAtt == null) {
            reportAbsence("select");
        } else {
        	select = makeExpression(selectAtt);
        }
    }

	@Override
    public void validate(ComponentDeclaration decl) throws XPathException 
	{
        super.validate(decl);
        select	= typeCheck("select", 	select);
    }

	@Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException 
	{
		
        return new ResolveInstruction(select);
    }

	private static class ResolveInstruction extends SimpleExpression 
	{

        public static final int SELECT	= 0;
        
		public ResolveInstruction(Expression select) 
		{
			Expression[] subs = {select};
        	setArguments(subs);
        }

    	@Override
        public int computeCardinality() 
    	{
            return StaticProperty.EXACTLY_ONE;
        }

    	@Override
        public String getExpressionType() 
    	{
            return "xcr:resolve";
        }

    	@Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException 
    	{
    		final Item select = arguments[SELECT].head();

//    		logger.info("resolve: " + select);
    		
    		if (select instanceof NodeInfo) 
    		{
				final NodeInfo 		xsltConrefNode 	= (NodeInfo)select;
    			
    			final SaxonNodeWrapper 	snw 		= new SaxonNodeWrapper(xsltConrefNode);
    			final XsltConref 		xsltConref 	= XsltConref.fromNode(snw);
    			if (xsltConref != null)
    			{
    				try
    				{
            			final String resolvedString = xsltConref.resolve().serialize();
            			
            			final DocumentInfo 	resultDoc	= context.getConfiguration().buildDocument(new StreamSource(new StringReader(resolvedString)));
            			
            			AxisIterator childIterator = resultDoc.iterateAxis(AxisInfo.CHILD, NodeKindTest.ELEMENT);
            			
            			return childIterator.next(); 
    				}
    				catch (Exception e)
    				{
    					logger.error(e, e);
    					throw new XPathException("Error resolving the xslt-conref:\n" + e.getMessage());	
    				}
    			}
    			else
    			{
    				throw new XPathException("The select attribute needs to refer to a valid xslt-conref element.");	
    			}
    		} 
    		else 
    		{
    			throw new XPathException("The select attribute needs to refer to an xml node.");
    		}
        }
    }
}
