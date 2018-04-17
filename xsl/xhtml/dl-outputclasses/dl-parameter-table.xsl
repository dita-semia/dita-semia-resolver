<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="ditaarch opentopic ds" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_PARAMETER_TABLE" 		as="xs:string">parameter-table</xsl:variable>
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_PARAMETER_TABLE]">
		<table>
			<xsl:call-template name="commonattributes"/>
			<!--<colgroup>
				<col style="width:25%"/>
				<col style="width:75%"/>
			</colgroup>-->
			<xsl:apply-templates mode="dl-parameter-table"/>
		</table>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dlhead ')] | *[contains(@class, ' topic/dlentry ')]" mode="dl-parameter-table">
		<tr>
			<xsl:call-template name="commonattributes"/>
			<xsl:call-template name="setidaname"/>
			<xsl:apply-templates mode="#current"/>
		</tr>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dlhead ')] | *[contains(@class, ' topic/dlentry ')]" mode="dl-parameter-table">
		<tr>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#current"/>
		</tr>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dthd ')] | *[contains(@class, ' topic/ddhd ')]" mode="dl-parameter-table">
		<th>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</th>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dt ')] | *[contains(@class, ' topic/dd ')]" mode="dl-parameter-table">
		<td>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</td>
	</xsl:template>
	
</xsl:stylesheet>
