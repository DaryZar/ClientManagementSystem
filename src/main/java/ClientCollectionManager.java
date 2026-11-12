import java.util.List;
import java.util.Map;

public class ClientCollectionManager extends AbstractClientStorage 
{

    @Override
    public void addClient(Client client) 
    {
        clientList.add(client);
        clientMap.put(client.getId(), client);
    }

    @Override
    public void updateClient(Client updatedClient) 
    {
        for (int i = 0; i < clientList.size(); i++) 
        {
            if (clientList.get(i).getId() == updatedClient.getId()) 
            {
                clientList.set(i, updatedClient);
                break;
            }
        }
        clientMap.put(updatedClient.getId(), updatedClient);
    }

    @Override
    public void deleteClient(int id) 
    {
        clientList.removeIf(client -> client.getId() == id);
        clientMap.remove(id);
    }

    public List<Client> getClientList() 
    {
        return clientList;
    }
    public Map<Integer, Client> getClientMap() 
    {
        return clientMap;
    }
    
    public void clearAll() 
    {
        clientList.clear();
        clientMap.clear();
    }
}
