import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Client 
{
    @JacksonXmlProperty(isAttribute = true)
    private int id;
    private String name;
    private List<ClientFile> files;
    private LocalDate contractDate;
    private double searchBudget;

    public Client() 
    {
        this.files = new ArrayList<>();
    }

    public Client(int id, String name, List<ClientFile> files, LocalDate contractDate, double searchBudget) 
    {
        this.id = id;
        this.name = name;
        this.files = files != null ? files : new ArrayList<>();
        this.contractDate = contractDate;
        this.searchBudget = searchBudget;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ClientFile> getFiles() { return files; }
    public void setFiles(List<ClientFile> files) { this.files = files; }

    public LocalDate getContractDate() { return contractDate; }
    public void setContractDate(LocalDate contractDate) { this.contractDate = contractDate; }

    public double getSearchBudget() { return searchBudget; }
    public void setSearchBudget(double searchBudget) { this.searchBudget = searchBudget; }

    @Override
    public String toString() 
    {
        String dateStr = (contractDate != null) ? contractDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : "null";
        return String.format("Client{id=%d, name='%s', files=%s, date=%s, budget=%.2f}",
                id, name, files.toString(), dateStr, searchBudget);
    }
}
