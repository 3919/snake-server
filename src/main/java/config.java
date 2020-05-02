package rest;

public class config
{
    public static final String war_url ="/rest"; 
    public static final String login_url = "/login"; 
    public static final String logout_url = "/logout";
    public static final String app_url = "/app";

    public static final String user_manage_url = "/users";
    public static final String mac_manage_url = "/mac";

    public static final String edit_url = "/{id}";
    public static final String remove_url = "/remove/{id}";

    public static final String sensor_update_url = "/sensor";
 
    public static final String lab_unlock_url = "/unlock";
    public static final String lab_lock_url = "/lock";
    
    public static final String download_logs_url= "/logs/download";
    
    // web resources
    public static final String login_page = "/login.html";
    public static final String snake_page = "/snake_main.jsp";
    public static final String edit_user_page = "/edit_user.jsp";
    public static final String edit_mac_page = "/edit_mac.jsp";

    
    public static final String log_name = "snakelogs";
    public static String getLoginUrl(){
        return war_url + login_url;
    }

    public static String getLogoutUrl(){
        return war_url + logout_url;
    }

    public static String getAppUrl(){
        return war_url + app_url;
    }

    public static String getUserEditUrl(){
        return war_url + user_manage_url;
    }

    public static String getMacEditUrl(){
        return war_url + mac_manage_url;
    }
};
