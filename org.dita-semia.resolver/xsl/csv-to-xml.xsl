<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
	xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform"
	xmlns:xs	= "http://www.w3.org/2001/XMLSchema"
	xmlns:ds	= "http://www.dita-semia.org"
    exclude-result-prefixes	= "#all"
	expand-text				= "yes">
	
	
	<xsl:variable name="CSV_QUOT" 			as="xs:string" select="'&quot;'"/>
	<xsl:variable name="CSV_ROW_DELIMITER" as="xs:string" select="'&#x0A;'"/>
	<xsl:variable name="CSV_COL_DELIMITER" as="xs:string" select="';'"/>

	<xsl:function name="ds:csvToXml" as="element(row)*">
		<xsl:param name="csvCode" as="xs:string"/>
		
		<!-- split into tokens (lexical analysis) -->
		<xsl:variable name="tokenlist" as="xs:string*">
			<xsl:analyze-string select="$csvCode" regex="[&quot;&#x0D;{$CSV_ROW_DELIMITER}{$CSV_COL_DELIMITER}]" flags="m">
				<xsl:matching-substring>
					<xsl:if test=". != '&#x0D;'">
						<xsl:sequence select="."/>
					</xsl:if>
				</xsl:matching-substring>
				<xsl:non-matching-substring>
					<xsl:sequence select="."/>
				</xsl:non-matching-substring>
			</xsl:analyze-string>
		</xsl:variable>
		
		<!-- create colums, keep column and row delimiters -->
		<xsl:variable name="cellList" as="node()*">
			<xsl:call-template name="ParseCsvToCells">
				<xsl:with-param name="tokenlist"	select="$tokenlist"/>
			</xsl:call-template>
		</xsl:variable>
		
		<!-- group columns into rows -->
		<xsl:call-template name="MergeCellsToRows">
			<xsl:with-param name="cellList"	select="$cellList"/>
		</xsl:call-template>
		
	</xsl:function>
	
	
	<xsl:template name="ParseCsvToCells">
		<xsl:param name="currentContent"	as="xs:string?"/>
		<xsl:param name="tokenlist"			as="xs:string*"/>
		
		<xsl:variable name="token" 		as="xs:string?" select="$tokenlist[1]"/>
		<xsl:variable name="nextToken" 	as="xs:string?"	select="$tokenlist[2]"/>
		
		<xsl:choose>
			<xsl:when test="exists($currentContent)">
				<!-- current state is within cell -->
				
				<xsl:choose>
					<xsl:when test="empty($token)">
						<!-- end of tokenlist -> create cell with current content -->
						<cell>
							<xsl:sequence select="$currentContent"/>
						</cell>
					</xsl:when>
					<xsl:when test="($token = $CSV_QUOT) and ($nextToken = $CSV_QUOT)">
						<!-- double quot -> merge into single quot and add to cell content -->
						<xsl:call-template name="ParseCsvToCells">
							<xsl:with-param name="currentContent" 	select="concat($currentContent, $token)"/>
							<xsl:with-param name="tokenlist"		select="$tokenlist[position() > 2]"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="($token = $CSV_QUOT)">
						<!-- single quot = end of cell -> create cell -->
						<cell>
							<xsl:sequence select="$currentContent"/>
						</cell>
						<xsl:call-template name="ParseCsvToCells">
							<xsl:with-param name="tokenlist"		select="$tokenlist[position() > 1]"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<!-- add everything else to the current cell content -->
						<xsl:call-template name="ParseCsvToCells">
							<xsl:with-param name="currentContent" 	select="concat($currentContent, $token)"/>
							<xsl:with-param name="tokenlist"		select="$tokenlist[position() > 1]"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:when>
			<xsl:otherwise>
				
				<xsl:choose>
					<xsl:when test="empty($token)">
						<!-- end of loop -->
					</xsl:when>
					<xsl:when test="$token = $CSV_QUOT">
						<!-- start new cell -->
						<xsl:call-template name="ParseCsvToCells">
							<xsl:with-param name="currentContent"	select="''"/>
							<xsl:with-param name="tokenlist"		select="$tokenlist[position() > 1]"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$token = $CSV_COL_DELIMITER">
						<!-- just ignore it -->
						<xsl:call-template name="ParseCsvToCells">
							<xsl:with-param name="tokenlist"		select="$tokenlist[position() > 1]"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$token = $CSV_ROW_DELIMITER">
						<!-- keep row-delimiter -->
						<xsl:value-of select="$token"/>
						<xsl:call-template name="ParseCsvToCells">
							<xsl:with-param name="tokenlist"		select="$tokenlist[position() > 1]"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<!-- text content -> create new cell with this content -->
						<cell>
							<xsl:sequence select="$token"/>
						</cell>
						<xsl:call-template name="ParseCsvToCells">
							<xsl:with-param name="tokenlist"		select="$tokenlist[position() > 1]"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>


	<xsl:template name="MergeCellsToRows" as="element(row)*">
		<xsl:param name="cellList" as="node()*"/>
		
		<xsl:variable name="delimiterPosList" as="xs:integer+">
			<xsl:sequence select="0"/>
			<xsl:sequence select="for $i in 1 to count($cellList) return if ($cellList[$i] = $CSV_ROW_DELIMITER) then $i else ()"/>
			<xsl:if test="$cellList[count($cellList)] != $CSV_ROW_DELIMITER">
				<xsl:sequence select="count($cellList) + 1"/>	
			</xsl:if>
		</xsl:variable> 
		
		<xsl:for-each select="1 to count($delimiterPosList) - 1">
			<xsl:variable name="rowIndex" as="xs:integer" select="."/>
			<xsl:variable name="startIndex" as="xs:integer" select="$delimiterPosList[$rowIndex]"/>
			<xsl:variable name="endIndex" 	as="xs:integer" select="$delimiterPosList[$rowIndex + 1]"/>
			<row>
				<xsl:sequence select="$cellList[($endIndex > position()) and (position() > $startIndex)]"/>
			</row>
		</xsl:for-each>
		
	</xsl:template>

</xsl:stylesheet>
