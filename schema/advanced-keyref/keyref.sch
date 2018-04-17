<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
	xmlns:sqf="http://www.schematron-quickfix.com/validator/process"
	xmlns:ikd="http://www.dita-semia.org/implicit-keydef"
	xmlns:akr="http://www.dita-semia.org/advanced-keyref"
	xmlns:cba="http://www.dita-semia.org/conbat">
	
	<sch:ns uri="java:org.DitaSemia.Oxygen.AdvancedKeyRef.AdvancedKeyRefSchematronUtil"  	prefix="jakr"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.SchematronUtil"							   		prefix="jsu"/>
	<sch:ns uri="java:org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface"					prefix="jkr"/>
	<sch:ns uri="java:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface"					prefix="jkd"/>
	<sch:ns uri="java:org.DitaSemia.Base.SaxonNodeWrapper"									prefix="jsnw"/>
	<sch:ns uri="java:org.DitaSemia.Base.FilterProperties"									prefix="jfp"/>
	<sch:ns uri="http://www.dita-semia.org/advanced-keyref" 							 	prefix="akr"/>
	<sch:ns uri="http://www.dita-semia.org/conbat" 							 				prefix="cba"/>
	
	<sch:pattern id="advanced-keyref">
		<sch:rule context="*[@akr:ref]"> 

			<sch:let name="refAttr"			value="@akr:ref"/>
			
			<sch:let name="keyRef"			value="jakr:createKeyRef(.)"/>
			<sch:let name="keyDef" 			value="jakr:getMatchingKeyDef($refAttr, base-uri())"/>
			
			<sch:let name="typeAttr" 		value="@akr:type"/>
			<sch:let name="typeList" 		value="tokenize($typeAttr, '\s+')"/>
			<sch:let name="namespaceAttr" 	value="@akr:namespace"/>
			<sch:let name="pathLenAttr" 	value="@akr:path-len"/>
			<sch:let name="outputclassAttr" value="@outputclass"/>
			<sch:let name="refFilter"		value="jfp:getFromNodeWithAncestors(jsnw:new(., ()))"/>
			<sch:let name="keyName" 		value="if (exists($keyDef)) then jkd:getName($keyDef) else ''"/>
			<sch:let name="keyPath" 		value="if (exists($keyDef)) then string-join((jkd:getNamespace($keyDef), jkd:getKey($keyDef)), '/') else if (contains($refAttr, '#')) then () else substring-after($refAttr, ':')"/>
			<sch:let name="destFilter"		value="if (exists($keyDef)) then jkd:getFilterProperties($keyDef) else ()"/>

			<sch:let name="refType"			value="jkr:getType($keyRef)"/>
			<sch:let name="refNamespace" 	value="jkr:getNamespace($keyRef)"/>

			<sch:let name="namespaceError" 	value="jakr:getXPathListErrorMessage(., $namespaceAttr)"/>
			
			<sch:assert test="jakr:matchesPathLen(./text(), $pathLenAttr, $keyRef)"> 
				Invalid keyref '<sch:value-of select="."/>': The text content does not match the given path length ('<sch:value-of select="$pathLenAttr"/>').
			</sch:assert>
			<sch:report test="exists($namespaceError)"> 
				Invalid value for @akr:namespace. It must be empty or a valid XPath: <sch:value-of select="$namespaceError"/>
			</sch:report>
			<sch:assert test="empty($keyDef) or exists(@cba:content) or empty($keyPath) or (jakr:textMatchesPath(text(), $keyPath))">
				<!-- 
					don't validate the text content when:
						- there is no keydef linked
						- it's set by conbat
						- the key-path is not known (ref-by-id and no valid keydef)
				-->
				Invalid keyref '<sch:value-of select="."/>': The text content does not match the reference ('<sch:value-of select="jkd:getRefString($keyDef)"/>').
			</sch:assert>
			<sch:assert test="empty($keyDef) or (jakr:matchesNamespaceFilter($keyRef, $keyDef))">
				Invalid keyref '<sch:value-of select="."/>': The referenced namespace ('<sch:value-of select="$refNamespace"/>') is not allowed in this context ('<sch:value-of select="$namespaceAttr"/>').
			</sch:assert>
			<sch:report test="($outputclassAttr = 'name') and (not(exists($keyName)) or empty($keyName))">
				Invalid keyref '<sch:value-of select="."/>': The referenced key has no name to be displayed with this outputclass ('<sch:value-of select="$outputclassAttr"/>').
			</sch:report>
			<sch:assert test="exists($keyDef)"> 
				Invalid keyref '<sch:value-of select="."/>': No matching key defined ('<sch:value-of select="$refAttr"/>')
			</sch:assert> 
			<sch:report test="exists($typeList) and not($refType = $typeList)">
				Invalid keyref '<sch:value-of select="."/>': The referenced type ('<sch:value-of select="$refType"/>') is none of the allowed types: <sch:value-of select="string-join($typeList, ', ')"/>!
			</sch:report> 
			<sch:report test="not(ancestor-or-self::*/@props = 'no-link-filter-check') and not(jfp:contains($refFilter, 'audience', 'GDV_DL')) and exists($keyDef) and not(jfp:isValidReference($refFilter, $destFilter))">
				Invalid keyref '<sch:value-of select="."/>': The filter properties of the reference (<sch:value-of select="$refFilter"/>) are less restrictive than those of the referenced key (<sch:value-of select="$destFilter"/>)!
			</sch:report>
		</sch:rule>
	</sch:pattern>
	
</sch:schema>