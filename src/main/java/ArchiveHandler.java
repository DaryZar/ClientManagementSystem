import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveHandler 
{
    public static void packToZip(String sourceFilename, String zipFilename) throws IOException 
    {
        File sourceFile = new File(sourceFilename);
        if (!sourceFile.exists()) 
        {
            throw new FileNotFoundException("Source file not found: " + sourceFilename);
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilename));
             FileInputStream fis = new FileInputStream(sourceFile)) {
            
            ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) 
            {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }

    public static void unpackFromZip(String zipFilename, String destDirectory) throws IOException 
    {
        File folder = new File(destDirectory);
        if (!folder.exists()) 
        {
            folder.mkdir();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilename))) 
        {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) 
            {
                String fileName = ze.getName();
                File newFile = new File(destDirectory + File.separator + fileName);

                if (newFile.getParentFile() != null)
                {
                    newFile.getParentFile().mkdirs();
                }
                
                try (FileOutputStream fos = new FileOutputStream(newFile)) 
                {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) 
                    {
                        fos.write(buffer, 0, len);
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    public static void packToJar(String sourceFilename, String jarFilename) throws IOException 
    {
        File sourceFile = new File(sourceFilename);
        if (!sourceFile.exists()) 
        {
            throw new FileNotFoundException("Source file not found: " + sourceFilename);
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFilename), manifest);
             FileInputStream fis = new FileInputStream(sourceFile)) 
        {
            JarEntry jarEntry = new JarEntry(sourceFile.getName());
            jarEntry.setTime(sourceFile.lastModified());
            jos.putNextEntry(jarEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) 
            {
                jos.write(buffer, 0, length);
            }
            jos.closeEntry();
        }
    }

    public static void unpackFromJar(String jarFilename, String destDirectory) throws IOException 
    {
        File folder = new File(destDirectory);
        if (!folder.exists()) 
        {
            folder.mkdir();
        }

        try (JarInputStream jis = new JarInputStream(new FileInputStream(jarFilename))) 
        {
            JarEntry je = jis.getNextJarEntry();
            while (je != null) 
            {
                if (!je.isDirectory()) 
                {
                    String fileName = je.getName();
                    File newFile = new File(destDirectory + File.separator + fileName);
                    
                    if (newFile.getParentFile() != null) 
                    {
                        newFile.getParentFile().mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) 
                    {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = jis.read(buffer)) > 0) 
                        {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                je = jis.getNextJarEntry();
            }
        }
    }
}
