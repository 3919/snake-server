package rest;
import java.util.Date;

public class userDescriptor
{
   private String userlogin="";

   private int id=-1;
   private int privilege=0;
   private int pin;
      
   private String name="";
   
   private String surname="";
   
   private String nick = "";
   private String accountexpire;

   private Date created;
   byte [] rfid= new byte[1];
   
   private int activeSessions=0;
   userDescriptor()
   {
    created= new Date();
   }

   userDescriptor(int e_id, 
                  String login, 
                  int priv,
                  int u_pin,
                  String u_name, 
                  String u_surname, 
                  String u_nick, 
                  String u_accountexpire,
                  byte[] u_rfid)
   {
    id = e_id;
    userlogin = login;
    privilege = priv;
    pin = u_pin;
    name = u_name;
    surname = u_surname;
    nick =u_nick;
    accountexpire = u_accountexpire;
    rfid = u_rfid;
    created= new Date();
    activeSessions++;
   }

   public int getid()
   {
    return id;
   }
   public String getuserlogin()
   {
    return userlogin;
   }
   public int getprivilege()
   {
    return privilege;
   }
   public int getpin()
   {
    return pin;
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
     return sha256.toHexString(rfid);
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
   public void setprivilege(int priv)
   {
    privilege = priv;
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
