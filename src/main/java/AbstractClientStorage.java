import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractClientStorage 
{
    protected List<Client> clientList = new ArrayList<>();
    protected Map<Integer, Client> clientMap = new HashMap<>();

    public abstract void addClient(Client client);
    public abstract void updateClient(Client client);
    public abstract void deleteClient(int id);
}
