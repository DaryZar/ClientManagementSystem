import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "clients")
class ClientsWrapper {
    @JacksonXmlProperty(localName = "client")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Client> clients;

    public ClientsWrapper() {}
    public ClientsWrapper(List<Client> clients) { this.clients = clients; }
    public List<Client> getClients() { return clients; }
    public void setClients(List<Client> clients) { this.clients = clients; }
}

public class FileHandler 
{
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static ObjectMapper createJsonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(sdf);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    private static XmlMapper createXmlObjectMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.setDateFormat(sdf);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return xmlMapper;
    }

    public static void saveToFile(String filename, ClientCollectionManager manager) throws IOException 
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) 
        {
            for (Client c : manager.getClientList()) 
            {
                String filesSerialized = c.getFiles().isEmpty() ? "-" :
                        c.getFiles().stream()
                                .map(f -> f.getFileName() + ":" + f.getFileType())
                                .collect(Collectors.joining("|"));

                writer.write(String.format(Locale.ROOT, "%d;%s;%s;%s;%.2f",
                        c.getId(), c.getName(), filesSerialized, c.getContractDate().toString(), c.getSearchBudget()));
                writer.newLine();
            }
        }
    }

    public static void loadFromFile(String filename, ClientCollectionManager manager) throws IOException 
    {
        manager.clearAll();
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                Client client = Validator.validateAndParse(line);
                if (client != null) {
                    manager.addClient(client);
                }
            }
        }
    }

    public static void saveToJson(String filename, ClientCollectionManager manager) throws IOException 
    {
        ObjectMapper mapper = createJsonObjectMapper();
        mapper.writeValue(new File(filename), manager.getClientList());
    }

    public static void loadFromJson(String filename, ClientCollectionManager manager) throws IOException 
    {
        manager.clearAll();
        File file = new File(filename);
        if (!file.exists()) return;

        ObjectMapper mapper = createJsonObjectMapper();
        List<Client> clients = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Client.class));
        for (Client c : clients) {
            manager.addClient(c);
        }
    }

    public static void saveToXml(String filename, ClientCollectionManager manager) throws IOException 
    {
        XmlMapper xmlMapper = createXmlObjectMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        ClientsWrapper wrapper = new ClientsWrapper(manager.getClientList());
        xmlMapper.writeValue(new File(filename), wrapper);
    }

    public static void loadFromXml(String filename, ClientCollectionManager manager) throws IOException 
    {
        manager.clearAll();
        File file = new File(filename);
        if (!file.exists()) return;
        
        XmlMapper xmlMapper = createXmlObjectMapper();
        ClientsWrapper wrapper = xmlMapper.readValue(file, ClientsWrapper.class);
        if (wrapper != null && wrapper.getClients() != null) {
            for (Client c : wrapper.getClients()) {
                manager.addClient(c);
            }
        }
    }
}