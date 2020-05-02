package rest;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class sensor
{
    public final class msg_type{
        public static final int TEMPERATURE= 0;
        public static final int HUMIDITY= 1;
        public static final int TBA= -1;
    }
    
    public String sensor_name;
    public  int type; 
    public double value;
};
