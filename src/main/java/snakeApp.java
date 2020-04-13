package rest;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.servlet.http.*;
import java.net.URI;

@Path("/app")
public class snakeApp{

    @Context
    private HttpServletRequest request;
    
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
};
