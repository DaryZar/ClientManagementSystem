import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class XmlAdvancedHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String XSD_SCHEMA = "schema.xsd";

    public static void saveWithDom(String filename, ClientCollectionManager manager) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("clients");
        doc.appendChild(root);

        for (Client c : manager.getClientList()) {
            Element clientNode = doc.createElement("client");
            root.appendChild(clientNode);
            clientNode.setAttribute("id", String.valueOf(c.getId()));

            Element nameNode = doc.createElement("name");
            nameNode.appendChild(doc.createTextNode(c.getName() != null ? c.getName() : ""));
            clientNode.appendChild(nameNode);

            Element dateNode = doc.createElement("contractDate");
            dateNode.appendChild(doc.createTextNode(c.getContractDate() != null ? c.getContractDate().format(FORMATTER) : ""));
            clientNode.appendChild(dateNode);

            Element budgetNode = doc.createElement("searchBudget");
            budgetNode.appendChild(doc.createTextNode(String.valueOf(c.getSearchBudget())));
            clientNode.appendChild(budgetNode);

            Element filesNode = doc.createElement("files");
            clientNode.appendChild(filesNode);
            if (c.getFiles() != null) {
                for (ClientFile f : c.getFiles()) {
                    Element fileNode = doc.createElement("file");
                    fileNode.setAttribute("type", f.getFileType() != null ? f.getFileType() : "");
                    fileNode.appendChild(doc.createTextNode(f.getFileName() != null ? f.getFileName() : ""));
                    filesNode.appendChild(fileNode);
                }
            }
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://apache.org}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filename));
        transformer.transform(source, result);
    }

    public static void loadWithDom(String filename, ClientCollectionManager manager) throws Exception {
        manager.clearAll();
        File file = new File(filename);
        if (!file.exists()) return;

        if (new File(XSD_SCHEMA).exists() && !Validator.validateXml(filename, XSD_SCHEMA)) {
            throw new RuntimeException("XML structure is invalid against XSD schema.");
        }

        parseDomDocument(file, manager);
    }

    public static void loadWithDomDtd(String filename, ClientCollectionManager manager) throws Exception {
        manager.clearAll();
        File file = new File(filename);
        if (!file.exists()) return;

        if (!Validator.validateXmlWithDtd(filename)) {
            throw new RuntimeException("XML structure is invalid against DTD schema.");
        }

        parseDomDocument(file, manager);
    }

    private static void parseDomDocument(File file, ClientCollectionManager manager) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList clientNodes = doc.getElementsByTagName("client");
        for (int i = 0; i < clientNodes.getLength(); i++) {
            Node node = clientNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                int id = Integer.parseInt(element.getAttribute("id"));
                String name = element.getElementsByTagName("name").item(0).getTextContent();
                LocalDate date = LocalDate.parse(element.getElementsByTagName("contractDate").item(0).getTextContent(), FORMATTER);
                double budget = Double.parseDouble(element.getElementsByTagName("searchBudget").item(0).getTextContent());

                List<ClientFile> files = new ArrayList<>();
                NodeList filesContainerList = element.getElementsByTagName("files");
                if (filesContainerList.getLength() > 0) {
                    Element filesContainer = (Element) filesContainerList.item(0);
                    NodeList fileNodes = filesContainer.getElementsByTagName("file");
                    for (int j = 0; j < fileNodes.getLength(); j++) {
                        Element fileElem = (Element) fileNodes.item(j);
                        files.add(new ClientFile(fileElem.getTextContent(), fileElem.getAttribute("type")));
                    }
                }
                manager.addClient(new Client(id, name, files, date, budget));
            }
        }
    }

    public static void loadWithSax(String filename, ClientCollectionManager manager) throws Exception {
        manager.clearAll();
        File file = new File(filename);
        if (!file.exists()) return;

        if (new File(XSD_SCHEMA).exists() && !Validator.validateXml(filename, XSD_SCHEMA)) {
            throw new RuntimeException("XML structure is invalid against XSD schema.");
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            private Client currentClient;
            private List<ClientFile> currentFiles;
            private StringBuilder content = new StringBuilder();
            private String fileTypeAttr = "";

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                content.setLength(0);
                if (qName.equalsIgnoreCase("client")) {
                    currentClient = new Client();
                    currentClient.setId(Integer.parseInt(attributes.getValue("id")));
                    currentFiles = new ArrayList<>();
                } else if (qName.equalsIgnoreCase("file")) {
                    fileTypeAttr = attributes.getValue("type");
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                content.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if (currentClient == null) return;
                String value = content.toString().trim();
                
                switch (qName.toLowerCase()) {
                    case "name" -> currentClient.setName(value);
                    case "contractdate" -> currentClient.setContractDate(LocalDate.parse(value, FORMATTER));
                    case "searchbudget" -> currentClient.setSearchBudget(Double.parseDouble(value));
                    case "file" -> currentFiles.add(new ClientFile(value, fileTypeAttr));
                    case "client" -> {
                        currentClient.setFiles(currentFiles);
                        manager.addClient(currentClient);
                        currentClient = null;
                    }
                }
                content.setLength(0);
            }
        };
        saxParser.parse(file, handler);
    }

        public static void saveWithStax(String filename, ClientCollectionManager manager) throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try (FileWriter fileWriter = new FileWriter(filename)) {
            XMLStreamWriter writer = factory.createXMLStreamWriter(fileWriter);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeStartElement("clients");
            writer.writeCharacters("\n");

            writeClientsToStax(writer, manager);

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        }
    }

    public static void saveWithStaxDtd(String filename, ClientCollectionManager manager, String dtdFilename) throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try (FileWriter fileWriter = new FileWriter(filename)) {
            XMLStreamWriter writer = factory.createXMLStreamWriter(fileWriter);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeDTD("<!DOCTYPE clients SYSTEM \"" + dtdFilename + "\">");
            writer.writeCharacters("\n");
            writer.writeStartElement("clients");
            writer.writeCharacters("\n");

            writeClientsToStax(writer, manager);

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        }
    }


    private static void writeClientsToStax(XMLStreamWriter writer, ClientCollectionManager manager) throws Exception {
        for (Client c : manager.getClientList()) {
            writer.writeCharacters("    ");
            writer.writeStartElement("client");
            writer.writeAttribute("id", String.valueOf(c.getId()));
            writer.writeCharacters("\n");

            writer.writeCharacters("        ");
            writer.writeStartElement("name");
            writer.writeCharacters(c.getName() != null ? c.getName() : "");
            writer.writeEndElement();
            writer.writeCharacters("\n");

            writer.writeCharacters("        ");
                    writer.writeStartElement("contractDate");
        writer.writeCharacters(c.getContractDate() != null ? c.getContractDate().format(FORMATTER) : "");
        writer.writeEndElement();
        writer.writeCharacters("\n");

        writer.writeCharacters("        ");
        writer.writeStartElement("searchBudget");
        writer.writeCharacters(String.valueOf(c.getSearchBudget()));
        writer.writeEndElement();
        writer.writeCharacters("\n");

        writer.writeCharacters("        ");
        writer.writeStartElement("files");
        writer.writeCharacters("\n");
        if (c.getFiles() != null) {
            for (ClientFile f : c.getFiles()) {
                writer.writeCharacters("            ");
                writer.writeStartElement("file");
                writer.writeAttribute("type", f.getFileType() != null ? f.getFileType() : "");
                writer.writeCharacters(f.getFileName() != null ? f.getFileName() : "");
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
        }
        writer.writeCharacters("        ");
        writer.writeEndElement();
        writer.writeCharacters("\n");

        writer.writeCharacters("    ");
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }
}

    public static void loadWithStax(String filename, ClientCollectionManager manager) throws Exception {
        manager.clearAll();
        File file = new File(filename);
        if (!file.exists()) return;

        if (new File(XSD_SCHEMA).exists() && !Validator.validateXml(filename, XSD_SCHEMA)) {
            throw new RuntimeException("XML structure is invalid against XSD schema.");
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (FileInputStream fis = new FileInputStream(file)) {
            XMLStreamReader reader = factory.createXMLStreamReader(fis);

            Client currentClient = null;
            List<ClientFile> currentFiles = null;
            String currentElement = "";
            String fileTypeAttr = "";

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        currentElement = reader.getLocalName();
                        if (currentElement.equalsIgnoreCase("client")) {
                            currentClient = new Client();
                            currentClient.setId(Integer.parseInt(reader.getAttributeValue(null, "id")));
                            currentFiles = new ArrayList<>();
                        } else if (currentElement.equalsIgnoreCase("file")) {
                            fileTypeAttr = reader.getAttributeValue(null, "type");
                        }
                    }
                    case XMLStreamConstants.CHARACTERS -> {
                        String value = reader.getText().trim();
                        if (value.isEmpty() || currentClient == null || currentElement.isEmpty()) break;

                        switch (currentElement.toLowerCase()) {
                            case "name" -> currentClient.setName(value);
                            case "contractdate" -> currentClient.setContractDate(LocalDate.parse(value, FORMATTER));
                            case "searchbudget" -> currentClient.setSearchBudget(Double.parseDouble(value));
                            case "file" -> currentFiles.add(new ClientFile(value, fileTypeAttr));
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        currentElement = "";
                        if (reader.getLocalName().equalsIgnoreCase("client") && currentClient != null) {
                            currentClient.setFiles(currentFiles);
                            manager.addClient(currentClient);
                            currentClient = null;
                        }
                    }
                }
            }
            reader.close();
        }
    }

    
public static String evaluateXPathFindNameById(String filename, int id) throws Exception {
    File file = new File(filename);
    if (!file.exists()) return "File not found";

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    Document doc = factory.newDocumentBuilder().parse(file);

    XPathFactory xPathfactory = XPathFactory.newInstance();
    XPath xpath = xPathfactory.newXPath();

    String expression = String.format("/clients/client[@id='%d']/name", id);
    XPathExpression expr = xpath.compile(expression);

    return (String) expr.evaluate(doc, XPathConstants.STRING);
}

public static void transformXmlToHtml(String xmlFilename, String xslFilename, String htmlOutputFilename) throws Exception {
    TransformerFactory factory = TransformerFactory.newInstance();
    Source xsl = new StreamSource(new File(xslFilename));
    Transformer transformer = factory.newTransformer(xsl);

    Source xml = new StreamSource(new File(xmlFilename));
    Result htmlResult = new StreamResult(new File(htmlOutputFilename));

    transformer.transform(xml, htmlResult);
}
}

