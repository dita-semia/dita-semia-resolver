# XsltConref
Support of the XSLT-Conref concept (add an XSL transformation to the standard DITA conref)


The basic idea of XSLT-Conref is to combine the conref concept of DITA with an XSL transformation. This way you can not only reuse DITA content but any data that is sufficiently structured to evaluate it with xslt. Ths result needs to be DITA content to be embedded into your DITA document. This works for graphics as well since oXygen and XSL-FO both support SVG pretty well.


## Syntax (short version)
Just add an xcr:xsl attribute (namespace http://www.dita-semia.org/xslt-conref) to your element and put an URL to the xslt script in it. The root element of the result needs to have the same or a base class class  as the element containing the xcr:xsl attribute. You can use absolute and reletive URLs as well as URNs previously registered with catalogs.

## Installation in oXygen
To make oXygen resolve your xslt-conrefs you have to configure a reference resolver. The easiest way is to use the frameworks that comes with this plugin: dita-semia and dita-semia-map. They are located in the folder org.dita-semia.resolver\oxygen-framework. To let oxygen find these you have to add the folder org.dita-semia.resolver to the custom locations.

More information can be found in the (preliminary) documentation dita-semia.pdf of the [lastes release](https://github.com/dita-semia/XsltConref/releases/latest).

## Compiling
To compile it with eclipse you have to set the variables OXYGENXML(locating the oXyen installation folder, e.g. "C:/Program Files/Oxygen XML Editor") and DITAOT (locating your dita-of folder, e.g. "C:/Development/dita-ot"). This needs to be done twice:
- Ecplipse Path variable: Window -> Preferences -> Java -> Build Path -> Classpath Variables
- For the Ant Builder: Window -> Preferences -> Ant -> Runtime -> Properties
