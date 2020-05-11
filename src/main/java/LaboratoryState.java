package rest;
import java.util.ArrayList;
public class LaboratoryState 
{
    public ArrayList<Sensor> activeSensors =  new ArrayList<Sensor>();
    public ArrayList<UserDescriptor> loggedUsers = new ArrayList<>();
    boolean labOpen = false;

    public Sensor getSensorByName(String name)
    {
        for(Sensor s : activeSensors)
        {
            if(s.sensor_name.equals(name) == true)
                return s;
        } 
        return null;
    }
};
