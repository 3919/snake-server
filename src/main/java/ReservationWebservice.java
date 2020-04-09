package rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/reservation")
public class ReservationWebservice {
    @GET
    public Response listReservations() {
        return Response.ok("Oto wszystkie rezerwacje :)").build();
    }
}

