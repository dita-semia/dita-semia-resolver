<?xml version="1.0" encoding="UTF-8"?>
<sch:schema 
	xmlns:sch	= "http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
	xmlns:sqf	= "http://www.schematron-quickfix.com/validator/process">
	
	<sch:ns uri="http://www.dita-semia.org/implicit-keydef" 							prefix="ikd"/>
	<sch:ns uri="java:org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface"				prefix="jkd"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.SchematronUtil" 								prefix="jsu"/>
	<sch:ns uri="java:org.DitaSemia.Oxygen.AdvancedKeyRef.ImplicitKeyDefSchematronUtil" prefix="jikd"/>

	<sch:pattern>
		<sch:rule context="*[@ikd:key-type]">

			<sch:let name="keyAttr"			value="@ikd:key"/>
			<sch:let name="namespaceAttr"	value="@ikd:namespace"/>
			<sch:let name="rootAttr"		value="@ikd:root"/>
			<sch:let name="refNodeAttr"		value="@ikd:ref-node"/>
			<sch:let name="nameAttr"		value="@ikd:name"/>
			<sch:let name="descAttr"		value="@ikd:desc"/>

			<sch:let name="jKeyDef"			value="jikd:createKeyDef(.)"/>

			<sch:let name="keyValue"		value="if (exists($jKeyDef)) 		then jkd:getKey($jKeyDef) else ''"/>
			<sch:let name="namespaceValue"	value="if (exists($jKeyDef)) 		then jkd:getNamespaceList($jKeyDef) else ()"/>
			<sch:let name="rootNode"		value="if (exists($rootAttr)) 		then jsu:evaluateXPathToNode(., $rootAttr) else ."/>
			<sch:let name="refNode"			value="if (exists($refNodeAttr)) 	then jsu:evaluateXPathToNode($rootNode, $refNodeAttr) else $rootNode"/>

			<sch:let name="namespaceError"	value="if (empty($jKeyDef)) then jikd:getXPathListErrorMessage(., $namespaceAttr) else ()"/>
			<sch:let name="rootError"		value="if (empty($jKeyDef)) then jikd:getXPathErrorMessage(., $rootAttr) else ()"/>
			<sch:let name="nameError"		value="if (empty($jKeyDef)) then jikd:getXPathErrorMessage(., $nameAttr) else ()"/>
			<sch:let name="descError"		value="if (empty($jKeyDef)) then jikd:getXPathErrorMessage(., $descAttr) else ()"/>
			<sch:let name="keyError"		value="if (empty($jKeyDef)) then jikd:getXPathErrorMessage(., $keyAttr) else ()"/>

			<sch:report test="exists($rootError)">
				Invalid value for @ikd:root. It must be empty or a valid XPath: <sch:value-of select="$rootError"/> 
			</sch:report>
			<sch:report test="exists($rootAttr) and empty($rootNode intersect ancestor-or-self::*)">
				Invalid value for @ikd:root. It must identify an ancestor or the element itself. 
			</sch:report>
			<sch:report test="exists($jKeyDef) and exists($refNode) and empty($refNode/@id) and empty(processing-instruction('SuppressWarnings')[tokenize(., '\s+') = 'ikd:missingId'])" role="warn">
				The referenced element ('<sch:value-of select="name($refNode)"/>') has no id attribute. Referencing elements won't create a link.
			</sch:report>
			<sch:report test="exists($keyError)">
				Invalid value for @ikd:key. It must be empty or a valid XPath: <sch:value-of select="$keyError"/> 
			</sch:report>
			<sch:report test="exists($namespaceError)"> 
				Invalid value for @ikd:namespace. It must be empty or a valid XPath: <sch:value-of select="$namespaceError"/>
			</sch:report>
			<sch:report test="exists($nameError)">
				Invalid value for @ikd:name. It must be empty or a valid XPath: <sch:value-of select="$nameError"/> 
			</sch:report>
			<sch:report test="exists($descError)">
				Invalid value for @ikd:desc. It must be empty or a valid XPath: <sch:value-of select="$descError"/> 
			</sch:report>
			<sch:report test="matches($keyValue, '[:/]')">
				Invalid value for the key ('<sch:value-of select="$keyValue"/>').
				It must not contain ":" or "/".
			</sch:report>
			<sch:report test="exists($namespaceValue[matches(., '[.:/]')])">
				Invalid value for the namespace ('<sch:value-of select="string-join($namespaceValue, ', ')"/>').
				It must not contain elements with ".", ":" or "/".
			</sch:report>
			<!-- TODO: dublicated keyDef -->
		</sch:rule>
	</sch:pattern>

</sch:schema>
