import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/error-handler-servlet")
public class ErrorHandlerServlet extends HttpServlet {
  @Override
  protected void service(HttpServletRequest request, 
                         HttpServletResponse response) throws ServletException, IOException {
      int statusCode = response.getStatus();

      if (statusCode >= 200 && statusCode < 299) {
          super.service(request, response);
      } else {
          Response.Status status = Response.Status.fromStatusCode(statusCode);
          try (PrintWriter writer = response.getWriter()) {
              writer.write("");
              writer.flush();
          }
      }
   }
}
