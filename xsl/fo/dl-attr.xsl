<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet version="2.0"
	xmlns:xs				= "http://www.w3.org/2001/XMLSchema" 
	xmlns:xsl				= "http://www.w3.org/1999/XSL/Transform" 
	xmlns:fo				= "http://www.w3.org/1999/XSL/Format"
	xmlns:ds				= "http://www.dita-semia.org"
	exclude-result-prefixes	= "#all">
	
	<xsl:variable name="KEEP_TABLE_ROW_VALUE" as="xs:string" select="'always'"/>	<!-- numeric values not supported by FOP -->
	
	<xsl:attribute-set name="ds:dlentry.dt__content">
		<xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="keep-with-next">always</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dlentry.dd__content"/>
	
	<xsl:attribute-set name="ds:dl-common-table-cell" use-attribute-sets="strow.stentry">
		<xsl:attribute name="padding-before"	>3pt</xsl:attribute>
		<xsl:attribute name="padding-after"		>3pt</xsl:attribute>
	</xsl:attribute-set>
	
	
	<!-- dl with outputclass "tree" -->
	
	<xsl:variable name="DL_TREE_BORDER_WIDTH"	as="xs:string">0.3mm</xsl:variable>
	<xsl:variable name="DL_TREE_INDENT"			as="xs:string">5.0mm</xsl:variable>
	<xsl:variable name="DL_TREE_WIDOWS"			as="xs:integer">5</xsl:variable>
	<xsl:variable name="DL_TREE_ORPHANS"		as="xs:integer">5</xsl:variable>
	
	<xsl:attribute-set name="ds:dl-tree">
		<xsl:attribute name="width"				>100%</xsl:attribute>
		<xsl:attribute name="font-size"			>11pt</xsl:attribute>
		<xsl:attribute name="space-before"		>2.5mm</xsl:attribute>
		<xsl:attribute name="space-after"		>2.5mm</xsl:attribute>
		<xsl:attribute name="background-color"	>transparent</xsl:attribute>
		<xsl:attribute name="border-color"		>rgb(128,128,128)</xsl:attribute>
		<xsl:attribute name="border-width"		><xsl:value-of select="$DL_TREE_BORDER_WIDTH"/></xsl:attribute>
		<xsl:attribute name="border-style"		>solid</xsl:attribute>
	</xsl:attribute-set>
	
	
	<xsl:attribute-set name="ds:dlhead-tree" use-attribute-sets="ds:dlentry-tree">
		<xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dthd-tree" use-attribute-sets="ds:dt-tree">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:ddhd-tree" use-attribute-sets="ds:dd-tree">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dlentry-tree">
		<xsl:attribute name="background-color"		>transparent</xsl:attribute>
		<xsl:attribute name="border-color"			>rgb(128,128,128)</xsl:attribute>
		<xsl:attribute name="border-width"			><xsl:value-of select="$DL_TREE_BORDER_WIDTH"/></xsl:attribute>
		<!-- the bottom line is required for page breaks and should overlap with the top line of the following entry. -->
		<xsl:attribute name="margin-bottom"			>-<xsl:value-of select="$DL_TREE_BORDER_WIDTH"/></xsl:attribute>
		<xsl:attribute name="border-bottom-style"	>solid</xsl:attribute>
		<!-- no padding-top here since it would mess up the middle border with outputclass 'table-frame' -->
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dl-tree-cell">
		<xsl:attribute name="padding-top"			>0.1em</xsl:attribute>
		<xsl:attribute name="padding-bottom"		>0.1em</xsl:attribute>
	</xsl:attribute-set>
		
	<xsl:attribute-set name="ds:dt-tree">
		
		<xsl:attribute name="margin-left"					>1.0mm</xsl:attribute>	<!-- padding-left doesn't work!? -->
		<xsl:attribute name="padding-right"					>1.0mm</xsl:attribute>
		<xsl:attribute name="padding-top"					>0.5mm</xsl:attribute>
		<xsl:attribute name="keep-together.within-column"	>always</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-tree">
		<xsl:attribute name="margin-left"					>1.0mm</xsl:attribute>	<!-- padding-left doesn't work!? -->
		<xsl:attribute name="padding-right"					>1.0mm</xsl:attribute>
		<xsl:attribute name="padding-top"					>0.5mm</xsl:attribute>
		<xsl:attribute name="keep-together.within-column"	>always</xsl:attribute>
	</xsl:attribute-set>
	
	
	<!-- dl with outputclass "header-table" -->
	
	<xsl:variable name="DL_HEADER_TABLE_INNER_BORDER_WIDTH"	as="xs:string">0.1mm</xsl:variable>
	<xsl:variable name="DL_HEADER_TABLE_OUTER_BORDER_WIDTH"	as="xs:string">0.3mm</xsl:variable>
	<!--<xsl:variable name="DL_HEADER_TABLE_WIDOWS"				as="xs:integer">2</xsl:variable>
	<xsl:variable name="DL_HEADER_TABLE_ORPHANS"			as="xs:integer">2</xsl:variable>-->
	
	<xsl:attribute-set name="ds:dl-header-table">
		<xsl:attribute name="width"					>100%</xsl:attribute>
		<xsl:attribute name="font-size"				>11pt</xsl:attribute>
		<xsl:attribute name="space-before"			>2.5mm</xsl:attribute>
		<xsl:attribute name="space-after"			>2.5mm</xsl:attribute>
		<xsl:attribute name="background-color"		>transparent</xsl:attribute>
		<xsl:attribute name="border-top-style"		>solid</xsl:attribute>
		<xsl:attribute name="border-bottom-style"	>solid</xsl:attribute>
		<xsl:attribute name="border-top-width"		><xsl:value-of select="$DL_HEADER_TABLE_OUTER_BORDER_WIDTH"/></xsl:attribute>
		<xsl:attribute name="border-bottom-width"	><xsl:value-of select="$DL_HEADER_TABLE_OUTER_BORDER_WIDTH"/></xsl:attribute>
		<xsl:attribute name="padding-top"			>0.5mm</xsl:attribute>	<!-- space between two upper borders --> 
		<xsl:attribute name="padding-bottom"		>0.5mm</xsl:attribute>	<!-- space between two lower borders --> 
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dlentry-header-table">
		<xsl:attribute name="background-color"		>transparent</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dt-header-table">
		<xsl:attribute name="margin-left"					>1.5mm</xsl:attribute>	<!-- padding-left doesn't work!? -->
		<xsl:attribute name="padding-right"					>1.5mm</xsl:attribute>
		<xsl:attribute name="font-weight"					>bold</xsl:attribute>
		<xsl:attribute name="space-before"					>1.0mm</xsl:attribute>
		<xsl:attribute name="space-before.conditionality"	>discard</xsl:attribute>
		<xsl:attribute name="space-after"					>0.5mm</xsl:attribute>
		<xsl:attribute name="space-after.conditionality"	>discard</xsl:attribute>
		<xsl:attribute name="start-indent"					>1.5mm</xsl:attribute>
		<xsl:attribute name="end-indent"					>1.5mm</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-header-table">
		<xsl:attribute name="margin-left"					>1.5mm</xsl:attribute>	<!-- padding-left doesn't work!? -->
		<xsl:attribute name="padding-right"					>1.5mm</xsl:attribute>
		<xsl:attribute name="space-before"					>1.0mm</xsl:attribute>
		<xsl:attribute name="space-before.conditionality"	>discard</xsl:attribute>
		<xsl:attribute name="space-after"					>0.5mm</xsl:attribute>
		<xsl:attribute name="space-after.conditionality"	>discard</xsl:attribute>
		<xsl:attribute name="start-indent"					>1.5mm</xsl:attribute>
		<xsl:attribute name="end-indent"					>1.5mm</xsl:attribute>
	</xsl:attribute-set>
	
	
	<!-- dl with outputclass "parameter-table" -->
	
	<xsl:attribute-set name="ds:dl-parameter-table" use-attribute-sets="ds:dl-header-table">
		<xsl:attribute name="margin-left"			>2mm</xsl:attribute>
		<xsl:attribute name="border-left-style"		>solid</xsl:attribute>
		<xsl:attribute name="border-right-style"	>solid</xsl:attribute>
		<xsl:attribute name="padding-top"			>0</xsl:attribute>
		<xsl:attribute name="padding-bottom"		>0</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dlentry-parameter-table" use-attribute-sets="ds:dlentry-header-table">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dt-parameter-table" use-attribute-sets="ds:dt-header-table">
		<xsl:attribute name="font-weight"			>normal</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-parameter-table" use-attribute-sets="ds:dd-header-table">
	</xsl:attribute-set>
	
	
	<!-- dl with outputclass "table" -->
	
	<xsl:attribute-set name="ds:dl-table" use-attribute-sets="simpletable">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dlhead-table" use-attribute-sets="strow">
		<xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dthd-table-cell" use-attribute-sets="ds:dt-table-cell">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:ddhd-table-cell" use-attribute-sets="ds:dd-table-cell">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dthd-table" use-attribute-sets="ds:dt-table">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:ddhd-table" use-attribute-sets="ds:dd-table">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dlentry-table" use-attribute-sets="strow">
		<xsl:attribute name="keep-together.within-page"	>always</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dt-table-cell" use-attribute-sets="ds:dl-common-table-cell">
		<xsl:attribute name="border-style"		>solid</xsl:attribute>
		<xsl:attribute name="border-width"		>1pt</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-table-cell" use-attribute-sets="ds:dl-common-table-cell">
		<xsl:attribute name="border-style"		>solid</xsl:attribute>
		<xsl:attribute name="border-width"		>1pt</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dt-table" use-attribute-sets="strow.stentry__content">
		<xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="space-after.conditionality">discard</xsl:attribute>
		<xsl:attribute name="space-before.conditionality">discard</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-table" use-attribute-sets="strow.stentry__content">
		<xsl:attribute name="space-after.conditionality">discard</xsl:attribute>
		<xsl:attribute name="space-before.conditionality">discard</xsl:attribute>
	</xsl:attribute-set>
	
	
	<!-- dl with outputclass "bullet-list-titles" -->
	
	
	<xsl:attribute-set name="ds:dlentry-bullet-list-titles" use-attribute-sets="ul.li">
		<xsl:attribute name="space-before"					>1.5mm</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dt-bullet-list-titles">
		<xsl:attribute name="font-weight"					>bold</xsl:attribute>
		<xsl:attribute name="space-after"					>1.0mm</xsl:attribute>
		<xsl:attribute name="keep-with-next.within-column"	>always</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-bullet-list-titles">
	</xsl:attribute-set>	
	
	
	<!-- dl with outputclass "bullet-list-dashes" -->
	
	<xsl:attribute-set name="ds:dt-bullet-list-dashes">
		<xsl:attribute name="font-weight"	>bold</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-bullet-list-dashes">
	</xsl:attribute-set>	
	
	
	<!-- dl with outputclass "numbered-list-titles" -->
	
	<xsl:attribute-set name="ds:dl-numbered-list-titles" use-attribute-sets="ol">
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dlentry-numbered-list-titles" use-attribute-sets="ol.li">
		<xsl:attribute name="space-before"					>1.5mm</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="ds:dlentry-numbered-list-titles-label-content" use-attribute-sets="ol.li__label__content">
		<xsl:attribute name="font-weight"	>bold</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dt-numbered-list-titles">
		<xsl:attribute name="font-weight"					>bold</xsl:attribute>
		<xsl:attribute name="space-after"					>1.0mm</xsl:attribute>
		<xsl:attribute name="keep-with-next.within-column"	>always</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="ds:dd-numbered-list-titles">
	</xsl:attribute-set>	
	
</xsl:stylesheet>
