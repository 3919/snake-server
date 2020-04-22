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
   
   private Date sessionExpires;

   userDescriptor(int id, String login, int priv, String name, String surname, String nick, Date expire)
   {
    id = id;
    userlogin = login;
    privilege = priv;
    name = name;
    surname = surname;
    nick =nick;
    sessionExpires = expire;
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
   public boolean isValid()
   {
     return sessionExpires.compareTo(new Date()) < 0;
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
