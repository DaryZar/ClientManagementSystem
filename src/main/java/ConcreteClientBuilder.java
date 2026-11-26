import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConcreteClientBuilder extends ClientBuilder 
{
    @Override
    public ClientBuilder buildId(int id) 
    {
        this.id = id;
        return this;
    }

    @Override
    public ClientBuilder buildName(String name) 
    {
        this.name = name;
        return this;
    }

    @Override
    public ClientBuilder buildFiles(List<ClientFile> files) 
    {
        this.files = files != null ? new ArrayList<>(files) : new ArrayList<>();
        return this;
    }

    @Override
    public ClientBuilder buildContractDate(LocalDate contractDate) 
    {
        this.contractDate = contractDate;
        return this;
    }

    @Override
    public ClientBuilder buildSearchBudget(double searchBudget) 
    {
        this.searchBudget = searchBudget;
        return this;
    }

    @Override
    public Client getResult() 
    {
        return new Client(id, name, files, contractDate, searchBudget);
    }

    @Override
    public void reset() 
    {
        id = 0;
        name = null;
        files = new ArrayList<>();
        contractDate = null;
        searchBudget = 0.0;
    }
}
