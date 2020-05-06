package rest;
import java.sql.*;
import com.fazecast.jSerialComm.*;

import java.util.logging.Logger;
import java.util.Date;
import javax.ws.rs.*;
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
import java.util.Random;
import java.util.ArrayList;
import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

@ApplicationScoped
@Path("")
public class systemCore
{
    public class cleanersDescriptor
    {
        public userDescriptor[] staff = {new userDescriptor(), new userDescriptor()};
        public int [] lastDraw = {-1, -1};
        public Date nextDrawDate = new Date();
        boolean userValid(int id)
        {
            return id != lastDraw[0] && id != lastDraw[1]; 
        }
    };

    private final static Logger logger= Logger.getLogger("SnakeLogger");  
    private static FileHandler fh;  
    @Context
    private HttpServletRequest request;
    
    private laboratoryState state = new laboratoryState();
    private Connection conn;

    private cleanersDescriptor cleaners= new cleanersDescriptor();
    private Slack slack;
    private String token;

    public systemCore()
    {
        try{
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
            fh = new FileHandler(config.log_name , true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);
            slack = Slack.getInstance();
            token = System.getenv("SLACK_TOKEN_BOT");

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public cleanersDescriptor getCleaners()
    {
        return cleaners;
    }

    public void drawCleaners() throws Exception
    {
        Date current_time = new Date();
        ArrayList<userDescriptor> users = userManagerServlet.getAllUsers();
        if(current_time.compareTo(cleaners.nextDrawDate) < 0 || users.size() == 0)
        {
            return;
        }
        
        Random rand = new Random();
        int u1, u2;
        do{
           u1 = rand.nextInt(users.size());
        }while(!cleaners.userValid(u1));
        cleaners.staff[0] = users.get(u1);
        cleaners.lastDraw[0] = u1;
        
        do{
           u2 = rand.nextInt(users.size());
        }while(!cleaners.userValid(u2));

        cleaners.staff[1] = users.get(u2);
        cleaners.lastDraw[1] = u2;
        cleaners.nextDrawDate.setTime(current_time.getTime() + 7*24*3600*1000);
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

    public void unlockLaboratory(String login)
    {
        //SerialPort locker = SerialPort.getCommPort("/dev/ttyUSB0");
        //boolean openedSuccessfully =locker.openPort(0);
		//if (!openedSuccessfully)
		//	return;
        //locker.setBaudRate(115200);
        //byte[] open_msg = {0x2,0x1};
        //locker.writeBytes(open_msg, 2);
        //locker.closePort();
        try{
            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                                          .channel("lab_info") // Channel ID
                                          .text(":unlock: User " + login + " unlocked laboratory"));
        }catch(Exception e)
        {
            log(Level.INFO, "Slack can't send info to channel",new String[] {});
        }

        log(Level.INFO, "Laboratory unlcked by {0}",new String[] {login});
        state.labOpen=true;
    }

    public void lockLaboratory(String login)
    {
        //SerialPort locker = SerialPort.getCommPort("/dev/ttyUSB0");
        //boolean openedSuccessfully =locker.openPort(0);
		//if (!openedSuccessfully)
		//	return;
        //locker.setBaudRate(115200);
        //byte[] open_msg = {0x2,0x2};
        //locker.writeBytes(open_msg, 2);
        //locker.closePort();
        try{
            for(sensor s : state.sensors)
            {
                if(s.type == sensor.OPEN_WINDOW_DETECTOR && s.value == 1.0) // if window open
                {
                    ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                                                  .channel("lab_info") // Channel ID
                                                  .text(":bomb: User @" + login + " locked laboratory. However window left opened"));
                    this.log(Level.SEVERE, "Laboratory locked. However window left opened", null);
                    break;
                }
            }
            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                                          .channel("lab_info") // Channel ID
                                          .text(":lock: Laboratory locked by " + login));
        }catch(Exception e)
        {
            log(Level.INFO, "Slack can't send info to channel",new String[] {});
        }
        this.log(Level.INFO, "Laboratory locked by {0}",new String[]{login});
        state.labOpen=false;
    }

    public laboratoryState getLabState()
    {
        return state;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(config.sensor_url)
    public Response handleSensor(sensor s)
    {
        sensor f_s = state.getSensorByName(s.sensor_name);
        if(f_s == null)
        {
            state.sensors.add(s);
        }else
        {
            f_s.type = s.type;
            f_s.value = s.value;
        }
        return Response.ok("").build();
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(config.ajax_sensors_path)
    public ArrayList<sensor> getSensors()
    {
        return state.sensors;
    }

    userDescriptor findUser(int id)
    {
        for(int i =0; i < state.loggedUsers.size(); i++)
        {
            userDescriptor u_tmp = state.loggedUsers.get(i);
            if(u_tmp.getid() == id)
            {
                return u_tmp;
            }
        }
        return null;
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

