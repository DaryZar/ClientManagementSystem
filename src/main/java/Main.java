public class Main 
{
    public static void main(String[] args) 
    {
        // Singleton: единственная точка доступа к файлу данных.
        DatabaseConnection connection = DatabaseConnection.getInstance("data.txt");
        ClientCollectionManager manager = new ClientCollectionManager();
        startApp(manager, connection.getDataFilename());
    }

    private static void startApp(ClientCollectionManager manager, String dataFilename)
    {
        MainMenu menu = new MainMenu(manager, dataFilename);
        menu.run();
    }
}
