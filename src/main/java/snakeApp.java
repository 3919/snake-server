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
    public Response renderApp()
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            return Response.status(Response.Status.FORBIDDEN).entity("You are not allowed to be here").build();
        }

        userDescriptor u = (userDescriptor)session.getAttribute("user_info");
        return Response.ok("User authenticated, privilege: " + u.getprivilege()).build();
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

    @GET
    @Path("/test")
	@Produces({ MediaType.TEXT_HTML })
    public String getMain() 
    {
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(request.getServletContext());
		templateResolver.setTemplateMode("HTML5");

		TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);

		WebContext context = new WebContext(request, response, request.getServletContext());

		context.setVariable("test", "This is a string that will be mapped to the variable 'test'");

		return templateEngine.process("/test.html", context);
	}
    @GET
    @Path("/index")
    public void getHome(@Context HttpServletRequest request, 
                        @Context HttpServletResponse response) throws Exception {
        request.setAttribute("surname", "Cysterna");
        request.getRequestDispatcher("/some.jsp")
               .forward(request, response);
    }
};
