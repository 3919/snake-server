package rest;
import java.sql.*;
import java.io.*; 

import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.servlet.http.*;
import java.net.URI;
@Path("/login")
public class authentication{

    @Context
    private HttpServletRequest request;

    @GET
    public Response getPage()
    {
        try
        {
            String page = httpPageLoader.loadHTML("login.html",request.getContextPath());
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
            PreparedStatement stmt = conn.prepareStatement("select * from Users where user_name=? and user_hash=?");
            String pass_hash = sha256.toHexString(sha256.getSHA(password)); 
            stmt.setString(1, uid);
            stmt.setString(2, pass_hash);
            //execute query
            ResultSet res = stmt.executeQuery();
            // check if user exist in db and passed correct data
            if(res.next())
            { 
                HttpSession session = request.getSession(true);
                System.out.print("Session = " + session);
                session.setMaxInactiveInterval(60);
                URI uri = new URI("/app");
                return Response.temporaryRedirect(uri).build();
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
