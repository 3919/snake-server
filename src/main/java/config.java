package rest;

public class config
{
    public static final String war_url ="/rest"; 
    public static final String login_url = "/login"; 
    public static final String logout_url = "/logout";
    public static final String app_url = "/app";
    
    // web resources
    public static final String login_page = "login.html";
    public static final String snake_page = "snake_main.jsp";

    public static String getLoginUrl(){
        return war_url + login_url;
    }

    public static String getLogoutUrl(){
        return war_url + logout_url;
    }

    public static String getAppUrl(){
        return war_url + app_url;
    }
};
