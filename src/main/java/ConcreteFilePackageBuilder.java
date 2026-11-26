import java.io.File;
import java.util.List;

/**
 * Конкретный строитель: реализует шаги сборки пакета файлов.
 */
public class ConcreteFilePackageBuilder extends FilePackageBuilder
{
    private FilePackage currentPackage;

    public ConcreteFilePackageBuilder()
    {
        reset();
    }

    @Override
    public void reset()
    {
        currentPackage = new FilePackage();
    }

    @Override
    public FilePackageBuilder addFile(ClientFile file)
    {
        currentPackage.addFile(file);
        return this;
    }

    @Override
    public FilePackageBuilder addFile(File file)
    {
        if (file != null)
        {
            currentPackage.addFile(toClientFile(file));
        }
        return this;
    }

    @Override
    public FilePackageBuilder addFiles(List<ClientFile> files)
    {
        if (files != null)
        {
            for (ClientFile file : files)
            {
                addFile(file);
            }
        }
        return this;
    }

    @Override
    public FilePackageBuilder addFiles(File[] files)
    {
        if (files != null)
        {
            for (File file : files)
            {
                addFile(file);
            }
        }
        return this;
    }

    @Override
    public FilePackage getResult()
    {
        return currentPackage;
    }

    /** Преобразует java.io.File в сущность проекта ClientFile. */
    private ClientFile toClientFile(File file)
    {
        String fullName = file.getName();
        int dot = fullName.lastIndexOf('.');
        if (dot > 0 && dot < fullName.length() - 1)
        {
            String name = fullName.substring(0, dot);
            String type = fullName.substring(dot + 1).toLowerCase();
            return new ClientFile(name, type);
        }
        return new ClientFile(fullName, "");
    }
}
