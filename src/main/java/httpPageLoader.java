package rest;
import java.io.*;

public class httpPageLoader{
    private static String appPath = System.getProperty("catalina.base") + "/webapps"; 
    
    public static String loadHTML(String filename, String context) throws Exception{
        String fPath = appPath + context + "/" + filename;
        File file = new File(fPath);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data, "UTF-8");
    }
};
