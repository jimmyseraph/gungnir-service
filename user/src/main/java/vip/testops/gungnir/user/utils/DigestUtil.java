package vip.testops.gungnir.user.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtil {
    public static String digest(String content, String algorithm)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        byte[] bytes = messageDigest.digest(content.getBytes("utf-8"));
        return bytes2string(bytes);
    }

    public static String bytes2string(byte[] bytes){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++){
            int tmp = bytes[i] < 0 ? bytes[i] + 256 : bytes[i];
            sb.append(StringUtil.lpadding(Integer.toHexString(tmp),2,'0'));
        }
        return sb.toString();
    }

}
