#DITA-SEMIA-Oxygen

Component required for oXygen on lower level (e.g. from Schematron).
Needs to be copied to the oXygen's lib folder as well.


*WARNING*

Do *NOT* add it to oXygen's class path for a document type. This would result it multiple instanxes of singletons since oXygen uses a different clas loader for these libraries.