<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="ditaarch opentopic ds" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_TABLE" 		as="xs:string">table</xsl:variable>
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_TABLE]">
		<table>
			<xsl:call-template name="commonattributes"/>
			<!--<colgroup>
				<col style="width:35%"/>
				<col style="width:65%"/>
			</colgroup>-->
			<xsl:if test="*[contains(@class, ' topic/dlhead ')]">
				<thead class="thead">
					<xsl:apply-templates select="*[contains(@class, ' topic/dlhead ')]" mode="dl-table"/>
				</thead>
			</xsl:if>
			<tbody class="tbody">
				<xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]" mode="dl-table"/>
			</tbody>
		</table>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dlhead ')] | *[contains(@class, ' topic/dlentry ')]" mode="dl-table">
		<tr>
			<xsl:call-template name="commonattributes"/>
			<xsl:call-template name="setidaname"/>
			<xsl:apply-templates mode="#current"/>
		</tr>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dthd ')] | *[contains(@class, ' topic/ddhd ')]" mode="dl-table">
		<th>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</th>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dt ')] | *[contains(@class, ' topic/dd ')]" mode="dl-table">
		<td>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</td>
	</xsl:template>
	
	
	<!-- mode: get-output-class -->
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_TABLE]" mode="get-output-class">
		<xsl:text>table</xsl:text>
		<xsl:next-match/>
	</xsl:template>
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][@outputclass = $DL_OUTPUTCLASS_TABLE]
		//*[contains(@class, ' topic/dthd ') or contains(@class, ' topic/ddhd ') or contains(@class, ' topic/dt ') or contains(@class, ' topic/dd ')]" mode="get-output-class">
		<xsl:text>entry</xsl:text>
		<xsl:next-match/>
	</xsl:template>
	
		
</xsl:stylesheet>
