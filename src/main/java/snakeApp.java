package rest;
import java.sql.*;
import java.io.*; 

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
import javax.inject.Inject;

@Path(config.app_url)
public class snakeApp{

    public final class PassStatus{
        public static final int PASS_CHANGE_OK     = 0;
        public static final int PASS_CHANGE_FAILED = 1;
        public static final int PASS_IDLE = -1;
    }

    @Context
    private HttpServletRequest request;
    
	@Context
	private HttpServletResponse response;
    
    @Inject
    private systemCore sc;

    @GET
    public void renderApp() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        requestSetAttributes(PassStatus.PASS_IDLE, u);
        request.getRequestDispatcher(config.snake_page)
               .forward(request, response);
    }

    void requestSetAttributes(int pass_stat, userDescriptor u)
    {
        laboratoryState l = sc.getLabState();
        request.setAttribute("u_info",       u);
        request.setAttribute("active_users", l.loggedUsers);
        request.setAttribute("temp_in",      l.temperature);
        request.setAttribute("humidity_out", l.humidity);
        request.setAttribute("response_msg", pass_stat);
    }

    @POST
    public void changeUserPassword(
		@FormParam("old_pass") String o_password, 
		@FormParam("new_pass") String n_password, 
		@FormParam("new_pass_repeated") String r_password) 
        throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            response.sendRedirect(config.getLoginUrl());
            return;
        }
        // checkc if old password is correct
        userDescriptor u =(userDescriptor)session.getAttribute("user_info");
        Class.forName("org.mariadb.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/pwr_snake", "wind", "alamakota");
        PreparedStatement stmt = conn.prepareStatement("select * from Users where login=? and pass_hash=?");
        String pass_hash = sha256.toHexString(sha256.getSHA(o_password)); 
        String login = u.getuserlogin(); 
        stmt.setString(1, login);
        stmt.setString(2, pass_hash);
        ResultSet res = stmt.executeQuery();
        if(!res.next())
        {
            requestSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }
        // new passwords match?
        if(!n_password.equals(r_password))  
        {
            requestSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }
        
        // update new password
        stmt = conn.prepareStatement("UPDATE Users SET pass_hash=? WHERE login=?");
        pass_hash = sha256.toHexString(sha256.getSHA(n_password)); 

        stmt.setString(1, pass_hash);
        stmt.setString(2, login);
        int rowsUpdated = stmt.executeUpdate();
        if(rowsUpdated == 0)
        {
            requestSetAttributes(PassStatus.PASS_CHANGE_FAILED,u);
            request.getRequestDispatcher(config.snake_page)
                   .forward(request, response);
            return;
        }

        requestSetAttributes(PassStatus.PASS_CHANGE_OK, u);
        request.getRequestDispatcher(config.snake_page)
               .forward(request, response);
        return;
    }

    @GET
    @Path(config.user_manage_url)
    public void renderUserManagerPage()throws Exception
    {

    }

    @POST
    @Path(config.user_add_url)
    public void addUser()throws Exception
    {

    }

    @POST
    @Path(config.user_edit_url)
    public void editUser()throws Exception
    {

    }

    @GET
    @Path(config.user_remove_url)
    public void removeUser()throws Exception
    {

    }

    @GET
    @Path(config.logout_url)
    public Response logout() throws Exception
    {
        HttpSession session = request.getSession(false);
        if(session == null)
        {
            URI uri = new URI(config.getLoginUrl());
            return Response.seeOther(uri).build();
        }

        URI uri = new URI(config.logout_url);
        return Response.seeOther(uri).build();
    }
};
