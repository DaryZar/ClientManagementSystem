import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;



public class SecurityHandler 
{
    private static final String ALGORITHM = "AES";
    private static final String KEY_FILE = "secret.key";

    public static String hashString(String input) throws Exception 
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) 
        {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) 
            {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static SecretKey getSecretKey() throws Exception 
    {
        File keyFile = new File(KEY_FILE);
        if (!keyFile.exists()) 
            {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            Files.write(keyFile.toPath(), secretKey.getEncoded());
            return secretKey;
        }
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static void encryptFile(String sourceFilename, String destFilename) throws Exception 
    {
        SecretKey secretKey = getSecretKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] fileContent = Files.readAllBytes(new File(sourceFilename).toPath());
        byte[] encryptedBytes = cipher.doFinal(fileContent);

        byte[] base64Encrypted = Base64.getEncoder().encode(encryptedBytes);
        Files.write(new File(destFilename).toPath(), base64Encrypted);
    }

    public static void decryptFile(String sourceFilename, String destFilename) throws Exception 
    {
        SecretKey secretKey = getSecretKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] base64Encrypted = Files.readAllBytes(new File(sourceFilename).toPath());
        byte[] encryptedBytes = Base64.getDecoder().decode(base64Encrypted);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        Files.write(new File(destFilename).toPath(), decryptedBytes);
    }
}
