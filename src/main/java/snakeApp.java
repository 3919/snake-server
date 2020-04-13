package rest;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.servlet.http.*;

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

        return Response.ok("Mhm, no to mamy problem").build();
    }
};
