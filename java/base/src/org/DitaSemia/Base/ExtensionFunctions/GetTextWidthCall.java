package org.DitaSemia.Base.ExtensionFunctions;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.Logger;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;

public class GetTextWidthCall extends ExtensionFunctionCall {
	
	private static final Logger logger = Logger.getLogger(GetTextWidthCall.class.getName());
	
	private static final double	SCALING	= 100;

	@Override
	public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
		//logger.info("call");
		try {
			final String 	text 		= arguments[0].head().getStringValue();
			final String 	fontFamily 	= arguments[1].head().getStringValue();
			final boolean 	isBold 		= ((BooleanValue)arguments[2].head()).getBooleanValue();
			final double	fontSize 	= ((DoubleValue)arguments[3].head()).getDoubleValue();
			
			final Font 				font 				= new Font(fontFamily, isBold ? Font.BOLD : Font.PLAIN, (int)Math.round(fontSize * SCALING));
			final FontRenderContext	fontRenderContext	= new FontRenderContext(new AffineTransform(), true, true);
			final Rectangle2D 		bondingRect			= font.getStringBounds(text, fontRenderContext);
			
			final int				textWidth			= (int)bondingRect.getWidth();
			
			return DoubleValue.makeDoubleValue((double)textWidth / SCALING).asAtomic();
			
		} catch (XPathException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
			throw new XPathException(e.getMessage());
		}
	}

}
