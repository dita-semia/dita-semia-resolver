<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet exclude-result-prefixes="ditaarch opentopic ds" version="2.0"
	xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:ds="org.dita-semia.resolver"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic="http://www.idiominc.com/opentopic"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	
	<xsl:variable name="DL_OUTPUTCLASS_TREE" 		as="xs:string">tree</xsl:variable>
	<xsl:variable name="DL_OUTPUTCLASS_DT_WIDTH" 	as="xs:string">^dt-([0-9]+)$</xsl:variable>
	<xsl:variable name="DL_TREE_BORDER_WIDTH"		as="xs:double">0.3</xsl:variable>
	<xsl:variable name="DL_TREE_INDENT"				as="xs:double">5.0</xsl:variable>
	
	<xsl:variable name="PAGE_WIDTH" 				as="xs:double">165</xsl:variable>
	<xsl:variable name="DEFAULT_TREE_DT_WIDTH" 		as="xs:integer" select="40"/>
	
	
	<xsl:template match="*[contains(@class, ' topic/dl ')][tokenize(@outputclass, '\s+') = $DL_OUTPUTCLASS_TREE]">
		<div>
			<xsl:call-template name="commonattributes"/>
			<xsl:call-template name="ds:dl-width"/>
			
			<xsl:variable name="dt-width-attr"	as="xs:string?" 	select="tokenize(@outputclass, '\s+')[matches(., $DL_OUTPUTCLASS_DT_WIDTH)]"/>
			<xsl:variable name="dt-width" 		as="xs:integer?"	select="if ($dt-width-attr) then xs:integer(replace($dt-width-attr, $DL_OUTPUTCLASS_DT_WIDTH, '$1')) else $DEFAULT_TREE_DT_WIDTH"/>
			
			<xsl:apply-templates mode="dl-tree">
				<xsl:with-param name="dt-width" select="$dt-width" tunnel="yes"/>
			</xsl:apply-templates>
		</div>
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dlentry ')]" mode="dl-tree">
		<xsl:param name="dt-width" as="xs:integer" tunnel="yes"/>
		
		<xsl:variable name="level" as="xs:integer" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) + 1"/>
		
		<div>
			<xsl:call-template name="commonattributes"/>
			<xsl:call-template name="setidaname"/>
			
			<xsl:choose>
				<xsl:when test="exists(*[contains(@class, ' topic/dd ')]/node())">
	
					<!-- formatting as table -->
					<table>
						<xsl:variable name="this-dt-width" as="xs:double" select="($PAGE_WIDTH * $dt-width div 100.0) - ($level * ($DL_TREE_INDENT + $DL_TREE_BORDER_WIDTH))"/>
						
						<tr>
							<td style="width: {$this-dt-width}mm">
								<xsl:apply-templates select="*[contains(@class, ' topic/dt ')]" mode="#current"/>
							</td>
							<td>
								<xsl:apply-templates select="*[contains(@class, ' topic/dd ')]" mode="#current"/>
							</td>
						</tr>
					</table>
					<xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]" mode="#current"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="*[contains(@class, ' topic/dt ') or contains(@class, ' topic/dlentry ')]" mode="#current"/>
				</xsl:otherwise>
			</xsl:choose>
		</div>
		
	</xsl:template>
	
	
	<xsl:template match="*[contains(@class, ' topic/dt ')] | *[contains(@class, ' topic/dd ')]" mode="dl-tree">
		<div>
			<xsl:call-template name="commonattributes"/>
			<xsl:apply-templates mode="#default"/>
		</div>
	</xsl:template>

</xsl:stylesheet>
