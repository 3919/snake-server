package rest;
import java.sql.*;
import java.io.*; 

import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.servlet.http.*;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;
import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParseException;


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
        requestSetAttributes(PassStatus.PASS_IDLE, u);
        request.getRequestDispatcher(config.snake_page)
               .forward(request, response);
    }

    void requestSetAttributes(int pass_stat, userDescriptor u)
    {
        laboratoryState l = sc.getLabState();
        request.setAttribute("u_info",       u);
        request.setAttribute("active_users", l.loggedUsers);
        request.setAttribute("temp_in",      l.temperature);
        request.setAttribute("humidity_out", l.humidity);
        request.setAttribute("response_msg", pass_stat);
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
        PreparedStatement stmt = conn.prepareStatement("select * from Users where login=? and pass_hash=?");
        String pass_hash = sha256.toHexString(sha256.getSHA(o_password)); 
        String login = u.getuserlogin(); 
        stmt.setString(1, login);
        stmt.setString(2, pass_hash);
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
            requestSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }
        // new passwords match?
        if(!n_password.equals(r_password))  
        {
            requestSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
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
            requestSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }

        requestSetAttributes(PassStatus.PASS_CHANGE_OK, u);
        request.getRequestDispatcher(config.snake_page)
               .forward(request, response);
        return;
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
            byte[] rfid = new byte[1024];
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
                                                  rfid,
                                                  new Date());
            users.add(u);
        }
        return users;
    }

    @GET
    @Path(config.user_manage_url)
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
        request.setAttribute("u_info",       u);
        request.setAttribute("users",   getAllUsers());
        request.getRequestDispatcher(config.edit_page)
               .forward(request, response);
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    // add/edit user based on delivered data
    // when id is not set eq -1, append new user
    // otherwise try to update date for requested id
    @POST
    @Path(config.user_url)
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
		@FormParam("expire") String rfid) throws Exception
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
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt;
        String query = "";
        byte [] raw_rfid;
        // validate data, than add user
        if( id == -1)
        {
            if(login.length() == 0 || password.length() == 0 || priv< 0 ||  priv >2 || pin >1000 && name.length() == 0 || surname.length() == 0 || nick.length() == 0)
            {
                request.setAttribute("status",       1);
                request.setAttribute("u_info",       u);
                request.setAttribute("users",   getAllUsers());
                request.getRequestDispatcher(config.edit_page)
                       .forward(request, response);
                return;

            }
            SimpleDateFormat formatter = 
                new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = formatter.parse(expire);
            } catch (ParseException e) {
                request.setAttribute("status",       1);
                request.setAttribute("u_info",       u);
                request.setAttribute("users",   getAllUsers());
                request.getRequestDispatcher(config.edit_page)
                       .forward(request, response);
                return;
            }
            String pass_hash = sha256.toHexString(sha256.getSHA(password)); 
            raw_rfid = hexStringToByteArray(rfid);
            query = "INSERT INTO Users(login, pass_hash, privilage, pin, user_name, user_surname, user_nick, valid_till, rfid) VALUES(?,?,?,?,?,?,?,?,?)";
        stmt = conn.prepareStatement(query);
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
        }else // validate data, than update user
        {
            stmt = conn.prepareStatement(query);

        }
        ResultSet res = stmt.executeQuery();
    }
    // this method returns requested user to fill form
    @POST
    @Path(config.user_edit_url)
    public void editUser()throws Exception
    {

    }

    @GET
    @Path(config.user_remove_url)
    public void removeUser()throws Exception
    {

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
