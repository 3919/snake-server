import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Cookie;
import java.util.Random;
import java.lang.Thread;
import javax.ws.rs.Consumes;
import javax.validation.*;
import javax.validation.constraints.*;
import javax.ws.rs.core.Response;

public class sensor{

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

    String server_url = "http://localhost:8080/rest";
    Response resp;
    public static double randFloat(double min, double max) {
        Random rand = new Random();

        return rand.nextFloat() * (max - min) + min;
    }

    public static void main(String[] args) throws Exception
    {
        sensor temperature = new sensor();
        sensor humidity = new sensor();
        boolean status = temperature.authenticate();
        
        if(status == false)
        {
            System.out.print("authentication failed");
            return;
        }

        status = humidity.authenticate();
        if(status == false)
        {
            System.out.print("authentication failed");
            return;
        }

        double tmp = 36.6;
        double hum = 23.0;
        sensor_msg m_tmp = new sensor_msg();
        sensor_msg h_tmp = new sensor_msg();
        m_tmp.type = sensor_msg.msg_type.TEMPERATURE;
        h_tmp.type = sensor_msg.msg_type.HUMIDITY;

        while(true)
        {
            // simualte sensor
            tmp= randFloat(-10, 50);
            hum= randFloat( 20, 60);
            m_tmp.value = tmp;
            h_tmp.value = tmp;
            temperature.updateSensor(m_tmp);
            Thread.sleep(1000);
            humidity.updateSensor(h_tmp);
            Thread.sleep(1000);
        }
    }

    private boolean authenticate() 
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server_url).path("sensor/auth");

        Form form = new Form();
        form.param("dev_name", "TMP36GT9Z");
        form.param("password", "kotmaale");

        resp =
        target.request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        int status =resp.getStatus();
        client.close();

        return status == 200;
    }

    private void updateSensor(sensor_msg msg) 
    {   
        final Client client = ClientBuilder.newClient();
        try {
            final WebTarget target = client.target(server_url);

            final Cookie sessionId = resp.getCookies().get("JSESSIONID");
            target.path("sensor")
                  .request(MediaType.APPLICATION_XML)
                  .cookie(sessionId)
                  .post(Entity.entity(msg, MediaType.APPLICATION_XML));
        } finally {
            client.close();
        }
    }
}
