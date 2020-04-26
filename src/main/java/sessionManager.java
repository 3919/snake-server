package rest;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.inject.Inject;

@WebListener
public class sessionManager implements HttpSessionListener {
  @Inject
  private systemCore sc;
  
  @Override
  public void sessionCreated(HttpSessionEvent se) {
      System.out.println("-- HttpSessionListener#sessionCreated invoked --");
      HttpSession session = se.getSession();
      session.setMaxInactiveInterval(10);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
      System.out.println("-- HttpSessionListener#sessionDestroyed invoked --");
      HttpSession session = se.getSession();
    
      userDescriptor u =(userDescriptor)session.getAttribute("user_info");
      sc.removeUser(u);
  }
}
