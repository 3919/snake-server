package rest;
import java.sql.*;
import com.fazecast.jSerialComm.*;

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
import javax.mail.*;  
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@ApplicationScoped
@Path("")
public class SystemCore
{
    public class CleanersDescriptor
    {
        public UserDescriptor[] staff = {new UserDescriptor(), new UserDescriptor()};
        public int [] lastDraw = {-1, -1};
        public Date nextDrawDate = new Date();
        boolean userValid(int id)
        {
            return id != lastDraw[0] && id != lastDraw[1]; 
        }
    };

    private final static Logger logger = Logger.getLogger("SnakeLogger");  
    private static FileHandler fh;
    @Context
    private HttpServletRequest request;
    
    private LaboratoryState state = new LaboratoryState();
    private Connection conn;

    private CleanersDescriptor cleaners= new CleanersDescriptor();
    private Slack slack;
    private String token;
    private ArrayList<Device> sensors;
    public SystemCore()
    {
        try{
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
            fh = new FileHandler(Config.log_name , true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);
            slack = Slack.getInstance();
            token = System.getenv("SLACK_TOKEN_BOT");
            loadKnownSensors();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadKnownSensors() throws Exception
    {
        ArrayList<Device> s = new ArrayList<Device>();
        PreparedStatement stmt = conn.prepareStatement("select * from Devices");

        ResultSet res = stmt.executeQuery();
        sensors = new ArrayList<Device>();
        Device d;
        while(res.next())
        {
             new Device();
            int id= res.getInt(1);
            String name= res.getString(2);
            int type = res.getInt(3);
            String token = res.getString(4);
            sensors.add(new Device(id, name, type, token));
        }
    }

    public CleanersDescriptor getCleaners()
    {
        return cleaners;
    }

    public ArrayList<Device> getDevices()
    {
        return sensors;
    }

    void sendEmail(String email, String header, String msg)
    {
        final String username = "skn.mos.snake@gmail.com";
        final String password = "supertajnehaslo";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
       try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("skn.mos.snake@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(email)
            );
            message.setSubject(header);
            message.setText(msg);

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        } 
    }

    public void drawCleaners() throws Exception
    {
        Date current_time = new Date();
        ArrayList<UserDescriptor> users = UserManagerServlet.getAllUsers();
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

    public void unlockLaboratory(String login, String email)
    {
        //SerialPort locker = SerialPort.getCommPort("/dev/ttyUSB0");
        //boolean openedSuccessfully =locker.openPort(0);
        //if (!openedSuccessfully) //  return;
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
        sendEmail(email, "Laboratry unlocked", "Laboratory open");
        log(Level.INFO, "Laboratory unlcked by {0}", new String[]{login});
        state.labOpen=true;
    }

    public void lockLaboratory(String login, String email)
    {
        //SerialPort locker = SerialPort.getCommPort("/dev/ttyUSB0");
        //boolean openedSuccessfully =locker.openPort(0);
        //if (!openedSuccessfully)
        //  return;
        //locker.setBaudRate(115200);
        //byte[] open_msg = {0x2,0x2};
        //locker.writeBytes(open_msg, 2);
        //locker.closePort();
        try{
            for(Sensor s : state.activeSensors)
            {
                if(s.type == Sensor.OPEN_WINDOW_DETECTOR && s.value == 1.0) // if window open
                {
                    ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                                                  .channel("lab_info") // Channel ID
                                                  .text(":bomb: User @" + login + " locked laboratory. However window left opened"));
                    sendEmail(email, "Laboratory alert", "Window left opened");
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
        sendEmail(email, "Laboratry locked", "Laboratory locked");
        this.log(Level.INFO, "Laboratory locked by {0}",new String[]{login});
        state.labOpen=false;
    }

    public LaboratoryState getLabState()
    {
        return state;
    }

    Device getSensorByToken(String token)
    {
        for(Device d : sensors)
        {
            if(d.token.equals(token) == true)
                return d;
        } 
        return null;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(Config.Sensor_url)
    public Response handleSensor(DevDescriptor s)
    {
        Device f_d = getSensorByToken(s.token);
        if(f_d == null)
        {
            return Response.status(Response.Status.NOT_FOUND).entity("").build();
        }

        Sensor f_s = state.getSensorByName(f_d.name);
        if(f_s == null)
        {
            f_s = new Sensor();
            f_s.sensor_name = f_d.name;
            f_s.type = f_d.type;
            f_s.value = s.value;
            state.activeSensors.add(f_s);
        }else
        {
            f_s.value = s.value;
        }
        return Response.ok("").build();
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(Config.ajax_Sensors_path)
    public ArrayList<Sensor> getSensors()
    {
        return state.activeSensors;
    }

    UserDescriptor findUser(int id)
    {
        for(int i =0; i < state.loggedUsers.size(); i++)
        {
            UserDescriptor u_tmp = state.loggedUsers.get(i);
            if(u_tmp.getid() == id)
            {
                return u_tmp;
            }
        }
        return null;
    }
    
    UserDescriptor findUser(UserDescriptor u)
    {
        for(int i =0; i < state.loggedUsers.size(); i++)
        {
            UserDescriptor u_tmp = state.loggedUsers.get(i);
            if(u_tmp.getid() == u.getid())
            {
                return u_tmp;
            }
        }
        return null;
    }

    public void addUser(UserDescriptor u)
    {
        // if user allready logged, only increment active user sessions
        UserDescriptor user = findUser(u); 
        if (user != null)
        {
           user.newSessionCreated();
           return;
        }

        state.loggedUsers.add(u);
    }

    public void removeUser(UserDescriptor u)
    {
        UserDescriptor user = findUser(u); 
        int leftSessions = user.sessionDestroyed();
        if(leftSessions > 0)
            return;

        state.loggedUsers.remove(user);
    } 
}

