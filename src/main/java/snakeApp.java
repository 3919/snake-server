package rest;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.servlet.http.*;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

@Path("/app")
public class snakeApp{

    @Context
    private HttpServletRequest request;
    
	@Context
	private HttpServletResponse response;

    @GET
    public void renderApp ()throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect("/rest/login");
            return;
        }

        userDescriptor u = (userDescriptor)session.getAttribute("user_info");
        request.setAttribute("surname", "Cysterna");
        request.getRequestDispatcher("/some.jsp")
               .forward(request, response);
    }

    @GET
    @Path("/logout")
    public Response logout() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            return Response.status(Response.Status.FORBIDDEN).entity("You are not allowed to be here").build();
        }

        URI uri = new URI("rest/logout");
        return Response.seeOther(uri).build();
    }
};
