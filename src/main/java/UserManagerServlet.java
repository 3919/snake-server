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
import java.util.logging.*;

@Path(Config.user_manage_url)
public class UserManagerServlet
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
    private SystemCore sc;
    

    void editSetAttributes(int status, UserDescriptor user, UserDescriptor edited_user) throws Exception
    {
        request.setAttribute("status",       status);
        request.setAttribute("current_user", user);
        request.setAttribute("edited_user",  edited_user);
        request.setAttribute("users",        getAllUsers());
    }

    public static ArrayList<UserDescriptor> getAllUsers() throws Exception
    {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select *, OCTET_LENGTH(rfid) from Users");
        ResultSet res = stmt.executeQuery();
        ArrayList<UserDescriptor> users = new ArrayList<UserDescriptor>();  
        while(res.next())
        {
            InputStream input = res.getBinaryStream("rfid");
            int rfid_size = res.getInt(11);
            byte[] rfid = new byte[rfid_size];
            input.read(rfid);

            int id = res.getInt(1);
            String login= res.getString(2);
            int user_Privilege = res.getInt(4);
            int pin = res.getInt(5);
            String user_name = res.getString(6);
            String user_surname = res.getString(7);
            String user_nick = res.getString(8);
            String account_expire_time = res.getString(9);
            UserDescriptor u = new UserDescriptor(id,
                                                  login,
                                                  user_Privilege,
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
            response.sendRedirect(Config.getLoginUrl());
            return;
        }
        UserDescriptor u =(UserDescriptor)session.getAttribute("user_info");
        if(u.getPrivilege() != Privilege.ADMIN)
        {
            response.sendRedirect(Config.getLoginUrl());
            return;
        }
        editSetAttributes(EditStatus.IDLE, u, new UserDescriptor());
        request.getRequestDispatcher(Config.edit_user_page)
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
		@FormParam("Privilege") int priv,
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
            response.sendRedirect(Config.getLoginUrl());
            return;
        }
        UserDescriptor u =(UserDescriptor)session.getAttribute("user_info");
        if(u.getPrivilege() != Privilege.ADMIN)
        {
            response.sendRedirect(Config.getLoginUrl());
            return;
        }

        ServletContext ueServlet= request.getServletContext().getContext(Config.getUserEditUrl());
        // validate required fileds
        if(login.length() == 0 || priv< 0 ||  priv >2 || pin < 1000)
        {
            editSetAttributes(EditStatus.FAILED,u, new UserDescriptor());
            ueServlet.getRequestDispatcher(Config.edit_user_page)
                   .forward(request, response);
            return;
        }

        SimpleDateFormat formatter = 
            new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = formatter.parse(expire);
        } catch (ParseException e) {
            editSetAttributes(EditStatus.FAILED, u, new UserDescriptor());
            ueServlet.getRequestDispatcher(Config.edit_user_page)
                   .forward(request, response);
           return;
        }
        // end of validation

        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt;
        byte [] raw_rfid = Sha256.toByteArray(rfid);
        if( id == -1)
        {
            if(password.length() == 0)
            {
                editSetAttributes(EditStatus.FAILED,u,  new UserDescriptor());
                ueServlet.getRequestDispatcher(Config.edit_user_page)
                       .forward(request, response);
                    return;
            }
            String pass_hash = Sha256.toHexString(Sha256.getSHA(password)); 
            stmt = conn.prepareStatement("INSERT INTO Users(login, pass_hash, Privilege, pin, user_name, user_surname, user_nick, valid_till, rfid) VALUES(?,?,?,?,?,?,?,?,?)");
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
            stmt = conn.prepareStatement("UPDATE Users SET login=?, Privilege=?, pin=?, user_name=?,user_surname=?, user_nick=?, valid_till=?, rfid=? WHERE id=?");
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
        try{
            int rowsUpdated = stmt.executeUpdate();
            if(rowsUpdated == 0)
            {
                editSetAttributes(EditStatus.FAILED,u, new UserDescriptor());
                ueServlet.getRequestDispatcher(Config.edit_user_page)
                       .forward(request, response);
               return;
            }
            
            sc.log(Level.INFO, "User succefully added/updated. Login {0}",new String[] {login});
            editSetAttributes(EditStatus.OK, u, new UserDescriptor());
            ueServlet.getRequestDispatcher(Config.edit_user_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            System.out.print(e);
            editSetAttributes(EditStatus.FAILED,u, new UserDescriptor());
            ueServlet.getRequestDispatcher(Config.edit_user_page)
                   .forward(request, response);
        }
    }

    //this method returns requested user
    //to fill form on the page
    @GET
    @Path(Config.edit_url)
    public void editUser(@PathParam("id") String id)throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(Config.getLoginUrl());
            return;
        }
        UserDescriptor u =(UserDescriptor)session.getAttribute("user_info");
        if(u.getPrivilege() != Privilege.ADMIN)
        {
            response.sendRedirect(Config.getLoginUrl());
            return;
        }
        ServletContext ueServlet= request.getServletContext().getContext(Config.getUserEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select *, OCTET_LENGTH(rfid) from Users where id=?");
        stmt.setString(1, id);
        
        try{
            ResultSet res = stmt.executeQuery();
            if(!res.next())
            {
               editSetAttributes(EditStatus.FAILED,u, new UserDescriptor());
               ueServlet.getRequestDispatcher(Config.edit_user_page)
                      .forward(request, response);
               return;
            }

            InputStream input = res.getBinaryStream("rfid");
            int rfid_size = res.getInt(11);
            byte[] rfid = new byte[rfid_size];
            input.read(rfid);

            String login= res.getString(2);
            int user_Privilege = res.getInt(4);
            int pin = res.getInt(5);
            String user_name = res.getString(6);
            String user_surname = res.getString(7);
            String user_nick = res.getString(8);
            String account_expire_time = res.getString(9);
            UserDescriptor edited_user = new UserDescriptor(Integer.parseInt(id),
                                                       login,
                                                       user_Privilege,
                                                       pin,
                                                       user_name,
                                                       user_surname,
                                                       user_nick,
                                                       account_expire_time,
                                                       rfid);
            editSetAttributes(EditStatus.OK, u, edited_user);
            ueServlet.getRequestDispatcher(Config.edit_user_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED,u, new UserDescriptor());
            ueServlet.getRequestDispatcher(Config.edit_user_page)
                   .forward(request, response);
        }
    }

    @GET
    @Path(Config.remove_url)
    public void removeUser(@PathParam("id") String id)throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(Config.getLoginUrl());
            return;
        }
        UserDescriptor u =(UserDescriptor)session.getAttribute("user_info");
        if(u.getPrivilege() != Privilege.ADMIN)
        {
            response.sendRedirect(Config.getLoginUrl());
            return;
        }
        ServletContext ueServlet= request.getServletContext().getContext(Config.getUserEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("delete from Users where id=?");
        stmt.setString(1, id);
        try
        { 
            int rowsUpdated = stmt.executeUpdate();
            if(rowsUpdated == 0)
            {
                editSetAttributes(EditStatus.FAILED, u, new UserDescriptor());
                response.sendRedirect(Config.getUserEditUrl());
                return;
            }
            sc.log(Level.INFO, "User succefully removed, User if {0}",new String[] {id});

            editSetAttributes(EditStatus.OK, u, new UserDescriptor());
            ueServlet.getRequestDispatcher(Config.edit_user_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            response.sendRedirect(Config.getUserEditUrl());
            return;
        }
        return;
    }

};
