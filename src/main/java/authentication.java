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
import javax.servlet.ServletConfig;

@Path("")
public class authentication{

    @Context
    private HttpServletRequest request;
    
	@Context
	private HttpServletResponse response;

    @Context
    private ServletConfig servletConfig;

    @GET
    @Path(config.login_url)
    public void getPage() throws Exception
    {
        try
        {
            HttpSession session = request.getSession(false);
            if(session != null)
            {
                response.sendRedirect(config.getAppUrl());
                return;
            }
            request.getRequestDispatcher(config.login_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            System.out.println("Exception thrown  :" + e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error ");
        }
    }

    @POST
    @Path(config.login_url)
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
                session.setMaxInactiveInterval(3600);
                int user_privilege = res.getInt(4);
                session.setAttribute("user_info",new userDescriptor(uid, user_privilege));
                URI uri = new URI(config.app_url);
                return Response.seeOther(uri).build();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Response.status(Response.Status.FORBIDDEN).entity("You are not a member of SKN MOS").build();
	}

    @GET
    @Path(config.logout_url)
    public void logout() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not allowed to be here");
            return;
        }

        response.sendRedirect(config.getLoginUrl());
    }
}
