package rest;
import java.sql.*;

import java.util.logging.Logger;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.servlet.http.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.*;
import javax.validation.constraints.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Consumes;

@ApplicationScoped
@Path("")
public class systemCore
{
    static class sensor_msg
    {
        public static enum msg_type 
        {
            TEMPERATURE,
            HUMIDITY
        };
        @NotNull
        @Max(2)
        public msg_type type; 
        @NotNull
        public double value;
    };

    @Context
    private HttpServletRequest request;
    
    private laboratoryState state = new laboratoryState();
    private Connection conn;

    public systemCore()
    {
        try{
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
    
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @POST
    @Path(config.sensor_auth_url)
    public Response authenticate (
		@FormParam("dev_name") String device,
		@FormParam("password") String password)throws Exception
    {
        PreparedStatement stmt = conn.prepareStatement("select * from Devices where device_name=? and pass_hash=?");
        String pass_hash = sha256.toHexString(sha256.getSHA(password)); 
        stmt.setString(1, device);
        stmt.setString(2, pass_hash);
        //execute query
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
           return Response.status(Response.Status.FORBIDDEN).entity("").build();
        }

        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(0); // never timeout
        int dev_privilege = res.getInt(4);

        session.setAttribute("dev_info",new devDescriptor(device, dev_privilege));
        return Response.ok("").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(config.sensor_update_url)
    public Response handleSensor(@Valid sensor_msg msg)
    {
        switch(msg.type)
        {
            case TEMPERATURE:
               state.temperature = msg.value; 
            break;

            case HUMIDITY:
               state.humidity = msg.value; 
            break;
        };
        return Response.ok("").build();
    }
    
    userDescriptor findUser(userDescriptor u)
    {
        for(int i =0; i < state.loggedUsers.size(); i++)
        {
            userDescriptor u_tmp = state.loggedUsers.get(i);
            if(u_tmp.getid() == u.getid())
            {
                return u_tmp;
            }
        }
        return null;
    }

    public void addUser(userDescriptor u)
    {
        // if user allready logged, only increment active user sessions
        userDescriptor user = findUser(u); 
        if (user != null)
        {
           user.newSessionCreated();
           return;
        }

        state.loggedUsers.add(u);
        state.labOpen = true;
        System.out.println("Logged: " + state.loggedUsers.size());
    }

    public void removeUser(userDescriptor u)
    {
        
        userDescriptor user = findUser(u); 
        int leftSessions = user.sessionDestroyed();
        if(leftSessions > 0)
            return;

        state.loggedUsers.remove(user);
        if(state.loggedUsers.size() == 0)
            state.labOpen = false;
        System.out.println("Logged: " + state.loggedUsers.size());
    } 
}

