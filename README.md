# Installation

Follow the instructions in INSTALL.txt to install the WSDL To Bonita Connector Plugin.

# Usage

1. Run wsdl2bonitaconnector as follows: "./wsdl2bonitaconnector [wsdl]".
   E.g., "./wsdl2bonitaconnector http://www.html2xml.nl/Services/Calculator/Version1/Calculator.asmx?wsdl". The command
   will produce an output folder in the current directory. The folder will contain the Web Service connector packaged as
   a JAR plus the connector source code.

2. From Bonita Studio, import the generated JAR as a connector. Note that the JAR may contain multiple connectors, a
   connector for each Web Service operation.