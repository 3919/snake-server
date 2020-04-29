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
import java.text.ParseException;
import javax.servlet.ServletContext;

@Path(config.user_manage_url)
public class userManagerServlet
{
    public final class EditStatus{
        public static final int OK     = 0;
        public static final int FAILED = 1;
        public static final int IDLE= -1;
    }
    @Context
    private HttpServletRequest request;
    
	@Context
	private HttpServletResponse response;
    
    @Inject
    private systemCore sc;
    
    public static String byteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }
    public static byte[] hexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    void editSetAttributes(int status, userDescriptor user, userDescriptor edited_user) throws Exception
    {
        request.setAttribute("status",       status);
        request.setAttribute("current_user", user);
        request.setAttribute("edited_user",  edited_user);
        request.setAttribute("users",        getAllUsers());
    }

    ArrayList<userDescriptor> getAllUsers() throws Exception
    {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select * from Users");
        ResultSet res = stmt.executeQuery();
        ArrayList<userDescriptor> users = new ArrayList<userDescriptor>();  
        while(res.next())
        {
            InputStream input = res.getBinaryStream("rfid");
            byte[] rfid = new byte[64];
            input.read(rfid);

            HttpSession session = request.getSession(true);
            int id = res.getInt(1);
            String login= res.getString(2);
            int user_privilege = res.getInt(4);
            int pin = res.getInt(5);
            String user_name = res.getString(6);
            String user_surname = res.getString(7);
            String user_nick = res.getString(8);
            String account_expire_time = res.getString(9);
            userDescriptor u = new userDescriptor(id,
                                                  login,
                                                  user_privilege,
                                                  pin,
                                                  user_name,
                                                  user_surname,
                                                  user_nick,
                                                  account_expire_time,
                                                  rfid);
            users.add(u);
        }
        return users;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public void renderUserManagerPage()throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        if(u.getprivilege() != privilege.ADMIN)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        editSetAttributes(EditStatus.IDLE, u, new userDescriptor());
        request.getRequestDispatcher(config.edit_page)
               .forward(request, response);
    }

    // add/edit user based on delivered data
    // when id is not set eq -1, append new user
    // otherwise try to update date for requested id
    @POST
    public void addUser(
		@FormParam("userid") int id,
		@FormParam("login") String login,
		@FormParam("password") String password,
		@FormParam("privilege") int priv,
		@FormParam("pin") int pin,
		@FormParam("name") String name,
		@FormParam("surname") String surname,
		@FormParam("nick") String nick,
		@FormParam("expire") String expire,
		@FormParam("rfid") String rfid) throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        if(u.getprivilege() != privilege.ADMIN)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }

        ServletContext ueServlet= request.getServletContext().getContext(config.getUserEditUrl());
        // validate required fileds
        if(login.length() == 0 || priv< 0 ||  priv >2 || pin < 1000)
        {
            editSetAttributes(EditStatus.FAILED,u, new userDescriptor());
            ueServlet.getRequestDispatcher(config.edit_page)
                   .forward(request, response);
            return;
        }

        SimpleDateFormat formatter = 
            new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = formatter.parse(expire);
        } catch (ParseException e) {
            editSetAttributes(EditStatus.FAILED, u, new userDescriptor());
            ueServlet.getRequestDispatcher(config.edit_page)
                   .forward(request, response);
           return;
        }
        // end of validation

        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt;
        byte [] raw_rfid;
        raw_rfid = new byte[8];//hexStringToByteArray(rfid);
        if( id == -1)
        {
            if(password.length() == 0)
            {
                editSetAttributes(EditStatus.FAILED,u,  new userDescriptor());
                ueServlet.getRequestDispatcher(config.edit_page)
                       .forward(request, response);
                    return;
            }
            String pass_hash = sha256.toHexString(sha256.getSHA(password)); 
            stmt = conn.prepareStatement("INSERT INTO Users(login, pass_hash, privilege, pin, user_name, user_surname, user_nick, valid_till, rfid) VALUES(?,?,?,?,?,?,?,?,?)");
            stmt.setString(1, login);
            stmt.setString(2, pass_hash);
            stmt.setInt(3, priv);
            stmt.setInt(4, pin);
            stmt.setString(5, name);
            stmt.setString(6, surname);
            stmt.setString(7, nick);
            stmt.setString(8, expire);
            InputStream input= new ByteArrayInputStream(raw_rfid);
            stmt.setBinaryStream(9, input);
        }
        else // validate data, than update user
        {
            stmt = conn.prepareStatement("UPDATE Users SET login=?, privilege=?, pin=?, user_name=?,user_surname=?, user_nick=?, valid_till=?, rfid=? WHERE id=?");
            stmt.setString(1, login);
            stmt.setInt(2, priv);
            stmt.setInt(3, pin);
            stmt.setString(4, name);
            stmt.setString(5, surname);
            stmt.setString(6, nick);
            stmt.setString(7, expire);
            InputStream input= new ByteArrayInputStream(raw_rfid);
            stmt.setBinaryStream(8, input);
            stmt.setInt(9, id);
        }

        int rowsUpdated = stmt.executeUpdate();
        if(rowsUpdated == 0)
        {
            editSetAttributes(EditStatus.FAILED,u, new userDescriptor());
            ueServlet.getRequestDispatcher(config.edit_page)
                   .forward(request, response);
           return;
        }
        
        editSetAttributes(EditStatus.OK, u, new userDescriptor());
        ueServlet.getRequestDispatcher(config.edit_page)
               .forward(request, response);
    }

    //this method returns requested user
    //to fill form on the page
    @GET
    @Path(config.user_edit_url)
    public void editUser(@PathParam("id") String id)throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        if(u.getprivilege() != privilege.ADMIN)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        ServletContext ueServlet= request.getServletContext().getContext(config.getUserEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select * from Users where id=?");
        stmt.setString(1, id);
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
           editSetAttributes(EditStatus.FAILED,u, new userDescriptor());
           ueServlet.getRequestDispatcher(config.edit_page)
                  .forward(request, response);
           return;
        }

        InputStream input = res.getBinaryStream("rfid");
        byte[] rfid = new byte[20];
        input.read(rfid);

        String login= res.getString(2);
        int user_privilege = res.getInt(4);
        int pin = res.getInt(5);
        String user_name = res.getString(6);
        String user_surname = res.getString(7);
        String user_nick = res.getString(8); 
        String account_expire_time = res.getString(9);
        userDescriptor edited_user = new userDescriptor(Integer.parseInt(id),
                                                   login, 
                                                   user_privilege,
                                                   pin,
                                                   user_name, 
                                                   user_surname, 
                                                   user_nick,
                                                   account_expire_time,
                                                   rfid);

        editSetAttributes(EditStatus.OK, u, edited_user);
        ueServlet.getRequestDispatcher(config.edit_page)
               .forward(request, response);
    }

    @GET
    @Path(config.user_remove_url)
    public void removeUser(@PathParam("id") String id)throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        if(u.getprivilege() != privilege.ADMIN)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        ServletContext ueServlet= request.getServletContext().getContext(config.getUserEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("delete from Users where id=?");
        stmt.setString(1, id);
        
        int rowsUpdated = stmt.executeUpdate();
        if(rowsUpdated == 0)
        {
            editSetAttributes(EditStatus.FAILED, u, new userDescriptor());
            response.sendRedirect(config.getUserEditUrl());
            return;
        }

        editSetAttributes(EditStatus.OK, u, new userDescriptor());
        response.sendRedirect(config.getUserEditUrl());
        return;
    }

};
