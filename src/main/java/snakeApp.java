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

@Path(config.app_url)
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
            response.sendRedirect(config.getLoginUrl());
            return;
        }

        userDescriptor u = (userDescriptor)session.getAttribute("user_info");
        request.setAttribute("temp_in",      32.7);
        request.setAttribute("humidity_out", 38.2);
        request.getRequestDispatcher(config.snake_page)
               .forward(request, response);
    }

    @GET
    @Path(config.user_edit_url)
    public void renderEdit()throws Exception
    {

    }

    @POST
    @Path(config.user_edit_url)
    public void serviceEdit()throws Exception
    {

    }

    @GET
    @Path(config.logout_url)
    public Response logout() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            return Response.status(Response.Status.FORBIDDEN).entity("You are not allowed to be here").build();
        }

        URI uri = new URI(config.logout_url);
        return Response.seeOther(uri).build();
    }
};
