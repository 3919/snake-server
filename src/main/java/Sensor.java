package rest;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Sensor
{
    public static final int TEMPERATURE= 0;
    public static final int HUMIDITY= 1;
    public static final int OPEN_WINDOW_DETECTOR= 2;
    
    public String Sensor_name;
    public int type; 
    public double value;
};
