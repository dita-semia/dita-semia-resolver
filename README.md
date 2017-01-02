#DITA-SEMIA
This is the core repository of the DITA-SEMIA project supporting different features that are too closely related to split them into different repositories:

- **XSLT-Conref** (XSLT Content Reference)

	The basic idea of XSLT-Conref is to combine the conref concept of DITA with an XSL transformation. This way you can not only reuse DITA content but any data that is sufficiently structured to evaluate it with xslt. Ths result needs to be DITA content to be embedded into your DITA document. This works for graphics as well since oXygen and XSL-FO both support SVG pretty well.

- **Conbat**  (Content by Attributes)

	The idea is to generate content defined in the schema by setting some specific attribute default values. this way you don't have to add specific CSS and XSLT code to properly display specialized elements.

- **Advanced-Keyref**

	One part of this feature is to implicitly define keys within your topic content. It is defined by a type and a sequence of identifiers while the last one is the key itself and the preceding ones form the optional namespace. Additionally a name and description can be defined. These properties can be set by dedicated attributes using embedded XPath expressions (esiecially useful when set as defaults by the schema).
	
	The second part is a new mechanism to reference these keys. Dedicated atributes allow to define a filter for which kind of keys (by namespace or type) can be referenced by a specific element. Since ths concept is designed to handle a large amount of keys (10.000+) there is also a dialog availiable to comfortably search for the correct key.
	
- **Topic Numbers**

	Display topic numbers in oXygen Author Mode in front of topic titles and (id-based) cross references depending on the current DITA Map selected in the Maps Manager.

## Installation in oXygen
To make oXygen resolve your xslt-conrefs you have to configure a reference resolver. The easiest way is to use the frameworks that comes with this plugin: dita-semia and dita-semia-map. They are located in the folder org.dita-semia.resolver\oxygen-framework. To let oxygen find these you have to add the folder org.dita-semia.resolver to the custom locations.

More information can be found in the (preliminary) documentation dita-semia.pdf of the [lastes release](https://github.com/dita-semia/XsltConref/releases/latest).

### Compiling
To compile it with eclipse you have to set the variables OXYGENXML(locating the oXyen installation folder, e.g. "C:/Program Files/Oxygen XML Editor") and DITAOT (locating your dita-of folder, e.g. "C:/Development/dita-ot"). This needs to be done twice:
- Ecplipse Path variable: Window -> Preferences -> Java -> Build Path -> Classpath Variables
- For the Ant Builder: Window -> Preferences -> Ant -> Runtime -> Properties
