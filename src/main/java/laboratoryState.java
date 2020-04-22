package rest;
import java.util.ArrayList;
public class laboratoryState 
{
    public double temperature = 0.0;
    public double humidity = 0.0;
    public ArrayList<userDescriptor> loggedUsers = new ArrayList<>();
    public boolean labOpen = false;
};
