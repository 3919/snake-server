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

@Path(Config.device_manage_url)
public class DeviceManager
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
    

    void editSetAttributes(int status, Device edited_dev) throws Exception
    {
        request.setAttribute("status",         status);
        request.setAttribute("edited_device",  edited_dev);
        request.setAttribute("sensors",        sc.getDevices());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public void renderDeviceManagerPage()throws Exception
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
        editSetAttributes(EditStatus.IDLE, new Device());
        request.getRequestDispatcher(Config.edit_device_page)
               .forward(request, response);
    }

    // add/edit device on delivered data
    // when id eq -1, append new device
    // otherwise try to update date for requested id
    @POST
    public void addDevice(
        @FormParam("devid") int id,
        @FormParam("name") String name,
        @FormParam("type") int type,
        @FormParam("token") String token) throws Exception
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
        ServletContext servlet= request.getServletContext().getContext(Config.getDeviceEditUrl());
        if(token.length() != 20)
        {
            editSetAttributes(EditStatus.FAILED,new Device());
            servlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
            return;
        }
        if(type <0 || type > 2)
        {
            editSetAttributes(EditStatus.FAILED,new Device());
            servlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
            return;
        }

        // validate required fileds
        if(name.length() == 0)
        {
            editSetAttributes(EditStatus.FAILED, new Device());
            servlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
            return;
        }
        // end of validation
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt;
        if(id == -1)
        {
            stmt = conn.prepareStatement("INSERT INTO Devices (name, type, token) VALUES(?,?,?)");
        }
        else 
        {
            stmt = conn.prepareStatement("UPDATE Devices SET name=?, type=?, token=? WHERE id=?");
            stmt.setInt(4, id);
        }
        stmt.setString(1,name);
        stmt.setInt(2,type);
        stmt.setString(3,token);
        try{
            int rowsUpdated = stmt.executeUpdate();
            if(rowsUpdated == 0)
            {
                editSetAttributes(EditStatus.FAILED,new Device());
                servlet.getRequestDispatcher(Config.edit_device_page)
                       .forward(request, response);
               return;
            }
            sc.loadKnownSensors();
            editSetAttributes(EditStatus.OK, new Device());
            servlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED, new Device());
            servlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
        }
    }

    //this method returns requested user
    //to fill form on the page
    @GET
    @Path(Config.edit_url)
    public void editDevice(@PathParam("id") String id)throws Exception
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
        ServletContext servlet= request.getServletContext().getContext(Config.getDeviceEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select * from Devices where id=?");
        stmt.setString(1, id);
        
        try{
            ResultSet res = stmt.executeQuery();
            if(!res.next())
            {
               editSetAttributes(EditStatus.FAILED, new Device());
               servlet.getRequestDispatcher(Config.edit_device_page)
                      .forward(request, response);
               return;
            }
            String name= res.getString(2);
            int type= res.getInt(3);
            String token= res.getString(4);

            Device edited_device = new Device(Integer.parseInt(id),
                                                                 name,
                                                                 type,
                                                                 token);
            editSetAttributes(EditStatus.OK, edited_device);
            servlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED,new Device());
            servlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
        }
    }

    @GET
    @Path(Config.remove_url)
    public void removeDevice(@PathParam("id") String id)throws Exception
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
        ServletContext ueServlet= request.getServletContext().getContext(Config.getDeviceEditUrl());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("delete from Devices where id=?");
        stmt.setString(1, id);
        try
        {
            int rowsUpdated = stmt.executeUpdate();
            if(rowsUpdated == 0)
            {
                editSetAttributes(EditStatus.FAILED, new Device());
                response.sendRedirect(Config.getDeviceEditUrl());
                return;
            }
            sc.loadKnownSensors();
            editSetAttributes(EditStatus.OK, new Device());
            ueServlet.getRequestDispatcher(Config.edit_device_page)
                   .forward(request, response);
        }catch(Exception e)
        {
            editSetAttributes(EditStatus.FAILED, new Device());
            response.sendRedirect(Config.getDeviceEditUrl());
            return;
        }
        return;
    }

};
