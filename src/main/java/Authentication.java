package rest;
import java.sql.*;
import java.io.*; 

import javax.ws.rs.GET;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/login")
public class Authentication{
    String PWD= "/home/windspring/git_repos/snake-server/src/main/";
    @GET
    public Response getPage()
    {
        try
        {
            File file = new File(PWD + "web_content/login.html");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String page = new String(data, "UTF-8");
            return Response.ok(page).build();
        }catch(Exception e)
        {
            System.out.println("Exception thrown  :" + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("OPS, some error occured").build();
        }
    }

    @POST
	@Path("/snake")
	public Response authenticate(
		@FormParam("name") String name,
		@FormParam("age") int age) 
    {
        //create connection for a server installed in localhost, with a user "root" with no password
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost/", "wind", "alamakota")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
                try (ResultSet rs = stmt.executeQuery("SELECT 'Hello World!'")) {
                    //position result to first
                    rs.first();
                    return Response.ok("You are authenticated").build();
                }
                catch (SQLException e)
                {
                    // do something appropriate with the exception, *at least*:
                    e.printStackTrace();
                }
            }
            catch (SQLException e)
            {
                // do something appropriate with the exception, *at least*:
                e.printStackTrace();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return Response.status(Response.Status.FORBIDDEN).entity("You are not a member of SKN MOS").build();
	}
}
