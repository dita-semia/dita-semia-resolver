<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:xcr	= "http://www.dita-semia.org/xslt-conref"
    exclude-result-prefixes	= "#all">
	
	<xsl:output method="xml" indent="yes"/>
	
	<xsl:param name="xcr:current" 		as="element()"/>
	
	<xsl:param name="minCount" 			as="xs:integer?"	select="1"/>
	<xsl:param name="sectionTitle" 		as="xs:string?"/>
	<xsl:param name="introduction" 		as="xs:string?"/>
	<xsl:param name="useLiTitle" 		as="xs:string?"/>
	<xsl:param name="showShortdesc" 	as="xs:string?"/>
	<xsl:param name="addXref" 			as="xs:string?"/>
	<xsl:param name="xrefPrefix" 		as="xs:string?"/>
	<xsl:param name="xrefSuffix" 		as="xs:string?"/>
	
	<xsl:include href="urn:dita-semia:xsl:class.xsl"/>
	
	<xsl:template match="/">
<xsl:message select="/"></xsl:message>
		<xsl:variable name="baseTopic"	as="element()?" select="($xcr:current/ancestor::*[contains(@class, $C_TOPIC)])[last()]"/>
    	<xsl:variable name="childList"	as="element()*"	select="$baseTopic/*[contains(@class, $C_TOPIC)]"/>
    	
		<xsl:choose>
			<xsl:when test="count($childList) >= number($minCount)">
				<section class="{$CP_SECTION}">
					
					<!-- section title -->
					<xsl:if test="string($sectionTitle) != ''">
						<title class="{$CP_TITLE}">
							<xsl:value-of select="$sectionTitle"/>
						</title>
					</xsl:if>
					
					<!-- introduction -->
					<xsl:if test="string($introduction) != ''">
						<p class="{$CP_P}">
							<xsl:value-of select="$introduction"/>
						</p>
					</xsl:if>
					
					<!-- list -->
					<ul class="{$CP_UL}">
						<xsl:for-each select="$childList">
							<li class="{$CP_LI}">
								
								<!-- title -->
								<xsl:choose>
									<xsl:when test="not($useLiTitle = 'no')">
										<title class="{$CP_TITLE}">
											<xsl:value-of select="title"/>
										</title>
									</xsl:when>
									<xsl:otherwise>
										<p class="{$CP_P}">
											<xsl:value-of select="title"/>
										</p>
									</xsl:otherwise>
								</xsl:choose>
								
								<!-- shortdesc -->
								<xsl:if test="not($showShortdesc = 'no') and exists(shortdesc)">
									<p class="{$CP_P}">
										<xsl:copy-of select="shortdesc/node()"/>
									</p>
								</xsl:if>
								
								<!-- xref -->
								<xsl:if test="not($addXref = 'no') and exists(@id)">
									<p class="{$CP_P}">
										<xsl:value-of select="$xrefPrefix"/>
										<xref href="#{@id}" format="dita" class="{$CP_XREF}"/>
										<xsl:value-of select="$xrefSuffix"/>
									</p>
								</xsl:if>
							</li>
						</xsl:for-each>
					</ul>
				</section>
			</xsl:when>
			<xsl:otherwise>
				<no-content>(Overview is hidden since less than <xsl:value-of select="$minCount"/> child topics are present.)</no-content>
			</xsl:otherwise>
		</xsl:choose>
    	
    	
    </xsl:template>
</xsl:stylesheet>
