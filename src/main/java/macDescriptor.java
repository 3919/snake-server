package rest;
import java.util.Date;

public class macDescriptor
{
   private String userlogin="";

   private int id=-1;
   private String mac_addr="";
   
   macDescriptor(){}

   macDescriptor(int e_id, 
                  String login, 
                  String mac)
   {
    id = e_id;
    userlogin = login;
    mac_addr = mac;
   }

   public int getid()
   {
    return id;
   }
   public String getuserlogin()
   {
    return userlogin;
   }

   public String getmac()
   {
    return mac_addr;
   }

   public void setid(int id)
   {
    id = id;
   }
   public void setuserlogin(String ul)
   {
    userlogin = ul;
   }
   public void setmac(String m)
   {
     mac_addr= m;
   }
}
