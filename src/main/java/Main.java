public class Main 
{
    public static void main(String[] args) 
    {
        ClientCollectionManager manager = new ClientCollectionManager();
        startApp(manager);
    }
    private static void startApp(ClientCollectionManager manager) 
    {
        MainMenu menu = new MainMenu(manager);
        menu.run();
    }
}
