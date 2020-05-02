package rest;
import java.util.ArrayList;
public class laboratoryState 
{
    public ArrayList<sensor> sensors =  new ArrayList<sensor>();
    public ArrayList<userDescriptor> loggedUsers = new ArrayList<>();
    boolean labOpen = false;

    sensor getSensorByName(String name)
    {
        for(sensor s : sensors)
        {
            if(s.sensor_name.equals(name) == true)
                return s;
        } 
        return null;
    }
};
