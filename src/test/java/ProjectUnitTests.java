import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProjectUnitTests 
{
    private ClientCollectionManager manager;

    @TempDir(cleanup = org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS)
    Path tempDir;

    @BeforeEach
    void setUp() {
        manager = new ClientCollectionManager();
        manager.addClient(new Client(1, "Alpha Corp",
                List.of(new ClientFile("contract", "pdf")),
                LocalDate.of(2025, 5, 10), 5000.0));
        manager.addClient(new Client(2, "Beta Ltd",
                List.of(new ClientFile("spec", "doc"), new ClientFile("budget", "xls")),
                LocalDate.of(2025, 6, 15), 2500.0));
    }

    @Test
    void testClientModel_GettersSettersAndToString() {
        Client client = new Client();
        client.setId(10);
        client.setName("TestLLC");
        client.setContractDate(LocalDate.of(2025, 1, 15));
        client.setSearchBudget(1234.56);
        client.setFiles(List.of(new ClientFile("report", "xlsx")));

        assertEquals(10, client.getId());
        assertEquals("TestLLC", client.getName());
        assertEquals(LocalDate.of(2025, 1, 15), client.getContractDate());
        assertEquals(1234.56, client.getSearchBudget());
        assertEquals("report.xlsx", client.getFiles().get(0).toString());

        String str = client.toString();
        assertTrue(str.contains("id=10"));
        assertTrue(str.contains("TestLLC"));
        assertTrue(str.contains("2025-01-15"));
    }

    @Test
    void testStorageHierarchy_PolymorphismAndClear() {
        AbstractClientStorage storage = new ClientCollectionManager();
        storage.addClient(new Client(5, "PolyCorp", List.of(), LocalDate.now(), 700.0));
        assertTrue(storage instanceof ClientCollectionManager);

        ClientCollectionManager casted = (ClientCollectionManager) storage;
        assertEquals(1, casted.getClientList().size());
        assertEquals(1, casted.getClientMap().size());

        casted.clearAll();
        assertTrue(casted.getClientList().isEmpty());
        assertTrue(casted.getClientMap().isEmpty());
    }

    @Test
    void testTxtFile_SaveAndLoadMultipleClients() throws IOException {
        String txtPath = tempDir.resolve("clients.txt").toString();
        FileHandler.saveToFile(txtPath, manager);

        ClientCollectionManager loaded = new ClientCollectionManager();
        FileHandler.loadFromFile(txtPath, loaded);

        assertEquals(2, loaded.getClientList().size());
        assertEquals("Alpha Corp", loaded.getClientMap().get(1).getName());
        assertEquals(2, loaded.getClientMap().get(2).getFiles().size());
    }

    @Test
    void testTxtFile_EmptyListAndMissingFileCreation() throws IOException {
        String emptyPath = tempDir.resolve("empty.txt").toString();
        manager.clearAll();
        FileHandler.saveToFile(emptyPath, manager);

        ClientCollectionManager loaded = new ClientCollectionManager();
        FileHandler.loadFromFile(emptyPath, loaded);
        assertTrue(loaded.getClientList().isEmpty());

        String nonExistent = tempDir.resolve("auto_created.txt").toString();
        FileHandler.loadFromFile(nonExistent, loaded);
        assertTrue(new File(nonExistent).exists());
    }

    @Test
    void testValidator_ValidLinesWithAndWithoutFiles() {
        Client withFiles = Validator.validateAndParse("15;Valid Co;a:pdf|b:txt;2025-03-01;4500.00");
        assertNotNull(withFiles);
        assertEquals(15, withFiles.getId());
        assertEquals(2, withFiles.getFiles().size());

        Client noFiles = Validator.validateAndParse("16;NoFiles Co;-;2025-04-01;100.00");
        assertNotNull(noFiles);
        assertTrue(noFiles.getFiles().isEmpty());
    }

    @Test
    void testValidator_InvalidDataReturnsNull() {
        assertNull(Validator.validateAndParse(null));
        assertNull(Validator.validateAndParse("   "));
        assertNull(Validator.validateAndParse("1;OnlyTwoFields"));
        assertNull(Validator.validateAndParse("not_int;Name;-;2025-01-01;100"));
        assertNull(Validator.validateAndParse("-5;NegativeId;-;2025-01-01;100"));
        assertNull(Validator.validateAndParse("1;;-;2025-01-01;100"));
        assertNull(Validator.validateAndParse("1;Name;-;2025-99-99;100"));
        assertNull(Validator.validateAndParse("1;Name;bad_file_no_colon;2025-01-01;100"));
    }

    @Test
    void testValidator_SkipsCorruptedLinesInFile() throws IOException {
        Path mixedFile = tempDir.resolve("mixed.txt");
        Files.write(mixedFile, List.of(
                "1;Good One;-;2025-05-05;1000.00",
                "broken_line_here",
                "2;Good Two;doc:pdf;2025-06-06;2000.00"
        ));

        ClientCollectionManager loaded = new ClientCollectionManager();
        FileHandler.loadFromFile(mixedFile.toString(), loaded);

        assertEquals(2, loaded.getClientList().size());
    }

    @Test
    void testCUD_CreateAndReadFromListAndMap() {
        Client newClient = new Client(3, "Gamma", List.of(), LocalDate.now(), 300.0);
        manager.addClient(newClient);

        assertEquals(3, manager.getClientList().size());
        assertEquals(newClient, manager.getClientMap().get(3));
    }

    @Test
    void testCUD_UpdateExistingClient() {
        Client updated = new Client(1, "Alpha Renamed", List.of(), LocalDate.of(2025, 5, 10), 8888.0);
        manager.updateClient(updated);

        assertEquals("Alpha Renamed", manager.getClientList().get(0).getName());
        assertEquals(8888.0, manager.getClientMap().get(1).getSearchBudget());
    }

    @Test
    void testCUD_DeleteClient() {
        manager.deleteClient(2);
        assertEquals(1, manager.getClientList().size());
        assertNull(manager.getClientMap().get(2));

        assertDoesNotThrow(() -> manager.deleteClient(2));
    }

    @Test
    void testPrintWithIterator_OutputsAllElements() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            DataOperations.printWithIterator(manager.getClientList());
            String output = outContent.toString();
            assertTrue(output.contains("Alpha Corp"));
            assertTrue(output.contains("Beta Ltd"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testPrintWithIterator_EmptyCollectionPrintsNothing() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            DataOperations.printWithIterator(List.of());
            assertEquals("", outContent.toString().trim());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testSortByBudgetLambda() {
        manager.addClient(new Client(3, "Cheap Co", List.of(), LocalDate.now(), 100.0));
        List<Client> list = manager.getClientList();
        DataOperations.sortByBudgetLambda(list);

        assertEquals(100.0, list.get(0).getSearchBudget());
        assertEquals(2500.0, list.get(1).getSearchBudget());
        assertEquals(5000.0, list.get(2).getSearchBudget());
    }

    @Test
    void testSortByNameLambda_IgnoreCase() {
        manager.addClient(new Client(3, "aaa First", List.of(), LocalDate.now(), 100.0));
        List<Client> list = manager.getClientList();
        DataOperations.sortByNameLambda(list);

        assertEquals("aaa First", list.get(0).getName());
        assertEquals("Alpha Corp", list.get(1).getName());
        assertEquals("Beta Ltd", list.get(2).getName());
    }

    @Test
    void testPrintSortedByMap_SortsKeysAscending() {
        manager.clearAll();
        manager.addClient(new Client(30, "Thirty", List.of(), LocalDate.now(), 100.0));
        manager.addClient(new Client(5, "Five", List.of(), LocalDate.now(), 200.0));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            DataOperations.printSortedByMap(manager.getClientMap());
            String output = outContent.toString();
            assertTrue(output.indexOf("Key (ID): 5") < output.indexOf("Key (ID): 30"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testXml_JacksonSaveAndLoad() throws IOException {
        String xmlPath = tempDir.resolve("jackson.xml").toString();
        FileHandler.saveToXml(xmlPath, manager);

        ClientCollectionManager loaded = new ClientCollectionManager();
        FileHandler.loadFromXml(xmlPath, loaded);

        assertEquals(2, loaded.getClientList().size());
        assertEquals("Beta Ltd", loaded.getClientMap().get(2).getName());
    }

    @Test
    void testXml_DomSaveAndSaxLoad() throws Exception {
        String xmlPath = tempDir.resolve("dom_sax.xml").toString();
        XmlAdvancedHandler.saveWithDom(xmlPath, manager);

        ClientCollectionManager loaded = new ClientCollectionManager();
        XmlAdvancedHandler.loadWithSax(xmlPath, loaded);

        assertEquals(2, loaded.getClientList().size());
        assertEquals(5000.0, loaded.getClientMap().get(1).getSearchBudget());
    }

    @Test
    void testXml_StaxSaveAndLoad() throws Exception {
        String xmlPath = tempDir.resolve("stax.xml").toString();
        XmlAdvancedHandler.saveWithStax(xmlPath, manager);

        ClientCollectionManager loaded = new ClientCollectionManager();
        XmlAdvancedHandler.loadWithStax(xmlPath, loaded);

        assertEquals(2, loaded.getClientList().size());
        assertEquals(2, loaded.getClientMap().get(2).getFiles().size());
    }

       @Test
    void testXml_XsdValidation_ValidAndBrokenXml() throws Exception {
        Path xsdPath = tempDir.resolve("test_schema.xsd");
        String xsdContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xs:schema xmlns:xs=\"" + javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI + "\">\n" +
                "  <xs:element name=\"clients\">\n" +
                "    <xs:complexType>\n" +
                "      <xs:sequence>\n" +
                "        <xs:element name=\"client\" maxOccurs=\"unbounded\" minOccurs=\"0\">\n" +
                "          <xs:complexType>\n" +
                "            <xs:sequence>\n" +
                "              <xs:element type=\"xs:string\" name=\"name\"/>\n" +
                "              <xs:element type=\"xs:date\" name=\"contractDate\"/>\n" +
                "              <xs:element type=\"xs:double\" name=\"searchBudget\"/>\n" +
                "              <xs:element name=\"files\">\n" +
                "                <xs:complexType>\n" +
                "                  <xs:sequence>\n" +
                "                    <xs:element name=\"file\" maxOccurs=\"unbounded\" minOccurs=\"0\">\n" +
                "                      <xs:complexType>\n" +
                "                        <xs:simpleContent>\n" +
                "                          <xs:extension base=\"xs:string\">\n" +
                "                            <xs:attribute type=\"xs:string\" name=\"type\" use=\"required\"/>\n" +
                "                          </xs:extension>\n" +
                "                        </xs:simpleContent>\n" +
                "                      </xs:complexType>\n" +
                "                    </xs:element>\n" +
                "                  </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "              </xs:element>\n" +
                "            </xs:sequence>\n" +
                "            <xs:attribute type=\"xs:int\" name=\"id\" use=\"required\"/>\n" +
                "          </xs:complexType>\n" +
                "        </xs:element>\n" +
                "      </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "  </xs:element>\n" +
                "</xs:schema>";
        Files.writeString(xsdPath, xsdContent);

        String validXml = tempDir.resolve("valid.xml").toString();
        XmlAdvancedHandler.saveWithDom(validXml, manager);
        assertTrue(Validator.validateXml(validXml, xsdPath.toString()));

        Path invalidXml = tempDir.resolve("invalid.xml");
        Files.writeString(invalidXml, "<clients><wrongTag/></clients>");
        assertFalse(Validator.validateXml(invalidXml.toString(), xsdPath.toString()));
    }



    @Test
    void testXml_DtdSaveAndValidation() throws Exception {
        File dtd = new File("schema.dtd");
        Assumptions.assumeTrue(dtd.exists());

        Files.copy(dtd.toPath(), tempDir.resolve("schema.dtd"));
        String dtdXml = tempDir.resolve("dtd_clients.xml").toString();

        XmlAdvancedHandler.saveWithStaxDtd(dtdXml, manager, "schema.dtd");
        ClientCollectionManager loaded = new ClientCollectionManager();
        XmlAdvancedHandler.loadWithDomDtd(dtdXml, loaded);

        assertEquals(2, loaded.getClientList().size());
    }

    @Test
    void testXml_XPathAndXslt() throws Exception {
        String xmlPath = tempDir.resolve("xpath_xslt.xml").toString();
        XmlAdvancedHandler.saveWithDom(xmlPath, manager);

        assertEquals("Alpha Corp", XmlAdvancedHandler.evaluateXPathFindNameById(xmlPath, 1));
        assertEquals("", XmlAdvancedHandler.evaluateXPathFindNameById(xmlPath, 999));

        File xsl = new File("template.xsl");
        if (xsl.exists()) {
            String htmlPath = tempDir.resolve("report.html").toString();
            XmlAdvancedHandler.transformXmlToHtml(xmlPath, "template.xsl", htmlPath);
            String html = Files.readString(Path.of(htmlPath));
            assertTrue(html.contains("Alpha Corp"));
        }
    }

    @Test
    void testJson_SaveAndLoad() throws IOException {
        String jsonPath = tempDir.resolve("clients.json").toString();
        FileHandler.saveToJson(jsonPath, manager);

        ClientCollectionManager loaded = new ClientCollectionManager();
        FileHandler.loadFromJson(jsonPath, loaded);

        assertEquals(2, loaded.getClientList().size());
        assertEquals(LocalDate.of(2025, 6, 15), loaded.getClientMap().get(2).getContractDate());
    }

    @Test
    void testJson_LoadCorruptedThrowsException() throws IOException {
        Path badJson = tempDir.resolve("bad.json");
        Files.writeString(badJson, "{ broken json [");

        ClientCollectionManager loaded = new ClientCollectionManager();
        assertThrows(IOException.class, () -> FileHandler.loadFromJson(badJson.toString(), loaded));
    }

    @Test
    void testSecurity_Sha256Hash() throws Exception {
        String hash1 = SecurityHandler.hashString("MyPassword123");
        String hash2 = SecurityHandler.hashString("MyPassword123");
        String hashDiff = SecurityHandler.hashString("MyPassword124");

        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hashDiff);
    }

    @Test
    void testSecurity_AesEncryptAndDecryptFile() throws Exception {
        Path plainFile = tempDir.resolve("plain.xml");
        Path encFile = tempDir.resolve("encrypted.enc");
        Path decFile = tempDir.resolve("decrypted.xml");

        String secretData = "<clients><client id=\"1\"><name>Secret</name></client></clients>";
        Files.writeString(plainFile, secretData);

        SecurityHandler.encryptFile(plainFile.toString(), encFile.toString());
        assertNotEquals(secretData, Files.readString(encFile));

        SecurityHandler.decryptFile(encFile.toString(), decFile.toString());
        assertEquals(secretData, Files.readString(decFile));
    }

    @Test
    void testArchiving_ZipPackAndUnpack() throws IOException {
        Path srcFile = tempDir.resolve("data.xml");
        Path zipFile = tempDir.resolve("data.zip");
        Path outDir = tempDir.resolve("unpacked_zip");

        Files.writeString(srcFile, "Content for ZIP archive");
        ArchiveHandler.packToZip(srcFile.toString(), zipFile.toString());
        ArchiveHandler.unpackFromZip(zipFile.toString(), outDir.toString());

        Path extracted = outDir.resolve("data.xml");
        assertTrue(Files.exists(extracted));
        assertEquals("Content for ZIP archive", Files.readString(extracted));
    }

    @Test
    void testArchiving_JarPackAndUnpack() throws IOException {
        Path srcFile = tempDir.resolve("data.xml");
        Path jarFile = tempDir.resolve("data.jar");
        Path outDir = tempDir.resolve("unpacked_jar");

        Files.writeString(srcFile, "Content for JAR archive");
        ArchiveHandler.packToJar(srcFile.toString(), jarFile.toString());
        ArchiveHandler.unpackFromJar(jarFile.toString(), outDir.toString());

        Path extracted = outDir.resolve("data.xml");
        assertTrue(Files.exists(extracted));
        assertEquals("Content for JAR archive", Files.readString(extracted));
    }

    @Test
    void testArchiving_MissingSourceThrowsFileNotFound() {
        String missing = tempDir.resolve("no_such_file.xml").toString();
        assertThrows(FileNotFoundException.class, () ->
                ArchiveHandler.packToZip(missing, tempDir.resolve("out.zip").toString()));
        assertThrows(FileNotFoundException.class, () ->
                ArchiveHandler.packToJar(missing, tempDir.resolve("out.jar").toString()));
    }

    @Test
    void testClient_IsAbstractClientHeirAndKeepsFields() {
        Client client = new Client(11, "Abstract Heir",
                List.of(new ClientFile("report", "pdf")),
                LocalDate.of(2026, 1, 20), 1234.56);

        assertTrue(client instanceof AbstractClient);
        AbstractClient abstraction = client;
        assertEquals(11, abstraction.getId());
        assertEquals("Abstract Heir", abstraction.getName());
        assertEquals(1234.56, abstraction.getSearchBudget());

        abstraction.setName("Renamed Heir");
        assertEquals("Renamed Heir", client.getName());
    }

    @Test
    void testBuilder_BuildsFluentClientAndResetWorks() {
        ClientBuilder builder = new ConcreteClientBuilder();
        assertSame(builder, builder.buildId(42));

        List<ClientFile> files = List.of(new ClientFile("contract", "pdf"));
        Client built = builder
                .buildName("Builder Corp")
                .buildFiles(files)
                .buildContractDate(LocalDate.of(2026, 2, 3))
                .buildSearchBudget(9876.5)
                .getResult();

        assertEquals(42, built.getId());
        assertEquals("Builder Corp", built.getName());
        assertEquals(1, built.getFiles().size());
        assertEquals("contract.pdf", built.getFiles().get(0).toString());
        assertEquals(LocalDate.of(2026, 2, 3), built.getContractDate());
        assertEquals(9876.5, built.getSearchBudget());

        builder.reset();
        Client empty = builder.getResult();
        assertEquals(0, empty.getId());
        assertNull(empty.getName());
        assertTrue(empty.getFiles().isEmpty());
    }

    @Test
    void testDatabaseConnection_IsSingletonAndCreatesDataFile() {
        DatabaseConnection.resetForTests();
        Path dbFile = tempDir.resolve("singleton_data.txt");

        DatabaseConnection first = DatabaseConnection.getInstance(dbFile.toString());
        DatabaseConnection second = DatabaseConnection.getInstance("ignored.txt");

        assertSame(first, second);
        assertEquals(dbFile.toString(), second.getDataFilename());
        assertTrue(Files.exists(dbFile));

        DatabaseConnection.resetForTests();
    }

    @Test
    void testMenu_CreateClientUpdatesTxtFileImmediately() throws IOException {
        manager.clearAll();
        String dataFile = tempDir.resolve("menu_data.txt").toString();
        String input = "2\n7\nMenu Co\nn\n2026-07-01\n777.50\n0\n";

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            MainMenu menu = new MainMenu(manager, dataFile);
            menu.run();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        assertEquals(1, manager.getClientList().size());
        String saved = Files.readString(Path.of(dataFile));
        assertTrue(saved.contains("Menu Co"));
        assertTrue(saved.contains("777.50"));
    }

    @Test
    void testDataOperations_SortByDateAndIdWithComparator() {
        manager.addClient(new Client(3, "Future", List.of(), LocalDate.of(2026, 12, 1), 300.0));
        manager.addClient(new Client(4, "No Date", List.of(), null, 400.0));

        List<Client> list = manager.getClientList();
        DataOperations.sortByDateWithComparator(list);
        assertEquals(1, list.get(0).getId());
        assertEquals(2, list.get(1).getId());
        assertEquals(3, list.get(2).getId());
        assertEquals(4, list.get(3).getId());

        DataOperations.sortByIdWithComparator(list);
        assertEquals(1, list.get(0).getId());
        assertEquals(2, list.get(1).getId());
        assertEquals(3, list.get(2).getId());
        assertEquals(4, list.get(3).getId());
    }

    @Test
    void testRealXsdSchemaIsUsableForValidation() throws Exception {
        File xsd = new File("schema.xsd");
        Assumptions.assumeTrue(xsd.exists());

        javax.xml.validation.SchemaFactory
                .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(xsd);

        String xmlPath = tempDir.resolve("schema_check.xml").toString();
        XmlAdvancedHandler.saveWithDom(xmlPath, manager);
        assertTrue(Validator.validateXml(xmlPath, "schema.xsd"));
    }
}

