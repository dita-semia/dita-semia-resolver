# XsltConref
Support of the XSLT-Conref concept (add an XSL transformation to the standard DITA conref)


The basic idea of XSLT-Conref is to combine the conref concept of DITA with an XSL transformation. This way you can not only reuse DITA content but any data that is sufficiently structured to evaluate it with xslt. Ths result needs to be DITA content to be embedded into your DITA document. This works for graphics as well since oXygen and XSL-FO both support SVG pretty well.


## Syntax (short version)
Just add an xslt-conref attribute to your element and put an URL to the xslt script in it. The rott element of the result needs to have the same name as the element containing the xslt-conref attribute. You can use absolute and reletive URLs as well as URNs previously registered with catalogs.


## Installation in oXygen
To make oXygen resolve your xslt-conrefs you have to configure a reference resolver. This requires you to download the repository dita-semia/JavaBase as well.

### Using the provided custom framework
The repository includes a framework with hhis reference resolver already configured in the framework folder. To make oXygen use this you have to add the XsltConref folder to the "Locations Preferences" and restart oXygen afterwards.
Now you should fine the new document type "DITA with XSLT-Conref support" and be able to run the samples.

### Adapting your own custom framework
First you have to add the files DitaSemiaJavaBase.jar and DitaSemiaXsltConref.jar to the class path of your extension. Then you can explicitly set the class OxygenXsltConrefResolver as reference resolver.
If you have already a custom reference resolver configured you will have to merge the sources...


## Installation in DITA-OT
Not done yet.


## Samples
The samples are located in a dedicated samples folder. There is an oXygen project as well hich additionally provides a transformation scenario that resolves the xslt-conrefs on xml level and returns the result.

### local-toc
This sample consists of a reference topic containing child reference topics. The xslt-conref generates an overview of the child topics as unordered list with their titles, their short descriptions and a cross reference.
