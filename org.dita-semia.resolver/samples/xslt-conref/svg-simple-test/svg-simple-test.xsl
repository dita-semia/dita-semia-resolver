<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all"
	expand-text				= "yes">
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:variable name="WIDTH"			as="xs:double"	select="160"/>
	<xsl:variable name="HEIGHT"			as="xs:double"	select="20"/>
	<xsl:variable name="RADIUS"			as="xs:double"	select="5"/>
	<xsl:variable name="STROKE_WIDTH"	as="xs:double"	select="1"/>
	<xsl:variable name="FONT"			as="xs:string"	select="'Arial'"/>
	
	<xsl:variable name="BACKROUND_GRADIENT_ID"	as="xs:string"	select="'BackgroundGradient'"/>
	
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
    <xsl:template match="/">

    	<fig class="{$CP_FIG}">
    		<svg-container class="{$CP_SVG_CONTAINER}">
	    		<svg xmlns="http://www.w3.org/2000/svg" width="{$WIDTH}mm" height="{$HEIGHT}mm">
	    			
	    			<defs>
	    				
		    			<linearGradient 
			    				id		= "{$BACKROUND_GRADIENT_ID}" 
			    				x1		= "0%" 
			    				y1		= "0%" 
			    				x2		= "100%" 
			    				y2		= "100%">
		    				<stop offset="0%"	style="stop-color:rgb(248,252,255);stop-opacity:1"/>
		    				<stop offset="50%"	style="stop-color:rgb(228,232,255);stop-opacity:1"/>
		    				<stop offset="100%"	style="stop-color:rgb(158,162,185);stop-opacity:1"/>
		    			</linearGradient>
	    				
	    			</defs>
	    			
	    			<rect
	    					x				= "{($STROKE_WIDTH div 2)}mm" 
	    					y				= "{($STROKE_WIDTH div 2)}mm"
		    				width			= "{$WIDTH - $STROKE_WIDTH}mm"
		    				height			= "{$HEIGHT - $STROKE_WIDTH}mm"
		    				rx				= "{$RADIUS}mm"
		    				ry				= "{$RADIUS}mm"
		    				fill			= "url(#{$BACKROUND_GRADIENT_ID})"
	    					stroke-width	= "{$STROKE_WIDTH}mm"
	    					stroke			= "rgb(85,90,100)"/>
	    				
	    			<text 
	    					x			= "{$WIDTH div 2}mm" 
	    					y			= "{$HEIGHT div 2}mm"
	    					dy			= "0.3em"
	    					style		= "text-anchor: middle"
	    					font-size	= "{$HEIGHT * 0.5}mm"
	    					font-family	= "{$FONT}"
	    					font-weight	= "700">
	    				<xsl:value-of select="*/title"/>
	    			</text>
	    			
	    		</svg>
    		</svg-container>
    	</fig>
    
    
    </xsl:template>
	
	
</xsl:stylesheet>
