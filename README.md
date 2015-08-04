# About the project

http://maps-agenda.ch/

Der Veranstaltungskalender MAPS Züri Agenda informiert in 16 Sprachen über günstige Angebote im Zürcher Kultur- 
und Freizeitbereich. Dieses Angebot richtet sich vor allem an Migrant/innen, deren Deutschkenntnisse nicht für die Lektüre
des "Züritipp" ausreichen und die über wenige finanzielle Mittel verfügen.


Aktuelle Veranstaltungshinweise entnehmen Sie der Online MAPS Agenda oder dem monatlich erscheinenden Newsletter
(Anmeldung via Homepage). Die gedruckte Version der MAPS Züri Agenda können Sie gratis bestellen.


Die Sprachen der MAPS Züri Agenda sind: Albanisch, Arabisch, Bosnisch/Serbisch/Kroatisch, Deutsch, Englisch, Französisch, 
Italienisch, Mandarin, Persisch, Portugiesisch, Russisch, Spanisch, Somali, Tamilisch, Türkisch und Tigrinya.

# Developer info

You will need eclipse and the google appengine plugin: https://developers.google.com/eclipse/docs/using_sdks

Note that you'll also have to do this because AppEngine does not yet work with Java 8:
http://java.wildstartech.com/Java-Platform-Standard-Edition/mac-os-x-java-development/how-to-configure-eclipse-to-run-with-java-7-when-java-8-is-installed
Make sure that "Properties -> Java Compiler" is set to 1.7 and that "Properties -> Project Facets" is also set to 1.7.

Go to: Properties -> Java Build Path -> Libraries tab -> Add external JAR and add the following JAR:
https://github.com/panterch/maps-agenda/blob/master/Maps%20Agenda/war/WEB-INF/lib/json-20140107.jar



