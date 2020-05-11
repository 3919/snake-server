package rest;
import java.util.ArrayList;
public class LaboratoryState 
{
    public ArrayList<Sensor> Sensors =  new ArrayList<Sensor>();
    public ArrayList<UserDescriptor> loggedUsers = new ArrayList<>();
    boolean labOpen = false;

    Sensor getSensorByName(String name)
    {
        for(Sensor s : Sensors)
        {
            if(s.Sensor_name.equals(name) == true)
                return s;
        } 
        return null;
    }
};
