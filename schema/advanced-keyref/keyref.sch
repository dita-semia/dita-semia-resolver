<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
	xmlns:sqf="http://www.schematron-quickfix.com/validator/process"
	xmlns:ikd="http://www.dita-semia.org/implicit-keydef"
	xmlns:akr="http://www.dita-semia.org/advanced-keyref">
	
	<sch:ns uri="java:org.DitaSemia.Oxygen.AdvancedKeyRef.AdvancedKeyRefSchematronUtil"  	prefix="jakr"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.SchematronUtil"							   		prefix="jsu"/>
	<sch:ns uri="http://www.dita-semia.org/advanced-keyref" 							 	prefix="akr"/>
	
	<sch:pattern>
		<sch:rule context="*[@akr:ref != '']"> 

			<sch:let name="refAttr"			value="@akr:ref"/>
			
			<sch:let name="keyRef"			value="jakr:createKeyRef(.)"/>
			<sch:let name="keyDef" 			value="jakr:getMatchingKeyDef($refAttr, base-uri())"/>
			
			<sch:let name="typeAttr" 		value="@akr:type"/>
			<sch:let name="namespaceAttr" 	value="@akr:namespace"/>
			<sch:let name="pathLenAttr" 	value="@akr:path-len"/>
			<sch:let name="outputclassAttr" value="@outputclass"/>
			<sch:let name="keyName" 		value="jakr:getKeyName($keyDef)"/>
			
			<sch:let name="refType"			value="jakr:getType($keyRef)"/>
			<sch:let name="refNamespace" 	value="jakr:getNamespace($keyRef)"/>
			
			<sch:let name="namespaceError" 	value="jakr:getXPathListErrorMessage(., $namespaceAttr)"/>
			
			<sch:assert test="jakr:matchesPathLen(./text(), $pathLenAttr)"> 
				 The text content does not match the given path length ('<sch:value-of select="$pathLenAttr"/>').
			</sch:assert>
			<sch:report test="exists($namespaceError)"> 
				Invalid value for @akr:namespace. It must be empty or a valid XPath: <sch:value-of select="$namespaceError"/>
			</sch:report>
			<sch:assert test="jakr:matchesRefText($keyRef)"> 
				Text content does not match @akr:ref ('<sch:value-of select="$refAttr"/>').
			</sch:assert> 
			<sch:assert test="jakr:matchesNamespaceFilter($keyRef, $keyDef)">
				The referenced namespace ('<sch:value-of select="$refNamespace"/>') is not allowed in this context ('<sch:value-of select="$namespaceAttr"/>').
			</sch:assert> 
			<sch:report test="$outputclassAttr = 'name' and empty($keyName)">
				The referenced Key has no name to be displayed with this outputclass ('<sch:value-of select="$outputclassAttr"/>').
			</sch:report>
			<sch:assert test="exists($keyDef)"> 
				 Invalid value for @akr:ref. It must match a KeyDef. ('<sch:value-of select="$refAttr"/>')
			</sch:assert> 
			<sch:report test="$typeAttr != $refType">
				Referenced type ('<sch:value-of select="$typeAttr"/>') is not allowed in this context ('<sch:value-of select="$refAttr"/>')!
			</sch:report>
		</sch:rule>
	</sch:pattern>
	
</sch:schema>