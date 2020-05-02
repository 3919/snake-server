package rest;
import java.sql.*;
import java.io.*; 

import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.servlet.http.*;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;
import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.text.ParseException;
import java.util.logging.*;

@Path(config.app_url)
public class snakeApp{

    public final class PassStatus{
        public static final int PASS_CHANGE_OK     = 0;
        public static final int PASS_CHANGE_FAILED = 1;
        public static final int PASS_IDLE = -1;
    }
    
    @Context
    private HttpServletRequest request;
    
	@Context
	private HttpServletResponse response;
    
    @Inject
    private systemCore sc;

    @GET
    public void renderApp() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        appSetAttributes(PassStatus.PASS_IDLE, u);
        request.getRequestDispatcher(config.snake_page)
               .forward(request, response);
    }

    void appSetAttributes(int pass_status, userDescriptor u)
    {
        laboratoryState l = sc.getLabState();
        request.setAttribute("u_info",       u);
        request.setAttribute("active_users", l.loggedUsers);
        request.setAttribute("active_sensors", l.sensors);
        request.setAttribute("response_msg", pass_status);
    }

    @POST
    public void changeUserPassword(
		@FormParam("old_pass") String o_password, 
		@FormParam("new_pass") String n_password, 
		@FormParam("new_pass_repeated") String r_password) 
        throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        // checkc if old password is correct
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select * from users where login=? and pass_hash=?");
        String pass_hash = sha256.toHexString(sha256.getSHA(o_password)); 
        String login = u.getuserlogin(); 
        stmt.setString(1, login);
        stmt.setString(2, pass_hash);
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
            appSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }
        // new passwords match?
        if(!n_password.equals(r_password))  
        {
            appSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }
        
        // update new password
        stmt = conn.prepareStatement("UPDATE Users SET pass_hash=? WHERE login=?");
        pass_hash = sha256.toHexString(sha256.getSHA(n_password)); 

        stmt.setString(1, pass_hash);
        stmt.setString(2, login);
        int rowsUpdated = stmt.executeUpdate();
        if(rowsUpdated == 0)
        {
            appSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }

        appSetAttributes(PassStatus.PASS_CHANGE_OK, u);
        request.getRequestDispatcher(config.snake_page)
               .forward(request, response);
        return;
    }
    @GET
    @Path(config.download_logs_url)
    @Produces("text/plain")
    public Response getTextFile() {
 
        File file = new File(config.log_name);
 
        ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition", "attachment; filename=\""+ config.log_name +"\"");
        return response.build();
    }

    @GET
    @Path(config.lab_unlock_url)
    public void ulock_from_page() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        sc.unlockLaboratory(u.getuserlogin());

        response.sendRedirect(config.getAppUrl());
    }

    @GET
    @Path(config.lab_lock_url)
    public void lock_from_page()throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        sc.lockLaboratory(u.getuserlogin());
        
        response.sendRedirect(config.getAppUrl());
    }

    @POST
    @Path(config.active_users_by_mac_url) 
    public Response log_active_users_by_mac(
            @FormParam("mac") String mac_addr
           )throws Exception
    {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select login from mac where mac=?");
        
        stmt.setString(1,mac_addr);
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
            sc.log(Level.WARNING, "Mac {0} not found",new String[] {mac_addr});
           return Response.status(Response.Status.FORBIDDEN).entity("").build();
        }
        String user = res.getString(1); 
        sc.log(Level.INFO, "User {0} detected in laboratory by his MAC attached to network",new String[] {user});
        return Response.ok("").build();
    }

    @POST
    @Path(config.lab_sensor_unlock_url) 
    public Response unlock_from_sensors(
            @FormParam("pin") int pin,
            @FormParam("rfid") String rfid
           )throws Exception
    {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select login, rfid, OCTET_LENGTH(rfid) from users where pin=?");
        stmt.setInt(1, pin);
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
           return Response.status(Response.Status.FORBIDDEN).entity("").build();
        }

        String login= res.getString(1);
        InputStream input = res.getBinaryStream("rfid");
        int rfid_size = res.getInt(2);
        byte[] rfid_raw = new byte[rfid_size];
        input.read(rfid_raw);
        String rfid_db = sha256.toHexString(rfid_raw);
        if(rfid.equals(rfid_db) == false)
        {
           return Response.status(Response.Status.FORBIDDEN).entity("").build();
        }

        sc.unlockLaboratory(login);
        return Response.ok("").build();
    }

    @POST
    @Path(config.lab_sensor_lock_url) 
    public Response lock_from_sensors(
            @FormParam("pin") int pin,
            @FormParam("rfid") String rfid
           )throws Exception
    {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select login, rfid, OCTET_LENGTH(rfid) from users where pin=?");
        stmt.setInt(1, pin);
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
           return Response.status(Response.Status.FORBIDDEN).entity("").build();
        }
        String login= res.getString(1);
        InputStream input = res.getBinaryStream("rfid");
        int rfid_size = res.getInt(2);
        byte[] rfid_raw = new byte[rfid_size];
        input.read(rfid_raw);
        String rfid_db = sha256.toHexString(rfid_raw);
        if(rfid.equals(rfid_db) == false)
        {
           return Response.status(Response.Status.FORBIDDEN).entity("").build();
        }

        sc.lockLaboratory(login);
        return Response.ok("").build();
    }

    @GET
    @Path(config.logout_url)
    public Response logout() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            URI uri = new URI(config.getLoginUrl());
            return Response.seeOther(uri).build();
        }

        URI uri = new URI(config.logout_url);
        return Response.seeOther(uri).build();
    }
};
