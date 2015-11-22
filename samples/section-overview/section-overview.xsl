<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl	= "http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
    	<sl class="+ topic/sl ">
        	<xsl:for-each select="*/*/section[@id][title]">
                <sli class="- topic/sli ">
                	<xref href="#{/*/@id}/{@id}" format="dita" class="- topic/xref "/>
                </sli>
            </xsl:for-each>
        </sl>
    </xsl:template>

</xsl:stylesheet>
