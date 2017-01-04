<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
	xmlns:sqf="http://www.schematron-quickfix.com/validator/process"
	xmlns:ikd="http://www.dita-semia.org/implicit-keydef"
	xmlns:akr="http://www.dita-semia.org/advanced-keyref"
	xmlns:cba="http://www.dita-semia.org/conbat">
	
	<sch:ns uri="java:org.DitaSemia.Oxygen.AdvancedKeyRef.AdvancedKeyRefSchematronUtil"  	prefix="jakr"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.SchematronUtil"							   		prefix="jsu"/>
	<sch:ns uri="java:org.DitaSemia.Base.AdvancedKeyref.KeyRef"  							prefix="jkr"/>
	<sch:ns uri="java:org.DitaSemia.Base.AdvancedKeyref.KeyDef"  							prefix="jkd"/>
	<sch:ns uri="http://www.dita-semia.org/advanced-keyref" 							 	prefix="akr"/>
	<sch:ns uri="http://www.dita-semia.org/conbat" 							 				prefix="cba"/>
	
	<sch:pattern>
		<sch:rule context="*[@akr:ref != '']"> 

			<sch:let name="refAttr"			value="@akr:ref"/>
			
			<sch:let name="keyRef"			value="jakr:createKeyRef(.)"/>
			<sch:let name="keyDef" 			value="jakr:getMatchingKeyDef($refAttr, base-uri())"/>
			
			<sch:let name="typeAttr" 		value="@akr:type"/>
			<sch:let name="namespaceAttr" 	value="@akr:namespace"/>
			<sch:let name="pathLenAttr" 	value="@akr:path-len"/>
			<sch:let name="outputclassAttr" value="@outputclass"/>
			<sch:let name="keyName" 		value="if (exists($keyDef)) then jkd:getName($keyDef) else ''"/>
			
			<sch:let name="refType"			value="jkr:getType($keyRef)"/>
			<sch:let name="refNamespace" 	value="jkr:getNamespace($keyRef)"/>
			
			<sch:let name="namespaceError" 	value="jakr:getXPathListErrorMessage(., $namespaceAttr)"/>
			
			<sch:assert test="jakr:matchesPathLen(./text(), $pathLenAttr)"> 
				Invalid keyref: The text content does not match the given path length ('<sch:value-of select="$pathLenAttr"/>').
			</sch:assert>
			<sch:report test="exists($namespaceError)"> 
				Invalid value for @akr:namespace. It must be empty or a valid XPath: <sch:value-of select="$namespaceError"/>
			</sch:report>
			<sch:assert test="exists(@cba:content) or (jakr:matchesRefText($keyRef))">
				<!-- don't validate the text content when it's set by conbat -->
				Invalid keyref: Text text content does not match the reference ('<sch:value-of select="$refAttr"/>').
			</sch:assert> 
			<sch:assert test="jakr:matchesNamespaceFilter($keyRef, $keyDef)">
				Invalid keyref: The referenced namespace ('<sch:value-of select="$refNamespace"/>') is not allowed in this context ('<sch:value-of select="$namespaceAttr"/>').
			</sch:assert> 
			<sch:report test="$outputclassAttr = 'name' and (not(exists($keyName)) or empty($keyName))">
				Invalid keyref: The referenced key has no name to be displayed with this outputclass ('<sch:value-of select="$outputclassAttr"/>').
			</sch:report>
			<sch:assert test="exists($keyDef)"> 
				 Invalid keyref: no matching key defined ('<sch:value-of select="$refAttr"/>')
			</sch:assert> 
			<sch:report test="$typeAttr != $refType">
				Invalid keyref: referenced type ('<sch:value-of select="$typeAttr"/>') is not allowed in this context ('<sch:value-of select="$refAttr"/>')!
			</sch:report>
		</sch:rule>
	</sch:pattern>
	
</sch:schema>