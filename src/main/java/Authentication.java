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
    @GET
    public Response getPage()
    {
        try
        {
            File file = new File("/home/windspring/git_repos/snake-server/src/main/web_content/login.html");
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
	public Response authenticate(
		@FormParam("userid") String uid,
		@FormParam("password") String password) 
    {     
        //create connection for a server installed in localhost, with a user "wind"
        try{
            //STEP 2: Register JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
            // create a Statement
            try
            {
                PreparedStatement stmt = conn.prepareStatement("select * from Users where user_name=? and user_hash=?");
                String pass_hash = sha256.toHexString(sha256.getSHA(password)); 
                stmt.setString(1, uid);
                stmt.setString(2, pass_hash);
                //execute query
                try{
                    ResultSet res = stmt.executeQuery();
                    //position result to first
                    if(res.next())
                    { 
                        return Response.ok("You are authenticated").build();
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return Response.status(Response.Status.FORBIDDEN).entity("You are not a member of SKN MOS").build();
	}
}
