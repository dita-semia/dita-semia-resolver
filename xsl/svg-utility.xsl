<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="3.0"
    xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
    xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
    xmlns:xlink	= "http://www.w3.org/1999/xlink"
    xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all"
	expand-text				= "yes">

	<xsl:variable name="SVG.FONT_NORMAL" 				as="xs:string" select="'normal'"/>
	<xsl:variable name="SVG.FONT_BOLD" 					as="xs:string" select="'bold'"/>
	
	<xsl:variable name="SVG.TEXT_ANCHOR_START" 			as="xs:string" select="'start'"/>
	<xsl:variable name="SVG.TEXT_ANCHOR_MIDDLE"			as="xs:string" select="'middle'"/>
	
	<xsl:variable name="SVG.DY_BASELINE"				as="xs:string" select="'0.8em'"/>
	<xsl:variable name="SVG.DY_CENTER"					as="xs:string" select="'0.3em'"/>
	<xsl:variable name="SVG.DY_BOTTOM"					as="xs:string" select="'1.0em'"/>

	<xsl:variable name="SVG.COLOR_TRANSPARENT" 			as="xs:string" select="'none'"/>
	<xsl:variable name="SVG.COLOR_BLACK" 				as="xs:string" select="'black'"/>
	<xsl:variable name="SVG.COLOR_WHITE" 				as="xs:string" select="'white'"/>
	
	<xsl:variable name="SVG.STROKE_LINEJOIN_MITER"		as="xs:string" select="'miter'"/>
	<xsl:variable name="SVG.STROKE_LINEJOIN_ROUND" 		as="xs:string" select="'round'"/>
	<xsl:variable name="SVG.STROKE_LINEJOIN_BEVEL" 		as="xs:string" select="'bevel'"/>

	<xsl:variable name="SVG.GUI_LINE_GRADIENT" 			as="xs:string" select="'GuiLineGradient'"/>
	<xsl:variable name="SVG.GUI_FILL_GRADIENT" 			as="xs:string" select="'GuiFillGradient'"/>
	<xsl:variable name="SVG.GUI_LINE_GRADIENT2" 		as="xs:string" select="'GuiLineGradient2'"/>
	
	<xsl:variable name="SVG.ICON_STROKE_WIDTH_SCALE"	as="xs:double"	select="0.1"/>
	
	<xsl:variable name="SVG.STANDARDFONT"				as="xs:string"	select="'Calibri'"/>
	

	<xsl:variable name="XML.SPACE_PRESERVE" as="attribute()">
		<xsl:attribute name="xml:space" select="'preserve'"/>
	</xsl:variable>


	<!-- ========== Template: SvgRoot ========== -->
	<xsl:template name="SvgRoot" as="element()">
		<xsl:param name="widthInMm" 	as="xs:double?"/>
		<xsl:param name="heightInMm" 	as="xs:double?"/>
		<xsl:param name="defs" 			as="item()*"/>
		<xsl:param name="content" 		as="item()*"/>
		
		<svg xmlns="http://www.w3.org/2000/svg" version="1.1">
			<xsl:if test="exists($widthInMm)">
				<xsl:attribute name="width" select="concat($widthInMm, 'mm')"/>
			</xsl:if>
			<xsl:if test="exists($heightInMm)">
				<xsl:attribute name="height" select="concat($heightInMm, 'mm')"/>
			</xsl:if>
			<xsl:if test="exists($defs)">
				<defs>
					<xsl:sequence select="$defs"/>
				</defs>
			</xsl:if>
			<xsl:sequence select="$content"/>
		</svg>
	</xsl:template>
	
	
	<!-- ========== Template: SvgGroup ========== -->
	<xsl:template name="SvgGroup" as="element()">
		<xsl:param name="translateXInMm"	as="xs:double"	select="0.0"/>
		<xsl:param name="translateYInMm"	as="xs:double"	select="0.0"/>
		<xsl:param name="scale"				as="xs:double?"/>
		<xsl:param name="content" 			as="item()*"/>
		
		<g xmlns="http://www.w3.org/2000/svg">
			<xsl:variable name="TransformList" as="xs:string*">
				<xsl:if test="($translateXInMm != 0.0) or ($translateYInMm != 0.0)">
					<xsl:text>translate({ds:mmToPx($translateXInMm)}, {ds:mmToPx($translateYInMm)})</xsl:text>
				</xsl:if>
				<xsl:if test="exists($scale)">
					<xsl:text>scale({$scale})</xsl:text>
				</xsl:if>
			</xsl:variable>
			<xsl:if test="exists($TransformList)">
				<xsl:attribute name="transform" select="string-join($TransformList, ' ')"/>
			</xsl:if>
			
			<xsl:sequence select="$content"/>
		</g>
	</xsl:template>


	<!-- ========== Template: SvgText ========== -->
	<xsl:template name="SvgText" as="element()">
		<xsl:param name="xInMm"			as="xs:double?"/>
		<xsl:param name="yInMm"			as="xs:double?"/>
		<xsl:param name="dy"			as="item()?"	select="$SVG.DY_BASELINE"/>
		<xsl:param name="textAnchor"	as="xs:string?"	select="$SVG.TEXT_ANCHOR_START"/>
		<xsl:param name="fontWeight"	as="xs:string?"	select="$SVG.FONT_NORMAL"/>
		<xsl:param name="fontFamily"	as="xs:string?"/>
		<xsl:param name="fontSize"		as="item()?"/>
		<xsl:param name="fillColor"		as="xs:string?"/>
		<xsl:param name="text"			as="xs:string?"/>

		<text xmlns="http://www.w3.org/2000/svg">
			<xsl:copy-of select="$XML.SPACE_PRESERVE"/>
			<xsl:if test="exists($xInMm)">
				<xsl:attribute name="x">{$xInMm}mm</xsl:attribute>
			</xsl:if>
			<xsl:if test="exists($yInMm)">
				<xsl:attribute name="y">{$yInMm}mm</xsl:attribute>
			</xsl:if>
			<xsl:if test="exists($dy)">
				<xsl:attribute name="dy" select="$dy"/>
			</xsl:if>
			<xsl:attribute name="style">
				<xsl:if test="exists($textAnchor)">
					<xsl:text>text-anchor: {$textAnchor}; </xsl:text>
				</xsl:if>
			</xsl:attribute>
			<xsl:if test="exists($fontWeight)">
				<xsl:attribute name="font-weight" select="$fontWeight"/>
			</xsl:if>
			<xsl:if test="exists($fontFamily)">
				<xsl:attribute name="fontFamily" select="$fontFamily"/>
			</xsl:if>
			<xsl:if test="exists($fontSize)">
				<xsl:attribute name="font-size" select="$fontSize"/>
			</xsl:if>
			<xsl:if test="exists($fillColor)">
				<xsl:attribute name="fill" select="$fillColor"/>
			</xsl:if>
			<xsl:value-of select="$text"/>
		</text>
	</xsl:template>
	
	
	<!-- ========== Template: SvgRect ========== -->
	<xsl:template name="SvgRect" as="element()">
		<xsl:param name="xInMm"				as="xs:double"	select="0.0"/>
		<xsl:param name="yInMm"				as="xs:double"	select="0.0"/>
		<xsl:param name="widthInMm"			as="xs:double"/>
		<xsl:param name="heightInMm"		as="xs:double"/>
		<xsl:param name="radiusXInMm"		as="xs:double"	select="0.0"/>
		<xsl:param name="radiusYInMm"		as="xs:double"	select="$radiusXInMm"/>
		<xsl:param name="fillColor"			as="xs:string?"/>
		<xsl:param name="fillUrl"			as="xs:string?"/>
		<xsl:param name="strokeWidth"		as="xs:double?"/>
		<xsl:param name="strokeColor"		as="xs:string?"/>
		<xsl:param name="strokeUrl"			as="xs:string?"/>
		<xsl:param name="strokeDasharray"	as="xs:string?"/>
		
		
		<rect xmlns="http://www.w3.org/2000/svg"
				x		= "{$xInMm}mm" 
				y		= "{$yInMm}mm"
				width	= "{$widthInMm}mm"
				height	= "{$heightInMm}mm"
				rx		= "{$radiusXInMm}mm"
				ry		= "{$radiusYInMm}mm">
			
			<xsl:choose>
				<xsl:when test="exists($fillColor)">
					<xsl:attribute name="fill" select="$fillColor"/>
				</xsl:when>
				<xsl:when test="exists($fillUrl)">
					<xsl:attribute name="fill">url(#{$fillUrl})</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="fill" select="$SVG.COLOR_TRANSPARENT"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="exists($strokeColor)">
					<xsl:attribute name="stroke" select="$strokeColor"/>
				</xsl:when>
				<xsl:when test="exists($strokeUrl)">
					<xsl:attribute name="stroke">url(#{$strokeUrl})</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="stroke" select="$SVG.COLOR_TRANSPARENT"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="exists($strokeWidth)">
				<xsl:attribute name="stroke-width">{$strokeWidth}mm</xsl:attribute>
			</xsl:if>
			<xsl:if test="exists($strokeDasharray)">
				<xsl:attribute name="stroke-dasharray" select="$strokeDasharray"/>
			</xsl:if>
		</rect>
	</xsl:template>
	
	
	
	<!-- ========== Template: SvgLine ========== -->
	<xsl:template name="SvgLine" as="element()">
		<xsl:param name="x1InMm"			as="xs:double"/>
		<xsl:param name="y1InMm"			as="xs:double"/>
		<xsl:param name="x2InMm"			as="xs:double"/>
		<xsl:param name="y2InMm"			as="xs:double"/>
		<xsl:param name="strokeColor"		as="xs:string?"/>
		<xsl:param name="strokeWidth"		as="xs:double?"/>
		<xsl:param name="strokeDasharray"	as="xs:string?"/>
		<xsl:param name="markerEndUrl"		as="xs:string?"/>

		<line xmlns="http://www.w3.org/2000/svg"
				x1					= "{$x1InMm}mm" 
				y1					= "{$y1InMm}mm" 
				x2					= "{$x2InMm}mm" 
				y2					= "{$y2InMm}mm">
			
			<xsl:if test="exists($strokeColor)">
				<xsl:attribute name="stroke" select="$strokeColor"/>
			</xsl:if>
			<xsl:if test="exists($strokeWidth)">
				<xsl:attribute name="stroke-width">{$strokeWidth}mm</xsl:attribute>
			</xsl:if>
			<xsl:if test="exists($strokeDasharray)">
				<xsl:attribute name="stroke-dasharray" select="$strokeDasharray"/>
			</xsl:if>
			<xsl:if test="exists($markerEndUrl)">
				<xsl:attribute name="marker-end">url(#{$markerEndUrl})</xsl:attribute>
			</xsl:if>
		</line>
	</xsl:template>
		
		
	<!-- ========== Template: SvgPath ========== -->
	<xsl:template name="SvgPath" as="element()">
		<xsl:param name="stroke-width"		as="xs:double?"/>
		<xsl:param name="stroke"			as="xs:string?"/>
		<xsl:param name="stroke-linejoin"	as="xs:string?"/>
		<xsl:param name="fill"				as="xs:string?"/>
		<xsl:param name="fillUrl"			as="xs:string?"/>
		<xsl:param name="d"					as="xs:string+"/>
		<xsl:param name="strokeDasharray"	as="xs:string?"/>
		
		<path xmlns="http://www.w3.org/2000/svg">
			<xsl:choose>
				<xsl:when test="exists($fill)">
					<xsl:attribute name="fill" select="$fill"/>
				</xsl:when>
				<xsl:when test="exists($fillUrl)">
					<xsl:attribute name="fill">url(#{$fillUrl})</xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<xsl:if test="exists($stroke-width)">
				<xsl:attribute name="stroke-width">{$stroke-width}mm</xsl:attribute>
			</xsl:if>
			<xsl:if test="exists($stroke)">
				<xsl:attribute name="stroke" select="$stroke"/>
			</xsl:if>
			<xsl:if test="exists($stroke-linejoin)">
				<xsl:attribute name="stroke-linejoin" select="$stroke-linejoin"/>
			</xsl:if>
			<xsl:if test="exists($strokeDasharray)">
				<xsl:attribute name="stroke-dasharray" select="$strokeDasharray"/>
			</xsl:if>
			<xsl:attribute name="d" select="$d"/>
		</path>
	</xsl:template>
	
	
	<!-- ========== Template: SvgUse ========== -->
	<xsl:template name="SvgUse" as="element()">
		<xsl:param name="id"			as="xs:string"/>
		<xsl:param name="xInMm"			as="xs:double?"/>
		<xsl:param name="yInMm"			as="xs:double?"/>
		<xsl:param name="widthInMm"		as="xs:double?"/>
		<xsl:param name="heightInMm"	as="xs:double?"/>
		
		<use xmlns="http://www.w3.org/2000/svg" xlink:href="#{$id}">
			<xsl:if test="exists($xInMm)">
				<xsl:attribute name="x" select="concat($xInMm, 'mm')"/>
			</xsl:if>
			<xsl:if test="exists($yInMm)">
				<xsl:attribute name="y" select="concat($yInMm, 'mm')"/>
			</xsl:if>
			<xsl:if test="exists($widthInMm)">
				<xsl:attribute name="width" select="concat($widthInMm, 'mm')"/>
			</xsl:if>
			<xsl:if test="exists($heightInMm)">
				<xsl:attribute name="height" select="concat($heightInMm, 'mm')"/>
			</xsl:if>
		</use>
	</xsl:template>
	
	

	<!-- ============================================================================ -->
	<!-- ==              Funktionen zur Umrechnung zwischen Einheiten              == -->
	<!-- ============================================================================ -->

	<!-- ========== Funktion: ds:MmToPx ========== -->
	<!-- Assumption: 96 dpi (DPI = pixel per inch, 1in = 2,54cm)                         -->
	<xsl:function name="ds:mmToPx" as="xs:double">
		<xsl:param name="Mm" as="xs:double"/>
		<xsl:sequence select="$Mm * 3.779528"/>
	</xsl:function>
	
	<xsl:function name="ds:mmToPt" as="xs:double">
		<xsl:param name="Mm" as="xs:double"/>
		<xsl:sequence select="$Mm * 3.779528 * 0.75"/>
	</xsl:function>
	
	<xsl:function name="ds:ptToMm" as="xs:double">
		<xsl:param name="Pt" as="xs:double"/>
		<xsl:sequence select="$Pt div (0.75 * 3.779528)"/>
	</xsl:function>
	
	<xsl:function name="ds:ptToPx" as="xs:double">
		<xsl:param name="Pt" as="xs:double"/>
		<xsl:sequence select="$Pt div 0.75"/>
	</xsl:function>
	
	<xsl:function name="ds:pxToMm" as="xs:double">
		<xsl:param name="Px" as="xs:double"/>
		<xsl:sequence select="$Px div 3.779528"/>
	</xsl:function>
	
	<xsl:function name="ds:anyToPx" as="xs:string">
		<xsl:param name="Value" as="xs:string?"/>
		<xsl:choose>
			<xsl:when test="matches($Value, '^[-+.0-9]+mm$')">
				<xsl:sequence select="string(ds:mmToPx(xs:double(replace($Value, 'mm$', ''))))"/>
			</xsl:when>
			<xsl:when test="matches($Value, '^[-+.0-9]+pt$')">
				<xsl:sequence select="string(ds:ptToPx(xs:double(replace($Value, 'pt$', ''))))"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$Value"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	
	
	
	<!-- ========== Template: SvgMarkerArrow ========== -->
	<xsl:template name="SvgMarkerArrow">
		<xsl:param name="id"		as="xs:string"/>
		<xsl:param name="scalingX"	as="xs:double"	select="6"/>
		<xsl:param name="scalingY"	as="xs:double"	select="6"/>
		<xsl:param name="offset"	as="xs:double"	select="$scalingX - 1"/>
		<xsl:param name="lineColor"	as="xs:string" 	select="$SVG.COLOR_BLACK"/>
		<xsl:param name="fillColor"	as="xs:string" 	select="$SVG.COLOR_TRANSPARENT"/>
		
		<marker xmlns="http://www.w3.org/2000/svg"
				id				= "{$id}"
				refX			= "{$offset}" 
				refY			= "{$scalingY div 2}"
				markerUnits		= "strokeWidth"
				markerWidth		= "{$scalingX}" 
				markerHeight	= "{$scalingY}"
				orient			= "auto"
				overflow		= "visible">
			<path 	d				= "M 0,0 l {$scalingX},{$scalingY div 2} l {-$scalingX},{$scalingY div 2}"
					stroke-width	= "1"
					stroke 			= "{$lineColor}"
					style 			= "fill: {$fillColor};"/>
		</marker>
	</xsl:template>
	
	
	<!-- ========== Function: adaptColor ========== -->
	<xsl:function name="ds:adaptColor" as="xs:string">
		<xsl:param name="color"			as="xs:string"/>
		<xsl:param name="adaptation"	as="xs:double"/>	<!-- +1 = white, -1 = black-->

		<xsl:variable name="rgbList" as="xs:double*" select="for $i in (tokenize($color, '[^0-9]+'))[. != ''] return xs:double($i)"/>

		<xsl:choose>
			<xsl:when test="$adaptation = 0">
				<!-- no adaptation -->
				<xsl:sequence select="$color"/>
			</xsl:when>
			<xsl:when test="not(matches($color, 'rgb\(\s*[0-9]+,\s*[0-9]+,\s*[0-9]+\)'))">
				<!-- adaptation not possible -->
				<xsl:sequence select="$color"/>
			</xsl:when>
			<xsl:when test="$adaptation > 0">
				<xsl:value-of select="concat(
						'rgb(', 
						string-join(
						for $i in $rgbList return xs:string(round($i + ((255 - $i) * $adaptation))),
							','),
						')')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat(
						'rgb(', 
						string-join(
						for $i in $rgbList return xs:string(round($i * (1 + $adaptation))),
							','),
						')')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	
	<!-- ========== Template: SvgGradient ========== -->
	<xsl:variable name="SvgGradient.DIAGONAL" 	as="xs:string" select="'diagonal'"/>
	<xsl:variable name="SvgGradient.HORIZONTAL" as="xs:string" select="'horizontal'"/>
	<xsl:variable name="SvgGradient.VERTIKAL" 	as="xs:string" select="'vertical'"/>

	<xsl:template name="SvgGradient" as="element()">
		<xsl:param name="id"			as="xs:string"/>
		<xsl:param name="color"			as="xs:string"/>
		<xsl:param name="direction"		as="xs:string"	select="$SvgGradient.DIAGONAL"/>
		<xsl:param name="adaptation1"	as="xs:double"	select="0.2"/>
		<xsl:param name="adaptation2"	as="xs:double"	select="0"/>
		<xsl:param name="adaptation3"	as="xs:double"	select="-0.2"/>
		
		<linearGradient xmlns	= "http://www.w3.org/2000/svg" 
				id		= "{$id}" 
				x1		= "0%" 
				y1		= "0%" 
				x2		= "{if ($direction = ($SvgGradient.DIAGONAL, $SvgGradient.HORIZONTAL)) then 100 else 0}%" 
				y2		= "{if ($direction = ($SvgGradient.DIAGONAL, $SvgGradient.VERTIKAL))   then 100 else 0}%">
			<stop offset="0%"   style="stop-color:{ds:adaptColor($color, $adaptation1)};stop-opacity:1"/>
			<stop offset="50%"  style="stop-color:{ds:adaptColor($color, $adaptation2)};stop-opacity:1"/>
			<stop offset="100%" style="stop-color:{ds:adaptColor($color, $adaptation3)};stop-opacity:1"/>
		</linearGradient>
	</xsl:template>
	
	
	<!-- ========== Template: SvgGradient2Farben ========== -->
	<xsl:template name="SvgGradient2Colors" as="element()">
		<xsl:param name="id"		as="xs:string"/>
		<xsl:param name="color1"	as="xs:string"/>
		<xsl:param name="color2"	as="xs:string"/>
		<xsl:param name="direction"	as="xs:string"	select="$SvgGradient.DIAGONAL"/>
		
		<linearGradient xmlns	= "http://www.w3.org/2000/svg" 
				id		= "{$id}" 
				x1		= "0%" 
				y1		= "0%" 
				x2		= "{if ($direction = ($SvgGradient.DIAGONAL, $SvgGradient.HORIZONTAL)) then 100 else 0}%" 
				y2		= "{if ($direction = ($SvgGradient.DIAGONAL, $SvgGradient.VERTIKAL))   then 100 else 0}%">
			<stop offset="0%"   style="stop-color:{$color1};stop-opacity:1"/>
			<stop offset="100%" style="stop-color:{$color2};stop-opacity:1"/>
		</linearGradient>
	</xsl:template>


</xsl:transform>

