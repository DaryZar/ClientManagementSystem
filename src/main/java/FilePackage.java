import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Продукт паттерна "Строитель".
 * Хранит пакет файлов клиента в виде списка.
 */
public class FilePackage
{
    private final List<ClientFile> files = new ArrayList<>();

    public void addFile(ClientFile file)
    {
        if (file != null)
        {
            files.add(file);
        }
    }

    public void addFiles(List<ClientFile> newFiles)
    {
        if (newFiles == null)
        {
            return;
        }
        for (ClientFile file : newFiles)
        {
            addFile(file);
        }
    }

    /** Возвращает копию списка, чтобы внешний код не мог испортить пакет. */
    public List<ClientFile> getFiles()
    {
        return new ArrayList<>(files);
    }

    public int size()
    {
        return files.size();
    }

    /** Итератор для вывода файлов пакета. */
    public Iterator<ClientFile> iterator()
    {
        return Collections.unmodifiableList(files).iterator();
    }

    @Override
    public String toString()
    {
        return "FilePackage" + files.toString();
    }
}
