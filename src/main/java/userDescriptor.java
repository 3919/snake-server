package rest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
class userDescriptor
{
   @NotNull
   private String username;

   @NotNull
   @Max(3)
   private int privilege;
   
   userDescriptor(String n, int p)
   {
    username = n;
    privilege = p;
   }

   public String getusername()
   {
    return username;
   }
   public int getprivilege()
   {
    return privilege;
   }
   public void setusername(String name)
   {
       username = name;
   }
   public void setprivilege(int priv)
   {
       privilege = priv;
   }
}
