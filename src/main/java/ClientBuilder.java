import java.time.LocalDate;
import java.util.List;

public abstract class ClientBuilder 
{
    protected int id;
    protected String name;
    protected List<ClientFile> files;
    protected LocalDate contractDate;
    protected double searchBudget;

    public abstract ClientBuilder buildId(int id);
    public abstract ClientBuilder buildName(String name);
    public abstract ClientBuilder buildFiles(List<ClientFile> files);
    public abstract ClientBuilder buildContractDate(LocalDate contractDate);
    public abstract ClientBuilder buildSearchBudget(double searchBudget);
    public abstract Client getResult();
    public abstract void reset();
}
