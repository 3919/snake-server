package rest;
import java.util.Date;

public class UserDescriptor
{
   private String userlogin="";

   private int id=-1;
   private int Privilege=0;
   private int pin;
      
   private String email="";
   private String name="";
   
   private String surname="";
   
   private String nick = "";
   private String accountexpire;

   private Date created;
   byte [] rfid= new byte[1];
   
   private int activeSessions=0;
   UserDescriptor()
   {
    created= new Date();
   }

   UserDescriptor(int e_id, 
                  String login, 
                  int priv,
                  int u_pin,
                  String u_name, 
                  String u_surname, 
                  String u_nick, 
                  String u_accountexpire,
                  String u_email, 
                  byte[] u_rfid)
   {
    id = e_id;
    userlogin = login;
    Privilege = priv;
    pin = u_pin;
    name = u_name;
    surname = u_surname;
    nick =u_nick;
    accountexpire = u_accountexpire;
    email=u_email;
    rfid = u_rfid;
    created= new Date();
    activeSessions++;
   }
   
   static boolean isEmailValid(String email) {
     String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
     return email.matches(regex);
   }

   public int getid()
   {
    return id;
   }
   public String getuserlogin()
   {
    return userlogin;
   }
   public int getPrivilege()
   {
    return Privilege;
   }
   public int getpin()
   {
    return pin;
   }
   public String getemail()
   {
     return email;
   }
   public String getname()
   {
     return name;
   }
   public String getsurname()
   {
     return surname;
   }
   public String getnick()
   {
     return nick;
   }
   public String getaccountexpire()
   {
     return accountexpire;
   }
   public String getrfid()
   {
     return Sha256.toHexString(rfid);
   }
   public Date getcreated()
   {
     return created;
   }
   int newSessionCreated()
   {
    activeSessions++;
    return activeSessions;
   }

   int sessionDestroyed()
   {
    activeSessions--;
    return activeSessions;
   }

   public void setid(int id)
   {
    id = id;
   }
   public void setuserlogin(String ul)
   {
    userlogin = ul;
   }
   public void setPrivilege(int priv)
   {
    Privilege = priv;
   }
   public void setemail(String e)
   {
     email= e;
   }
   public void setname(String n)
   {
     name = n;
   }
   public void setsurname(String s)
   {
     surname= s;
   }
   public void setnink(String n)
   {
     nick = n;
   }
}
