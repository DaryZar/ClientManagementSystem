import java.io.File;
import java.util.List;

/**
 * Директор из схемы Строителя.
 * Задаёт порядок шагов: reset -> добавить файлы -> результат берёт клиент у строителя.
 */
public class FilePackageDirector
{
    private final FilePackageBuilder builder;

    public FilePackageDirector(FilePackageBuilder builder)
    {
        this.builder = builder;
    }

    public void construct(List<ClientFile> files)
    {
        builder.reset();
        builder.addFiles(files);
    }

    public void construct(File[] files)
    {
        builder.reset();
        builder.addFiles(files);
    }

    public void construct(ClientFile file)
    {
        builder.reset();
        builder.addFile(file);
    }

    public void construct(File file)
    {
        builder.reset();
        builder.addFile(file);
    }
}
