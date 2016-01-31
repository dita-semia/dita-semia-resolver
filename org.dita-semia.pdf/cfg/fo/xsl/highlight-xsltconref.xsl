<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:fo	= "http://www.w3.org/1999/XSL/Format"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
	exclude-result-prefixes="#all">
	
	
	<xsl:param name="highlight.xslt-conref.bk-col" as="xs:string?"/>
	
	
	<xsl:attribute-set name="xslt-conref-bk-col">
		<xsl:attribute name="background-color" select="$highlight.xslt-conref.bk-col"/>
	</xsl:attribute-set>
	
	<xsl:template match="*[@xcr:xsl][contains(@class, ' topic/p ')]
						|*[@xcr:xsl][contains(@class, ' topic/topic ')]
						|*[@xcr:xsl][contains(@class, ' topic/div ')]
						|*[@xcr:xsl][contains(@class, ' topic/section ')]
						|*[@xcr:xsl][contains(@class, ' topic/table ')]
						|*[@xcr:xsl][contains(@class, ' topic/simpletable ')]
						|*[@xcr:xsl][contains(@class, ' topic/sl ')]
						|*[@xcr:xsl][contains(@class, ' topic/ul ')]
						|*[@xcr:xsl][contains(@class, ' topic/ol ')]
						|*[@xcr:xsl][contains(@class, ' topic/fig ')]
						|*[@xcr:xsl][contains(@class, ' topic/pre ')]" priority="1">
		<xsl:choose>
			<xsl:when test="exists($highlight.xslt-conref.bk-col)">
				<fo:block xsl:use-attribute-sets="xslt-conref-bk-col">
					<xsl:next-match/>
				</fo:block>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="*[@xcr:xsl][contains(@class,' pr-d/codeblock ')]" priority="2">
		<xsl:choose>
			<xsl:when test="exists($highlight.xslt-conref.bk-col)">
				<xsl:call-template name="generateAttrLabel"/>
				<fo:block xsl:use-attribute-sets="codeblock xslt-conref-bk-col">
					<xsl:call-template name="commonattributes"/>
					<xsl:call-template name="setFrame"/>
					<xsl:call-template name="setScale"/>
					<xsl:variable name="codeblock.line-number" as="xs:boolean">
						<xsl:apply-templates select="." mode="codeblock.generate-line-number"/>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$codeblock.wrap or $codeblock.line-number">
							<xsl:variable name="content" as="node()*">
								<xsl:apply-templates/>
							</xsl:variable>
							<xsl:choose>
								<xsl:when test="$codeblock.line-number">
									<xsl:variable name="buf" as="document-node()">
										<xsl:document>
											<xsl:processing-instruction name="line-number"/>
											<xsl:apply-templates select="$content" mode="codeblock.line-number"/>
										</xsl:document>
									</xsl:variable>
									<xsl:variable name="line-count" select="count($buf/descendant::processing-instruction('line-number'))"/>
									<xsl:apply-templates select="$buf" mode="codeblock">
										<xsl:with-param name="line-count" select="$line-count" tunnel="yes"/>
									</xsl:apply-templates>    
								</xsl:when>
								<xsl:otherwise>
									<xsl:apply-templates select="$content" mode="codeblock"/>
								</xsl:otherwise>
							</xsl:choose>                
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates/>
						</xsl:otherwise>
					</xsl:choose>
				</fo:block>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>