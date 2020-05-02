package rest;
import java.sql.*;
import com.fazecast.jSerialComm.*;

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
import java.util.logging.*;

@ApplicationScoped
@Path("")
public class systemCore
{

    
    private final static Logger logger= Logger.getLogger("SnakeLogger");  
    private static FileHandler fh;  
    @Context
    private HttpServletRequest request;
    
    private laboratoryState state = new laboratoryState();
    private Connection conn;
    
    public systemCore()
    {
        try{
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
            fh = new FileHandler(config.log_name , true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void log(Level l, String msg, String[] params)
    {
        try {  
            logger.log(l, msg, params); 
            fh.flush();
        } catch (Exception e) {  
            e.printStackTrace();  
        }
    }

    public void unlockLaboratory()
    {
        SerialPort locker = SerialPort.getCommPort("/dev/ttyUSB0");
        boolean openedSuccessfully =locker.openPort(0);
		if (!openedSuccessfully)
			return;
        locker.setBaudRate(115200);
        byte[] open_msg = {0x2,0x1};
        locker.writeBytes(open_msg, 2);
        locker.closePort();
        
        state.labOpen=true;
    }

    public void lockLaboratory()
    {
        SerialPort locker = SerialPort.getCommPort("/dev/ttyUSB0");
        boolean openedSuccessfully =locker.openPort(0);
		if (!openedSuccessfully)
			return;
        locker.setBaudRate(115200);
        byte[] open_msg = {0x2,0x2};
        locker.writeBytes(open_msg, 2);
        locker.closePort();
        state.labOpen=false;
    }

    public laboratoryState getLabState()
    {
        return state;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(config.sensor_update_url)
    public Response handleSensor(@Valid sensor s)
    {
        sensor f_s = state.getSensorByName(s.sensor_name);
        if(f_s == null)
        {
            state.sensors.add(s);
        }else
        {
            f_s.value = s.value;
        }
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
    }

    public void removeUser(userDescriptor u)
    {
        userDescriptor user = findUser(u); 
        int leftSessions = user.sessionDestroyed();
        if(leftSessions > 0)
            return;

        state.loggedUsers.remove(user);
    } 
}

