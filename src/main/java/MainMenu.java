import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainMenu {
    private final ClientCollectionManager manager;
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final String dataFilename;

    public MainMenu(ClientCollectionManager manager) {
        this(manager, DatabaseConnection.getInstance("data.txt").getDataFilename());
    }

    public MainMenu(ClientCollectionManager manager, String dataFilename) {
        this.manager = manager;
        this.dataFilename = dataFilename;
    }

        public void run() {
        try {
            FileHandler.loadFromFile(dataFilename, manager);
        } catch (IOException e) {
            System.out.println("Error loading initial file: " + e.getMessage());
        }

        while (true) {
            System.out.println("\n--- CLIENT MANAGEMENT MENU ---");
            System.out.println("1. Show all clients (via Iterator)");
            System.out.println("2. Add new client (Create)");
            System.out.println("3. Edit client information (Update)");
            System.out.println("4. Delete client (Delete)");
            System.out.println("5. Sort by budget (Lambda)");
            System.out.println("6. Sort by name (Lambda)");
            System.out.println("7. Show sorted structure (SortMap)");
            System.out.println("8. Save changes to TXT file");
            System.out.println("9. JSON: Save to data.json");
            System.out.println("10. JSON: Load from data.json");
            System.out.println("11. XML: Save to data.xml");
            System.out.println("12. XML: Load from data.xml");
            System.out.println("13. Advanced XML: Save via DOM");
            System.out.println("14. Advanced XML: Load via SAX (XSD)");
            System.out.println("15. Advanced XML: Save via StAX");
            System.out.println("16. Advanced XML: Load via StAX (XSD)");
            System.out.println("17. Advanced XML: XPath Search Name by ID");
            System.out.println("18. Advanced XML: XSLT Transform XML to HTML Report");
            System.out.println("19. Advanced XML: Save via StAX (with DTD Link)");
            System.out.println("20. Advanced XML: Load via DOM (DTD Validation)");
            System.out.println("21. Security: Hash string/password (SHA-256)");
            System.out.println("22. Security: Encrypt data.xml (AES with Key)");
            System.out.println("23. Security: Decrypt data.xml (AES with Key)");
            System.out.println("24. Archiving: Pack data.xml to ZIP");
            System.out.println("25. Archiving: Unpack data.xml from ZIP");
            System.out.println("26. Archiving: Pack data.xml to JAR");
            System.out.println("27. Archiving: Unpack data.xml from JAR");
            System.out.println("28. Sort by contract date (Collections/Comparator)");
            System.out.println("29. Sort by ID (Collections/Comparator)");
            System.out.println("0. Exit");
            System.out.print("Select action: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number from the menu.");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> DataOperations.printWithIterator(manager.getClientList());
                    case 2 -> executeCreate();
                    case 3 -> executeUpdate();
                    case 4 -> executeDelete();
                    case 5 -> {
                        List<Client> sortedList = new ArrayList<>(manager.getClientList());
                        DataOperations.sortByBudgetLambda(sortedList);
                        System.out.println("List sorted by budget:");
                        DataOperations.printWithIterator(sortedList);
                    }
                    case 6 -> {
                        List<Client> sortedList = new ArrayList<>(manager.getClientList());
                        DataOperations.sortByNameLambda(sortedList);
                        System.out.println("List sorted by name:");
                        DataOperations.printWithIterator(sortedList);
                    }
                    case 7 -> DataOperations.printSortedByMap(manager.getClientMap());
                    case 8 -> {
                        FileHandler.saveToFile(dataFilename, manager);
                        System.out.println("Data saved to TXT successfully.");
                    }
                    case 9 -> {
                        FileHandler.saveToJson("data.json", manager);
                        System.out.println("Data saved to JSON successfully.");
                    }
                    case 10 -> {
                        FileHandler.loadFromJson("data.json", manager);
                        System.out.println("Data loaded from JSON successfully.");
                    }
                    case 11 -> {
                        FileHandler.saveToXml("data.xml", manager);
                        System.out.println("Data saved to XML successfully.");
                    }
                    case 12 -> {
                        FileHandler.loadFromXml("data.xml", manager);
                        System.out.println("Data loaded from XML successfully.");
                    }
                    case 13 -> {
                        XmlAdvancedHandler.saveWithDom("dom_data.xml", manager);
                        System.out.println("Saved via DOM successfully to dom_data.xml");
                    }
                    case 14 -> {
                        XmlAdvancedHandler.loadWithSax("dom_data.xml", manager);
                        System.out.println("Loaded via SAX successfully from dom_data.xml");
                    }
                    case 15 -> {
                        XmlAdvancedHandler.saveWithStax("stax_data.xml", manager);
                        System.out.println("Saved via StAX successfully to stax_data.xml");
                    }
                    case 16 -> {
                        XmlAdvancedHandler.loadWithStax("stax_data.xml", manager);
                        System.out.println("Loaded via StAX successfully from stax_data.xml");
                    }
                    case 17 -> {
                        int searchId = readValidInt("Enter client ID to search via XPath: ");
                        String foundName = XmlAdvancedHandler.evaluateXPathFindNameById("dom_data.xml", searchId);
                        System.out.println("XPath Result (Company Name): " + (foundName.isEmpty() ? "Not Found" : foundName));
                    }
                    case 18 -> {
                        XmlAdvancedHandler.transformXmlToHtml("dom_data.xml", "template.xsl", "report.html");
                        System.out.println("XSLT Transformation completed! Check 'report.html'.");
                    }
                    case 19 -> {
                        XmlAdvancedHandler.saveWithStaxDtd("dtd_data.xml", manager, "schema.dtd");
                        System.out.println("Saved via StAX with DTD reference successfully to dtd_data.xml");
                    }
                    case 20 -> {
                        XmlAdvancedHandler.loadWithDomDtd("dtd_data.xml", manager);
                        System.out.println("Loaded and validated via DOM/DTD successfully from dtd_data.xml");
                    }
                    case 21 -> {
                        System.out.print("Enter string/password to hash: ");
                        String pass = scanner.nextLine();
                        System.out.println("SHA-256 Hash: " + SecurityHandler.hashString(pass));
                    }
                    case 22 -> {
                        SecurityHandler.encryptFile("data.xml", "data_encrypted.enc");
                        System.out.println("File 'data.xml' successfully encrypted to 'data_encrypted.enc'.");
                    }
                    case 23 -> {
                        SecurityHandler.decryptFile("data_encrypted.enc", "data_decrypted.xml");
                        System.out.println("File 'data_encrypted.enc' successfully decrypted to 'data_decrypted.xml'.");
                    }
                    case 24 -> {
                        ArchiveHandler.packToZip("data.xml", "data.zip");
                        System.out.println("File 'data.xml' successfully zipped into 'data.zip'.");
                    }
                    case 25 -> {
                        ArchiveHandler.unpackFromZip("data.zip", "./unpacked");
                        System.out.println("Archived files successfully extracted into './unpacked' folder.");
                    }
                    case 26 -> {
                        ArchiveHandler.packToJar("data.xml", "data.jar");
                        System.out.println("File 'data.xml' successfully archived into 'data.jar'.");
                    }
                    case 27 -> {
                        ArchiveHandler.unpackFromJar("data.jar", "./unpacked_jar");
                        System.out.println("JAR files successfully extracted into './unpacked_jar' folder.");
                    }
                    case 28 -> {
                        List<Client> sortedList = new ArrayList<>(manager.getClientList());
                        DataOperations.sortByDateWithComparator(sortedList);
                        System.out.println("List sorted by contract date:");
                        DataOperations.printWithIterator(sortedList);
                    }
                    case 29 -> {
                        List<Client> sortedList = new ArrayList<>(manager.getClientList());
                        DataOperations.sortByIdWithComparator(sortedList);
                        System.out.println("List sorted by ID:");
                        DataOperations.printWithIterator(sortedList);
                    }

                    case 0 -> {
                        System.out.println("Exiting program.");
                        return;
                    }
                    default -> System.out.println("Invalid menu option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Operation error: " + e.getMessage());
            }
        }
    }


    private void executeCreate() {
        int id = readValidInt("Enter ID: ");
        System.out.print("Enter Company Name: ");
        String name = scanner.nextLine().trim();
        while (name.isEmpty()) {
            Validator.logError("Invalid company name input: ''");
            System.out.print("Name cannot be empty! Enter Company Name: ");
            name = scanner.nextLine().trim();
        }
        List<ClientFile> files = readClientFiles();
        LocalDate date = readValidDate("Enter contract date (YYYY-MM-DD): ");
        double budget = readValidDouble("Enter search budget: ");

        ClientBuilder builder = new ConcreteClientBuilder();
        Client client = builder
                .buildId(id)
                .buildName(name)
                .buildFiles(files)
                .buildContractDate(date)
                .buildSearchBudget(budget)
                .getResult();
        manager.addClient(client);
        saveChanges();
        System.out.println("Client added successfully.");
    }

    private void executeUpdate() {
        int id = readValidInt("Enter client ID to update: ");
        if (!manager.getClientMap().containsKey(id)) {
            System.out.println("Client with this ID not found.");
            return;
        }
        System.out.print("Enter new Name: ");
        String name = scanner.nextLine().trim();
        while (name.isEmpty()) {
            Validator.logError("Invalid company name input: ''");
            System.out.print("Name cannot be empty! Enter new Name: ");
            name = scanner.nextLine().trim();
        }
        List<ClientFile> files = readClientFiles();
        LocalDate date = readValidDate("Enter new contract date (YYYY-MM-DD): ");
        double budget = readValidDouble("Enter new budget: ");

        ClientBuilder builder = new ConcreteClientBuilder();
        Client updatedClient = builder
                .buildId(id)
                .buildName(name)
                .buildFiles(files)
                .buildContractDate(date)
                .buildSearchBudget(budget)
                .getResult();
        manager.updateClient(updatedClient);
        saveChanges();
        System.out.println("Information updated.");
    }

    private void executeDelete() {
        int id = readValidInt("Enter ID to delete: ");
        if (manager.getClientMap().containsKey(id)) {
            manager.deleteClient(id);
            saveChanges();
            System.out.println("Client deleted successfully.");
        } else {
            System.out.println("Client with this ID not found.");
        }
    }

    private void saveChanges() {
        try {
            FileHandler.saveToFile(dataFilename, manager);
        } catch (IOException e) {
            String message = "Failed to save data to TXT: " + e.getMessage();
            System.out.println(message);
            Validator.logError(message);
        }
    }

    private List<ClientFile> readClientFiles() {
        List<ClientFile> files = new ArrayList<>();
        while (true) {
            System.out.print("Add file to client? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (!answer.equals("y")) {
                break;
            }
            System.out.print("Enter file name: ");
            String fName = scanner.nextLine().trim();
            while (fName.isEmpty()) {
                Validator.logError("Invalid file name input: ''");
                System.out.print("File name cannot be empty! Enter file name: ");
                fName = scanner.nextLine().trim();
            }
            System.out.print("Enter file type (txt, doc, pdf, xls, json): ");
                        String fType = scanner.nextLine().trim().toLowerCase();
            while (fType.isEmpty()) {
                Validator.logError("Invalid file type input: ''");
                System.out.print("File type cannot be empty! Enter file type: ");
                fType = scanner.nextLine().trim().toLowerCase();
            }
            files.add(new ClientFile(fName, fType));
        }
        return files;
    }

    private int readValidInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid integer input: '" + input + "'";
                System.out.println("Invalid number format! Please enter an integer.");
                Validator.logError(errorMessage);
            }
        }
    }

    private double readValidDouble(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid double input: '" + input + "'";
                System.out.println("Invalid double format! Please enter a decimal number.");
                Validator.logError(errorMessage);
            }
        }
    }

    private LocalDate readValidDate(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                String errorMessage = "Invalid date input: '" + input + "'";
                System.out.println("Invalid date format! Use YYYY-MM-DD.");
                Validator.logError(errorMessage);
            }
        }
    }
}

