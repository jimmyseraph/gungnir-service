package vip.testops.gungnir.user.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {
    public static final String KEY_ALGORITHM = "AES";
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static final String IV_STRING = "qazwsxedqazwsxed";

    public static String decrypt(String encryptValue, Key key) throws Exception {
        return aesDecryptByBytes(base64Decode(encryptValue), key);
    }

    public static String encrypt(String value, Key key) throws Exception {
        return base64Encode(aesEncryptToBytes(value, key));
    }

    private static String base64Encode(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }

    private static byte[] base64Decode(String base64Code) {
        if (base64Code == null) {
            return null;
        } else {
            return Base64.getDecoder().decode(base64Code);
        }
    }

    public static byte[] generateKey(String aesKey) throws Exception {
        KeyGenerator kgen =KeyGenerator.getInstance(KEY_ALGORITHM);
        kgen.init(128, new SecureRandom(aesKey.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        byte[] encodeFormat = secretKey.getEncoded();
        SecretKeySpec keySpec = new SecretKeySpec(encodeFormat, KEY_ALGORITHM);
        return keySpec.getEncoded();
    }

    public static Key convertToKey(byte[] keyBytes) throws Exception{
        SecretKey secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        return secretKey;
    }

    private static AlgorithmParameters generateIV(String ivVal) throws Exception{
        //iv 为一个 16 字节的数组
        byte[] iv = ivVal.getBytes();
        AlgorithmParameters params = AlgorithmParameters.getInstance(KEY_ALGORITHM);
        params.init(new IvParameterSpec(iv));
        return params;
    }

    private static byte[] aesEncryptToBytes(String content, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        AlgorithmParameters iv = generateIV(IV_STRING);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(content.getBytes("utf-8"));
    }

    private static String aesDecryptByBytes(byte[] encryptBytes, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        AlgorithmParameters iv = generateIV(IV_STRING);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }

}
