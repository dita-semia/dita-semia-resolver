<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2" xmlns:sqf="http://www.schematron-quickfix.com/validator/process">
	
	<sch:pattern>
		<sch:rule context="Person">
			<sch:assert id="Person.001" test="TypeOfPerson = ('1', '2')" subject="TypeOfPerson">
				The field TypeOfPerson must contain a value according to its definition.
			</sch:assert>
			<sch:report id="Person.002" test="(TypeOfPerson = '1') and empty(DateOfBirth/text())">
				If the field TypeOfPerson contains the value 1, then the element DateOfBirth must not be empty.
			</sch:report>
		</sch:rule>
	</sch:pattern>
	
</sch:schema>