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

@Path(Config.mac_manage_url)
public class MacManagerServlet
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
    

    void editSetAttributes(int status, MacDescriptor edited_mac) throws Exception
    {
        request.setAttribute("status",       status);
        request.setAttribute("edited_mac",  edited_mac);
        request.setAttribute("macs",        getAllMacs());
    }

    ArrayList<MacDescriptor> getAllMacs() throws Exception
    {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select * from MAC_MAP");
        ResultSet res = stmt.executeQuery();
        ArrayList<MacDescriptor> macs = new ArrayList<MacDescriptor>();  
        while(res.next())
        {
            HttpSession session = request.getSession(true);
            int id = res.getInt(1);
            String login= res.getString(2);
            String mac = res.getString(3);
            MacDescriptor u = new MacDescriptor(id,
                                                login,
                                                mac);
            macs.add(u);
        }
        return macs;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public void renderMacManagerPage()throws Exception
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
        editSetAttributes(EditStatus.IDLE, new MacDescriptor());
        request.getRequestDispatcher(Config.edit_mac_page)
               .forward(request, response);
    }

    // add/edit mac based on delivered data
    // when id is not set eq -1, append new mac
    // otherwise try to update date for requested id
    @POST
    public void addMac(
        @FormParam("userid") int id,
        @FormParam("login") String login,
        @FormParam("mac") String mac) throws Exception
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
        ServletContext servlet= request.getServletContext().getContext(Config.getMacEditUrl());
        
        mac = mac.replace(":","");
        if(mac.length() != 12)
        {
            editSetAttributes(EditStatus.FAILED,new MacDescriptor());
            servlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
            return;
        }

        try
        {
            Long.parseLong(mac, 16);
        }
        catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED, new MacDescriptor());
            servlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
            return;
        }

        // validate required fileds
        if(login.length() == 0)
        {
            editSetAttributes(EditStatus.FAILED, new MacDescriptor());
            servlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
            return;
        }
        // end of validation

        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt;
        if(id == -1)
        {
            stmt = conn.prepareStatement("INSERT INTO MAC_MAP(login, mac) VALUES(?,?)");
        }
        else 
        {
            stmt = conn.prepareStatement("select id from MAC_MAP where login = ?");
            stmt.setString(1, login);
            ResultSet res = stmt.executeQuery();
            if(!res.next())
            {
               editSetAttributes(EditStatus.FAILED, new MacDescriptor());
               servlet.getRequestDispatcher(Config.edit_mac_page)
                      .forward(request, response);
               return;
            }
            stmt = conn.prepareStatement("UPDATE MAC_MAP SET login=?, mac=? WHERE id=?");
            stmt.setInt(3, id);
        }
        stmt.setString(1, login);
        stmt.setString(2, mac);
        try{
            int rowsUpdated = stmt.executeUpdate();
            if(rowsUpdated == 0)
            {
                editSetAttributes(EditStatus.FAILED,new MacDescriptor());
                servlet.getRequestDispatcher(Config.edit_mac_page)
                       .forward(request, response);
               return;
            }
            
            sc.log(Level.INFO, "Mac address succefully added/updated. Login {0}, mac {1}",new String[] {login, mac});
            editSetAttributes(EditStatus.OK, new MacDescriptor());
            servlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED, new MacDescriptor());
            servlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
        }
    }

    //this method returns requested user
    //to fill form on the page
    @GET
    @Path(Config.edit_url)
    public void editMac(@PathParam("id") String id)throws Exception
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
        ServletContext servlet= request.getServletContext().getContext(Config.getMacEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select * from MAC_MAP where id=?");
        stmt.setString(1, id);
        
        try{
            ResultSet res = stmt.executeQuery();
            if(!res.next())
            {
               editSetAttributes(EditStatus.FAILED, new MacDescriptor());
               servlet.getRequestDispatcher(Config.edit_mac_page)
                      .forward(request, response);
               return;
            }
            String login= res.getString(2);
            String mac= res.getString(3);
            MacDescriptor edited_mac = new MacDescriptor(Integer.parseInt(id),
                                                       login,
                                                       mac);
            editSetAttributes(EditStatus.OK, edited_mac);
            servlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED,new MacDescriptor());
            servlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
        }
    }

    @GET
    @Path(Config.remove_url)
    public void removeMac(@PathParam("id") String id)throws Exception
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
        ServletContext ueServlet= request.getServletContext().getContext(Config.getMacEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("delete from MAC_MAP where id=?");
        stmt.setString(1, id);
        try
        {
            int rowsUpdated = stmt.executeUpdate();
            if(rowsUpdated == 0)
            {
                editSetAttributes(EditStatus.FAILED, new MacDescriptor());
                response.sendRedirect(Config.getMacEditUrl());
                return;
            }
            sc.log(Level.INFO, "Mac succefully removed, Mac id {0}",new String[] {id});

            editSetAttributes(EditStatus.OK, new MacDescriptor());
            ueServlet.getRequestDispatcher(Config.edit_mac_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED, new MacDescriptor());
            response.sendRedirect(Config.getMacEditUrl());
            return;
        }
        return;
    }

};
