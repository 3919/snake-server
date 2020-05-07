package rest;
import javax.validation.*;
import javax.validation.constraints.*;

public class sensor
{
    public final class msg_type{
        public static final int TEMPERATURE= 0;
        public static final int HUMIDITY= 1;
        public static final int TBA= -1;
    }

    @NotNull
    public String sensor_name;
    @NotNull
    @Max(2)
    public  int type; 
    @NotNull
    public double value;
};
