<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:fo	= "http://www.w3.org/1999/XSL/Format"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	exclude-result-prefixes="#all">
	
	
	<xsl:param name="highlight.conbat.bk-col" as="xs:string?"/>
	
	<xsl:attribute-set name="conbat-bk-col">
		<xsl:attribute name="background-color" select="$highlight.conbat.bk-col"/>
	</xsl:attribute-set>

	
	<xsl:template match="*[contains(@class, ' topic/thead ')]/*[contains(@class, ' topic/row ')]/*[contains(@class, ' topic/entry ')][processing-instruction('CBA')]">
		<xsl:choose>
			<xsl:when test="exists($highlight.conbat.bk-col)">
				<fo:table-cell xsl:use-attribute-sets="thead.row.entry conbat-bk-col">
					<xsl:call-template name="commonattributes"/>
					<xsl:call-template name="applySpansAttrs"/>
					<xsl:call-template name="applyAlignAttrs"/>
					<xsl:call-template name="generateTableEntryBorder"/>
					<fo:block xsl:use-attribute-sets="thead.row.entry__content">
						<xsl:apply-templates select="." mode="ancestor-start-flag"/>
						<xsl:call-template name="processEntryContent"/>
						<xsl:apply-templates select="." mode="ancestor-end-flag"/>
					</fo:block>
				</fo:table-cell>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="*[contains(@class, ' topic/sthead ')][processing-instruction('CBA')]">
		<xsl:param name="number-cells" as="xs:integer">
			<xsl:apply-templates select="../*[1]" mode="count-max-simpletable-cells"/>
		</xsl:param>
		
		<xsl:choose>
			<xsl:when test="exists($highlight.conbat.bk-col)">
				<fo:table-header xsl:use-attribute-sets="sthead conbat-bk-col">
					<xsl:call-template name="commonattributes"/>
					<fo:table-row xsl:use-attribute-sets="sthead__row">
						<xsl:apply-templates select="*[contains(@class, ' topic/stentry ')]"/>
						<xsl:variable name="row-cell-count" select="count(*[contains(@class, ' topic/stentry ')])" as="xs:integer"/>
						<xsl:if test="$row-cell-count &lt; $number-cells">
							<xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
								<xsl:with-param name="fill-in-count" select="$number-cells - $row-cell-count"/>
							</xsl:apply-templates>
						</xsl:if>
					</fo:table-row>
				</fo:table-header>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*[exists(processing-instruction('CBA'))][contains(@class, ' topic/p ') or contains(@class, ' topic/title ')]" priority="1">
		<xsl:choose>
			<xsl:when test="exists($highlight.conbat.bk-col)">
				<fo:block xsl:use-attribute-sets="conbat-bk-col">
					<xsl:next-match/>
				</fo:block>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template match="*[exists(processing-instruction('CBA'))][contains(@class, ' topic/ph ')]" priority="1">
		<xsl:choose>
			<xsl:when test="exists($highlight.conbat.bk-col)">
				<fo:inline xsl:use-attribute-sets="conbat-bk-col">
					<xsl:next-match/>
				</fo:inline>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>