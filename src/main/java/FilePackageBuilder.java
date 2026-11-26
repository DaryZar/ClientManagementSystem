import java.io.File;
import java.util.List;

/**
 * Интерфейс строителя по схеме Refactoring.Guru.
 * Объявляет ВСЕ шаги сборки продукта "пакет файлов".
 */
public abstract class FilePackageBuilder
{
    /** Начать сборку заново: создать пустой продукт. */
    public abstract void reset();

    /** Добавить один файл-описатель в собираемый пакет. */
    public abstract FilePackageBuilder addFile(ClientFile file);

    /** Добавить реальный файл из файловой системы. */
    public abstract FilePackageBuilder addFile(File file);

    /** Добавить список файлов-описателей. */
    public abstract FilePackageBuilder addFiles(List<ClientFile> files);

    /** Добавить массив реальных файлов. */
    public abstract FilePackageBuilder addFiles(File[] files);

    /** Вернуть готовый пакет файлов. */
    public abstract FilePackage getResult();
}
