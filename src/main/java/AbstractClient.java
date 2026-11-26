import java.time.LocalDate;
import java.util.List;

/**
 * Абстрактный контракт сущности «клиент».
 * Конкретная реализация хранит поля и описана в классе Client.
 */
public abstract class AbstractClient 
{
    public abstract int getId();
    public abstract void setId(int id);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract List<ClientFile> getFiles();
    public abstract void setFiles(List<ClientFile> files);

    public abstract LocalDate getContractDate();
    public abstract void setContractDate(LocalDate contractDate);

    public abstract double getSearchBudget();
    public abstract void setSearchBudget(double searchBudget);
}
