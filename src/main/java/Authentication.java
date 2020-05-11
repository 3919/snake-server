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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import java.util.Date;
import java.util.logging.*;

@Path("")
public class Authentication{

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Context
    private ServletConfig servletConfig;

    @Inject
    private SystemCore sc;

    @GET
    @Path(Config.login_url)
    public void getPage() throws Exception
    {
        try
        {
            HttpSession session = request.getSession(false);
            if(session != null)
            {
                response.sendRedirect(Config.getAppUrl());
                return;
            }
            request.getRequestDispatcher(Config.login_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            System.out.println("Exception thrown  :" + e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error ");
        }
    }

    @POST
    @Path(Config.login_url)
    public Response authenticate(
        @FormParam("userid") String login,
        @FormParam("password") String password) 
    {
        //create connection for a server installed in localhost, with a user "wind"
        try{
            //Register JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
            // create a Statement
            PreparedStatement stmt = conn.prepareStatement("select * from Users where login=? and pass_hash=?");
            String pass_hash = Sha256.toHexString(Sha256.getSHA(password)); 
            stmt.setString(1, login);
            stmt.setString(2, pass_hash);
            //execute query
            ResultSet res = stmt.executeQuery();
            // check if user exist in db and passed correct data
            if(res.next())
            {
                String account_expire_time = res.getString(9);
                if(!isAccountValid(account_expire_time))
                {
                    sc.log(Level.WARNING, "User {0} try to log, hovewer account expired",new String[] {login});
                    return Response.status(Response.Status.FORBIDDEN).entity("Your account expired. Please contact the head of skn mos").build();
                }

                InputStream input = res.getBinaryStream("rfid");
                byte[] rfid = new byte[1024];
                input.read(rfid);

                HttpSession session = request.getSession(true);
                int id = res.getInt(1);
                int user_Privilege = res.getInt(4);
                int pin = res.getInt(5);
                String user_name = res.getString(6);
                String user_surname = res.getString(7);
                String user_nick = res.getString(8); 
                String user_email = res.getString(10); 
                UserDescriptor u = new UserDescriptor(id,
                                                      login, 
                                                      user_Privilege,
                                                      pin,
                                                      user_name, 
                                                      user_surname, 
                                                      user_nick,
                                                      account_expire_time,
                                                      user_email,
                                                      rfid);
                session.setAttribute("user_info", u);
                sc.addUser(u);
                sc.log(Level.INFO, "User: {0}, authorized",new String[] {login});
                URI uri = new URI(Config.app_url);
                return Response.seeOther(uri).build();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        sc.log(Level.WARNING, "Unauthorized logging action detected. Login: {0}",new String[] {login});
        return Response.status(Response.Status.FORBIDDEN).entity("You are not a member of skn mos").build();
    }

    @GET
    @Path(Config.logout_url)
    public void logout() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not allowed to be here");
            return;
        }
        session.invalidate();
        response.sendRedirect(Config.getLoginUrl());
    }

    public boolean isAccountValid(String d) throws Exception
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //Parsing the given String to Date object
        Date date = formatter.parse(d);
        return date.compareTo(new Date()) > 0;
    }
}
