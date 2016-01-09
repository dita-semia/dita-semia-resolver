<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="2.0" 
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:xsi	= "http://www.w3.org/2001/XMLSchema-instance"
	xmlns:err	= "http://www.w3.org/2005/xqt-errors"
	xmlns:svg	= "http://www.w3.org/2000/svg"
	xmlns:xlink	= "http://www.w3.org/1999/xlink"
	xmlns:ds	= "http://www.dita-semia.org"
	exclude-result-prefixes="#all">
	
	
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:class.xsl"/>
	<xsl:include href="urn:dita-semia:xslt-conref:xsl:svg-utility.xsl"/>
	
	<xsl:variable name="GRAPHICS_MIN_WIDTH"						as="xs:double"	select="160"/>
	<xsl:variable name="FONT_FAMILY_DEFAULT"					as="xs:string"	select="'Calibri'"/>
	<xsl:variable name="FONT_SIZE_DEFAULT"						as="xs:integer"	select="10"/>	<!-- pt -->
	<xsl:variable name="TEXT_MARGIN_X"							as="xs:double"	select="1.5"/>
	<xsl:variable name="TEXT_MARGIN_Y"							as="xs:double"	select="0.5"/>
	<xsl:variable name="ARROW_WIDTH"							as="xs:double"	select="0.4"/>
	<xsl:variable name="ARROW_COLOR"							as="xs:string"	select="'rgb(  0,  0,  0)'"/>

	<xsl:variable name="COMPONENT_WIDTH"						as="xs:double"	select="30"/>
	<xsl:variable name="COMPONENT_HEIGHT"						as="xs:double"	select="7"/> <!-- with single text line -->
	<xsl:variable name="COMPONENT_RADIUS"						as="xs:double"	select="2.5"/>
	<xsl:variable name="COMPONENT_MIN_SPACE_X"					as="xs:double"	select="5"/>
	<xsl:variable name="COMPONENT_FRAME_WIDTH"					as="xs:double"	select="0.3"/>
	<xsl:variable name="COMPONENT_BK_COLOR"						as="xs:string"	select="'rgb(238,242,255)'"/>
	<xsl:variable name="COMPONENT_FRAME_COLOR"					as="xs:string"	select="'rgb(  0,  0,  0)'"/>
	<xsl:variable name="COMPONENT_FONT_FAMILY"					as="xs:string"	select="$FONT_FAMILY_DEFAULT"/>
	<xsl:variable name="COMPONENT_FONT_SIZE"					as="xs:integer"	select="$FONT_SIZE_DEFAULT"/>
	<xsl:variable name="COMPONENT_GRADIENT_ID"					as="xs:string"	select="'GradientComponent'"/>
	<xsl:variable name="COMPONENT_LINE_WIDTH"					as="xs:double"	select="0.5"/>
	<xsl:variable name="COMPONENT_LINE_DASHARRAY"				as="xs:string"	select="'7 7'"/>	<!-- in Px -->
	<xsl:variable name="COMPONENT_LINE_COLOR"					as="xs:string"	select="'rgb(128,128,128)'"/>
	
	<xsl:variable name="ACTION_WIDTH"							as="xs:double"	select="3"/>
	<xsl:variable name="ACTION_SPACE_Y"							as="xs:double"	select="3"/>
	<xsl:variable name="ACTION_RADIUS"							as="xs:double"	select="0.5"/>
	<xsl:variable name="ACTION_FRAME_WIDTH"						as="xs:double"	select="0.3"/>
	<xsl:variable name="ACTION_BK_COLOR"						as="xs:string"	select="'rgb(134,164,200)'"/>
	<xsl:variable name="ACTION_FRAME_COLOR"						as="xs:string"	select="'rgb(  0,  0,  0)'"/>
	<xsl:variable name="ACTION_FONT_FAMILY"						as="xs:string"	select="$FONT_FAMILY_DEFAULT"/>
	<xsl:variable name="ACTION_FONT_SIZE"						as="xs:integer"	select="$FONT_SIZE_DEFAULT"/>
	<xsl:variable name="ACTION_GRADIENT_ID"						as="xs:string"	select="'GradientAction'"/>
	
	<xsl:variable name="CALL_WIDTH"								as="xs:double"	select="$ACTION_WIDTH"/>
	<xsl:variable name="CALL_HEIGHT"							as="xs:double"	select="15"/>
	<xsl:variable name="CALL_SPACE_Y"							as="xs:double"	select="3"/>
	<xsl:variable name="CALL_RADIUS"							as="xs:double"	select="$ACTION_RADIUS"/>
	<xsl:variable name="CALL_FRAME_WIDTH"						as="xs:double"	select="$ACTION_FRAME_WIDTH"/>
	<xsl:variable name="CALL_BK_COLOR"							as="xs:string"	select="$ACTION_BK_COLOR"/>
	<xsl:variable name="CALL_FRAME_COLOR"						as="xs:string"	select="$ACTION_FRAME_COLOR"/>
	<xsl:variable name="CALL_RESPONSE_ARROW_DASHARRAY"			as="xs:string"	select="'5 5'"/>	<!-- in Px -->
	<xsl:variable name="CALL_FONT_FAMILY"						as="xs:string"	select="$FONT_FAMILY_DEFAULT"/>
	<xsl:variable name="CALL_FONT_SIZE"							as="xs:integer"	select="$FONT_SIZE_DEFAULT - 1"/>
	<xsl:variable name="CALL_GRADIENT_ID"						as="xs:string"	select="'GradientAufruf'"/>
	<xsl:variable name="CALL_ARROW_COLOR"						as="xs:string"	select="$ACTION_FRAME_COLOR"/>
	<xsl:variable name="CALL_ARROW_WIDTH"						as="xs:double"	select="$ACTION_FRAME_WIDTH"/>
	<xsl:variable name="CALL_ARROW_ID"							as="xs:string"	select="'CallArrow'"/>


	<!-- ========== Template: DrawSequenceDiagram ========== -->
	<xsl:template name="DrawSequenceDiagram">
		<!-- the name of the component that performs the calls -->
		<xsl:param name="callingComponentName"	as="xs:string"/>
		<!-- a list of function calls with format "<component>.<function>" each -->
		<xsl:param name="callList"				as="xs:string+"/>
		
		<xsl:variable name="componentList" as="xs:string+">
			<xsl:value-of select="$callingComponentName"/>
			<xsl:for-each-group select="$callList" group-by="substring-before(., '.')">
				<xsl:value-of select="current-grouping-key()"/>
			</xsl:for-each-group>
		</xsl:variable>
		
		<xsl:variable name="callCount" 		as="xs:integer" select="count($callList)"/>
		<xsl:variable name="componentCount" as="xs:integer" select="count($componentList)"/>
		
		<xsl:variable name="componentsWidth" 	as="xs:double" 	select="$componentCount * $COMPONENT_WIDTH"/>
		<!-- calculate the spacing to either fill the GRAPHIC_MIN_WIDTH or use the COMPONENT_MIN_SPACE -->
		<xsl:variable name="componentSpace"		as="xs:double" 	select="max(($COMPONENT_MIN_SPACE_X, ($GRAPHICS_MIN_WIDTH - $componentsWidth) div ($componentCount - 1)))"/>
		<xsl:variable name="graphicsWidth"		as="xs:double"	select="$componentsWidth + (($componentCount - 1) * $componentSpace)"/>
		
		<xsl:variable name="graphicsHeight"		as="xs:double"	select="$COMPONENT_HEIGHT + (2 * $ACTION_SPACE_Y) + ($callCount * ($CALL_SPACE_Y + $CALL_HEIGHT)) + $CALL_SPACE_Y"/>
		
		<fig class="{$CP_FIG}">
			<svg-container class="{$CP_SVG_CONTAINER}">
				<xsl:call-template name="SvgRoot">
					<xsl:with-param name="widthInMm"	select="$graphicsWidth"/>
					<xsl:with-param name="heightInMm"	select="$graphicsHeight"/>
					<xsl:with-param name="content">
						
						<xsl:call-template name="Definitions"/>
						
						<xsl:call-template name="DrawComponents">
							<xsl:with-param name="componentList" 	select="$componentList"/>
							<xsl:with-param name="componentSpace"	select="$componentSpace"/>
							<xsl:with-param name="graphicsHeight"	select="$graphicsHeight"/>
						</xsl:call-template>
						
						<xsl:call-template name="DrawCallingAction">
							<xsl:with-param name="yOffset"			select="$COMPONENT_HEIGHT"/>
							<xsl:with-param name="graphicsHeight"	select="$graphicsHeight"/>
						</xsl:call-template>
						
						<xsl:call-template name="DrawCalls">
							<xsl:with-param name="callList" 		select="$callList"/>
							<xsl:with-param name="componentList" 	select="$componentList"/>
							<xsl:with-param name="componentSpace"	select="$componentSpace"/>
							<xsl:with-param name="yOffset"			select="$COMPONENT_HEIGHT + $ACTION_SPACE_Y + $CALL_SPACE_Y"/>
						</xsl:call-template>
						
					</xsl:with-param>
				</xsl:call-template>
			</svg-container>
		</fig>
		
	</xsl:template>
	
	
	<!-- ========== Template: Definitions ========== -->
	<xsl:template name="Definitions">
		
		<defs xmlns="http://www.w3.org/2000/svg">
			<xsl:call-template name="SvgGradient">
				<xsl:with-param name="id"		select="$COMPONENT_GRADIENT_ID"/>
				<xsl:with-param name="color"	select="$COMPONENT_BK_COLOR"/>
			</xsl:call-template>
	
			<xsl:call-template name="SvgGradient">
				<xsl:with-param name="id"			select="$ACTION_GRADIENT_ID"/>
				<xsl:with-param name="color"		select="$ACTION_BK_COLOR"/>
				<xsl:with-param name="direction"	select="$SvgGradient.HORIZONTAL"/>
				<xsl:with-param name="adaptation1"	select="0.0"/>
				<xsl:with-param name="adaptation2"	select="0.8"/>
				<xsl:with-param name="adaptation3"	select="0.0"/>
			</xsl:call-template>
			
			<xsl:call-template name="SvgGradient">
				<xsl:with-param name="id"			select="$CALL_GRADIENT_ID"/>
				<xsl:with-param name="color"		select="$CALL_BK_COLOR"/>
				<xsl:with-param name="direction"	select="$SvgGradient.HORIZONTAL"/>
				<xsl:with-param name="adaptation1"	select="0.0"/>
				<xsl:with-param name="adaptation2"	select="0.8"/>
				<xsl:with-param name="adaptation3"	select="0.0"/>
			</xsl:call-template>
	
			<xsl:call-template name="SvgGradient">
				<xsl:with-param name="id"		select="$ACTION_GRADIENT_ID"/>
				<xsl:with-param name="color"	select="$ACTION_BK_COLOR"/>
			</xsl:call-template>
	
			<xsl:call-template name="SvgMarkerArrow">
				<xsl:with-param name="id"			select="$CALL_ARROW_ID"/>
				<xsl:with-param name="lineColor"	select="$CALL_ARROW_COLOR"/>
			</xsl:call-template>
		</defs>

	</xsl:template>
	
	
	<!-- ========== Template: DrawComponents ========== -->
	<xsl:template name="DrawComponents" as="element()*">
		<xsl:param name="componentList"		as="xs:string+"/>
		<xsl:param name="componentSpace"	as="xs:double"/>
		<xsl:param name="graphicsHeight"	as="xs:double"/>
		
		<xsl:for-each select="$componentList">
			<xsl:variable name="offsetX"	as="xs:double" select="(position() - 1) * ($COMPONENT_WIDTH + $componentSpace)"/>
			
			<xsl:call-template name="SvgRect">
				<xsl:with-param name="xInMm"		select="$offsetX"/>
				<xsl:with-param name="widthInMm"	select="$COMPONENT_WIDTH"/>
				<xsl:with-param name="heightInMm"	select="$COMPONENT_HEIGHT"/>
				<xsl:with-param name="radiusXInMm"	select="$COMPONENT_RADIUS"/>
				<xsl:with-param name="strokeWidth"	select="$COMPONENT_FRAME_WIDTH"/>
				<xsl:with-param name="fillUrl"		select="$COMPONENT_GRADIENT_ID"/>
				<xsl:with-param name="strokeColor"	select="$COMPONENT_FRAME_COLOR"/>
			</xsl:call-template>
			
			<xsl:call-template name="SvgText">
				<xsl:with-param name="xInMm"		select="$offsetX + ($COMPONENT_WIDTH div 2)"/>
				<xsl:with-param name="yInMm"		select="$COMPONENT_HEIGHT div 2"/>
				<xsl:with-param name="textAnchor"	select="$SVG.TEXT_ANCHOR_MIDDLE"/>
				<xsl:with-param name="dy"			select="$SVG.DY_CENTER"/>
				<xsl:with-param name="fontFamily"	select="$COMPONENT_FONT_FAMILY"/>
				<xsl:with-param name="fontSize"		select="$COMPONENT_FONT_SIZE"/>
				<xsl:with-param name="text"			select="."/>
			</xsl:call-template>
			
			<xsl:call-template name="SvgLine">
				<xsl:with-param name="x1InMm"			select="$offsetX + ($COMPONENT_WIDTH div 2)"/>
				<xsl:with-param name="y1InMm"			select="$COMPONENT_HEIGHT"/>
				<xsl:with-param name="x2InMm"			select="$offsetX + ($COMPONENT_WIDTH div 2)"/>
				<xsl:with-param name="y2InMm"			select="$graphicsHeight"/>
				<xsl:with-param name="strokeColor"		select="$COMPONENT_LINE_COLOR"/>
				<xsl:with-param name="strokeWidth"		select="$COMPONENT_LINE_WIDTH"/>
				<xsl:with-param name="strokeDasharray"	select="$COMPONENT_LINE_DASHARRAY"/>
			</xsl:call-template>
				
		</xsl:for-each>
	</xsl:template>
	
	
	<!-- ========== Template: DrawCallingAction ========== -->
	<xsl:template name="DrawCallingAction">
		<xsl:param name="yOffset"			as="xs:double"/>
		<xsl:param name="graphicsHeight"	as="xs:double"/>
		
		<xsl:call-template name="SvgRect">
			<xsl:with-param name="xInMm"		select="($COMPONENT_WIDTH div 2) - ($ACTION_WIDTH div 2)"/>
			<xsl:with-param name="yInMm"		select="$yOffset +  $ACTION_SPACE_Y"/>
			<xsl:with-param name="widthInMm"	select="$ACTION_WIDTH"/>
			<xsl:with-param name="heightInMm"	select="$graphicsHeight - $yOffset - (2 * $ACTION_SPACE_Y)"/>
			<xsl:with-param name="radiusXInMm"	select="$ACTION_RADIUS"/>
			<xsl:with-param name="strokeWidth"	select="$ACTION_FRAME_WIDTH"/>
			<xsl:with-param name="strokeColor"	select="$ACTION_FRAME_COLOR"/>
			<xsl:with-param name="fillUrl"		select="$ACTION_GRADIENT_ID"/>
		</xsl:call-template>

	</xsl:template>
	
	
	<!-- ========== Template: DrawCalls ========== -->
	<xsl:template name="DrawCalls">
		<xsl:param name="callList"			as="xs:string+"/>
		<xsl:param name="componentList"		as="xs:string+"/>
		<xsl:param name="componentSpace"	as="xs:double"/>
		<xsl:param name="yOffset"			as="xs:double"/>
		
		<xsl:for-each select="$callList">
			<xsl:call-template name="DrawSingleCall">
				<xsl:with-param name="component"		select="substring-before(., '.')"/>
				<xsl:with-param name="function"			select="substring-after(., '.')"/>
				<xsl:with-param name="componentList"	select="$componentList"/>
				<xsl:with-param name="componentSpace"	select="$componentSpace"/>
				<xsl:with-param name="yOffset"			select="$yOffset + ((position() - 1) * ($CALL_SPACE_Y + $CALL_HEIGHT))"/>				
			</xsl:call-template>
		</xsl:for-each>
		
	</xsl:template>
	
	
	<!-- ========== Template: DrawSingleCall ========== -->
	<xsl:template name="DrawSingleCall">
		<xsl:param name="component"			as="xs:string"/>
		<xsl:param name="function"			as="xs:string"/>
		<xsl:param name="componentList"		as="xs:string+"/>
		<xsl:param name="componentSpace"	as="xs:double"/>
		<xsl:param name="yOffset"			as="xs:double"/>
		<xsl:param name="hasResponse"		as="xs:boolean"	select="true()"/>

		<xsl:variable name="xOffset"	as="xs:double" select="($COMPONENT_WIDTH div 2) + ($ACTION_WIDTH div 2)"/>

		<!-- function name -->
		<xsl:call-template name="SvgText">
			<xsl:with-param name="xInMm"		select="$xOffset + $TEXT_MARGIN_X"/>
			<xsl:with-param name="yInMm"		select="$yOffset + $TEXT_MARGIN_Y"/>
			<xsl:with-param name="fontFamily"	select="$CALL_FONT_FAMILY"/>
			<xsl:with-param name="fontSize"		select="$CALL_FONT_SIZE"/>
			<xsl:with-param name="dy"			select="$SVG.DY_BOTTOM"/>
			<xsl:with-param name="text"			select="$function"/>
		</xsl:call-template>

		<xsl:variable name="textHeight"		as="xs:double"	select="ds:ptToMm($CALL_FONT_SIZE) + (2 * $TEXT_MARGIN_Y)"/>
		<xsl:variable name="componentIndex"	as="xs:integer"	select="index-of($componentList, $component)"/>
		<xsl:variable name="componentXPos" 	as="xs:double"	select="($componentIndex - 1) * ($componentSpace + $COMPONENT_WIDTH) + ($COMPONENT_WIDTH div 2)"/>

		<!-- calling arrow -->
		<xsl:call-template name="SvgLine">
			<xsl:with-param name="x1InMm"		select="$xOffset"/>
			<xsl:with-param name="y1InMm"		select="$yOffset + $textHeight"/>
			<xsl:with-param name="x2InMm"		select="$componentXPos - ($CALL_WIDTH div 2) - (3 * $CALL_ARROW_WIDTH)"/>
			<xsl:with-param name="y2InMm"		select="$yOffset + $textHeight"/>
			<xsl:with-param name="strokeWidth"	select="$CALL_ARROW_WIDTH"/>
			<xsl:with-param name="strokeColor"	select="$CALL_ARROW_COLOR"/>
			<xsl:with-param name="markerEndUrl"	select="$CALL_ARROW_ID"/>
		</xsl:call-template>
		
		<!-- action bar -->
		<xsl:call-template name="SvgRect">
			<xsl:with-param name="xInMm"		select="$componentXPos - ($CALL_WIDTH div 2)"/>
			<xsl:with-param name="yInMm"		select="$yOffset + $textHeight"/>
			<xsl:with-param name="widthInMm"	select="$ACTION_WIDTH"/>
			<xsl:with-param name="heightInMm"	select="$CALL_HEIGHT - $textHeight"/>
			<xsl:with-param name="radiusXInMm"	select="$CALL_RADIUS"/>
			<xsl:with-param name="strokeWidth"	select="$CALL_FRAME_WIDTH"/>
			<xsl:with-param name="strokeColor"	select="$CALL_FRAME_COLOR"/>
			<xsl:with-param name="fillUrl"		select="$CALL_GRADIENT_ID"/>
		</xsl:call-template>

		<!-- response arror -->
		<xsl:if test="$hasResponse">
			<xsl:call-template name="SvgLine">
				<xsl:with-param name="x1InMm"			select="$componentXPos - ($CALL_WIDTH div 2)"/>
				<xsl:with-param name="y1InMm"			select="$yOffset + $CALL_HEIGHT"/>
				<xsl:with-param name="x2InMm"			select="$xOffset + (3 * $CALL_ARROW_WIDTH)"/>
				<xsl:with-param name="y2InMm"			select="$yOffset + $CALL_HEIGHT"/>
				<xsl:with-param name="strokeWidth"		select="$CALL_ARROW_WIDTH"/>
				<xsl:with-param name="strokeColor"		select="$CALL_ARROW_COLOR"/>
				<xsl:with-param name="strokeDasharray"	select="$CALL_RESPONSE_ARROW_DASHARRAY"/>
				<xsl:with-param name="markerEndUrl"		select="$CALL_ARROW_ID"/>
			</xsl:call-template>
		</xsl:if>
		
	</xsl:template>
	
</xsl:transform>
