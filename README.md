#DITA-SEMIA
This is the core reposiory of the DITA-SEMIA project supporting different features that are too closely related to split them into different repositories:

- *XsltConref* (XSLT Content Reference)
	The basic idea of XSLT-Conref is to combine the conref concept of DITA with an XSL transformation. This way you can not only reuse DITA content but any data that is sufficiently structured to evaluate it with xslt. Ths result needs to be DITA content to be embedded into your DITA document. This works for graphics as well since oXygen and XSL-FO both support SVG pretty well.
- *Conbat*  (Content by Attributes)
	The idea is to generate content defined in the schema by setting some specific attribute default values. this way you don't have to add specific CSS and XSLT code to properly display specialized elements.

## Installation in oXygen
To make oXygen resolve your xslt-conrefs you have to configure a reference resolver. The easiest way is to use the frameworks that comes with this plugin: dita-semia and dita-semia-map. They are located in the folder org.dita-semia.resolver\oxygen-framework. To let oxygen find these you have to add the folder org.dita-semia.resolver to the custom locations.

More information can be found in the (preliminary) documentation dita-semia.pdf of the [lastes release](https://github.com/dita-semia/XsltConref/releases/latest).

### Compiling
To compile it with eclipse you have to set the variables OXYGENXML(locating the oXyen installation folder, e.g. "C:/Program Files/Oxygen XML Editor") and DITAOT (locating your dita-of folder, e.g. "C:/Development/dita-ot"). This needs to be done twice:
- Ecplipse Path variable: Window -> Preferences -> Java -> Build Path -> Classpath Variables
- For the Ant Builder: Window -> Preferences -> Ant -> Runtime -> Properties
