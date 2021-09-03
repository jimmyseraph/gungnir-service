package vip.testops.gungnir.gateway.utils;

public class StringUtil {
    /**
     * test the string is null or empty
     * @param content the string to be tested
     * @return true if the string is null or empty, otherwise return false
     */
    public static boolean isEmptyOrNull(String content){
        if(content == null){
            return true;
        }else return content.trim().equals("");
    }

    /**
     * padding the string from left with the specified character
     * @param content ordinary string to be padding
     * @param len the string length after padding
     * @param pad the character to fill in to the string
     * @return the padding string
     */
    public static String lpadding(String content, int len, char pad){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < len - content.length(); i++){
            sb.append(pad);
        }
        sb.append(content);
        return sb.toString();
    }

}
