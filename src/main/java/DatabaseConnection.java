import java.io.File;
import java.io.IOException;

public class DatabaseConnection 
{
    private static DatabaseConnection instance;
    private final String dataFilename;

    private DatabaseConnection(String dataFilename) 
    {
        this.dataFilename = dataFilename;
        initializeDatabaseFile();
    }

    public static synchronized DatabaseConnection getInstance(String filename) 
    {
        if (instance == null) 
        {
            instance = new DatabaseConnection(filename);
        }
        return instance;
    }

    /** Используется только в тестах, чтобы каждый тест начинался с чистого состояния. */
    static synchronized void resetForTests() 
    {
        instance = null;
    }

    public String getDataFilename() 
    {
        return dataFilename;
    }

    private void initializeDatabaseFile() 
    {
        try 
        {
            File file = new File(dataFilename);
            if (!file.exists()) 
            {
                file.createNewFile();
            }
        } 
        catch (IOException e) 
        {
            System.err.println(e.getMessage());
        }
    }
}
