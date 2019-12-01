# About the project

http://maps-agenda.ch/

Der Veranstaltungskalender MAPS Züri Agenda informiert in 16 Sprachen über günstige Angebote im Zürcher Kultur- 
und Freizeitbereich. Dieses Angebot richtet sich vor allem an Migrant/innen, deren Deutschkenntnisse nicht für die Lektüre
des "Züritipp" ausreichen und die über wenige finanzielle Mittel verfügen.


Aktuelle Veranstaltungshinweise entnehmen Sie der Online MAPS Agenda oder dem monatlich erscheinenden Newsletter
(Anmeldung via Homepage). Die gedruckte Version der MAPS Züri Agenda können Sie gratis bestellen.


Die Sprachen der MAPS Züri Agenda sind: Albanisch, Arabisch, Bosnisch/Serbisch/Kroatisch, Deutsch, Englisch, Französisch, 
Italienisch, Mandarin, Persisch, Portugiesisch, Russisch, Spanisch, Somali, Tamilisch, Türkisch und Tigrinya.


# Installation

1. Install the google Cloud SDK. This is documented well on the Cloud SDK website. For ubuntu the procedure is https://cloud.google.com/sdk/docs/quickstart-debian-ubuntu
1. Make sure to go through all installation steps, including ```gcloud init```
1. Install jdk8 and maven. For Ubuntu this is done by running ```sudo apt-get install openjdk-8-jdk maven```
1. Checkout project: ```git clone git@github.com:panterch/maps-agenda.git```
1. Switch to the code directory: ```cd Maps Agenda```
1. Install dependencies: ```mvn install```
1. Run locally: ```mvn appengine:run``` 
1. navigate to http://localhost:8080/admin2/#/languages - add a first language
1. Make sure to push the "Save"-Button
1. navigate to http://localhost:8080/admin - add the first event 
1. application under ready at http://localhost:8080
