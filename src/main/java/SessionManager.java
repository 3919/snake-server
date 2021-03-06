package rest;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.inject.Inject;

@WebListener
public class SessionManager implements HttpSessionListener {
  @Inject
  private SystemCore sc;
  
  @Override
  public void sessionCreated(HttpSessionEvent se) {
      HttpSession session = se.getSession();
      session.setMaxInactiveInterval(3600);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
      HttpSession session = se.getSession();
      UserDescriptor u =(UserDescriptor)session.getAttribute("user_info");
      sc.removeUser(u);
  }
}
