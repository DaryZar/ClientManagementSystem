import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Validator 
{
    private static final String LOG_FILE = "errors.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Client validateAndParse(String line) 
    {
        if (line == null || line.trim().isEmpty()) return null;

        String[] parts = line.split(";");
        if (parts.length != 5) {
            logError("Invalid field count in line: " + line);
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String filesStr = parts[2].trim();
            
            LocalDate date = LocalDate.parse(parts[3].trim(), DATE_FORMATTER);
            
            String budgetStr = parts[4].trim().replace(",", ".").replaceAll("[^0-9.]", "");
            double budget = Double.parseDouble(budgetStr);

            if (id <= 0 || name.isEmpty() || budget < 0) {
                logError("Logical value error in line: " + line);
                return null;
            }

            List<ClientFile> fileList = new ArrayList<>();
            if (!filesStr.isEmpty() && !filesStr.equals("-")) {
                String[] fileTokens = filesStr.split("\\|");
                for (String token : fileTokens) {
                    String[] fileData = token.split(":");
                    if (fileData.length == 2) {
                        fileList.add(new ClientFile(fileData[0].trim(), fileData[1].trim()));
                    } else {
                        logError("Invalid file token format '" + token + "' in line: " + line);
                        return null; 
                    }
                }
            }
            return new Client(id, name, fileList, date, budget);
        } catch (NumberFormatException | DateTimeParseException e) {
            logError("Type parsing error (" + e.getMessage() + ") in line: " + line);
            return null;
        }
    }

    public static boolean validateXml(String xmlFilename, String xsdFilename) {
        File xsdFile = new File(xsdFilename);
        if (!xsdFile.exists()) return false;
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdFile);
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlFilename)));
            return true;
        } catch (Exception e) {
            logError("XML validation failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateXmlWithDtd(String xmlFilename) {
        File file = new File(xmlFilename);
        if (!file.exists()) return false;
        try 
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override public void warning(org.xml.sax.SAXParseException e) {}
                @Override public void error(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException { throw e; }
                @Override public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException { throw e; }
            });
            builder.parse(file);
            return true;
        } catch (Exception e) {
            logError("DTD validation failed: " + e.getMessage());
            return false;
        }
    }

    public static void logError(String message) 
    {
        System.err.println("[ERROR] " + message);
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) 
        {
            String timestamp = LocalDateTime.now().format(LOG_TIMESTAMP_FORMATTER);
            out.println(timestamp + " : " + message);
        } 
        catch (IOException e) 
        {
            System.err.println("Failed to write to log: " + e.getMessage());
        }
    }
}
