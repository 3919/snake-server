package rest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
import java.util.Date;

public class userDescriptor
{
   @NotNull
   private String userlogin;

   @NotNull
   @Max(3)
   private int id;
   private int privilege;
   
   private String name;
   
   private String surname;
   
   private String nick;
   
   private int activeSessions=0;

   userDescriptor(int id, String login, int priv, String name, String surname, String nick)
   {
    id = id;
    userlogin = login;
    privilege = priv;
    name = name;
    surname = surname;
    nick =nick;
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
