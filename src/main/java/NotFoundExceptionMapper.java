package rest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.*;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<ClientErrorException> {
    private static String appPath = System.getProperty("catalina.base") + "/webapps";

    @Override
    public Response toResponse(ClientErrorException exception) 
    {
        try{
            String fPath = appPath + Config.war_url + "/404.html";
            File file = new File(fPath);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return Response.status(404).entity(new String(data, "UTF-8")).type("text/html").build();
        }
        catch(Exception e)
        {
            return Response.status(500).entity("Internal server error:(").type("text/plain").build();
        }

     }
}
