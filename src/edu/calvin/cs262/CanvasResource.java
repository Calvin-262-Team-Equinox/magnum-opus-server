package edu.calvin.cs262;

import com.google.gson.Gson;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.sql.*;

@Path("/equinox")
public class CanvasResource {

    /**
     * a hello-world resource
     *
     * @return a simple string value
     */
    @SuppressWarnings("SameReturnValue")
    @GET
    @Path("/hello")
    @Produces("text/plain")
    public String getClichedMessage() {
        return "Hello, Jersey!";
    }

    /**
     * GET method that returns a particular tile
     *
     * @param canvasID the id of the canvas
     * @param xCoordinate the x coordinate of the tile
     * @param yCoordinate the y coordinate of the tile
     * @return a JSON version of the tile record, if any, with the given id
     */
    @GET
    @Path("/tile/{canvasID}/{xCoordinate}/{yCoordinate}")
    @Produces("application/json")
    public String getTile(
            @PathParam("canvasID") int canvasID,
            @PathParam("xCoordinate") int xCoordinate,
            @PathParam("yCoordinate") int yCoordinate) {
        try {
            return new Gson().toJson(retrieveCanvas(canvasID, xCoordinate, yCoordinate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* DBMS Utility Functions *********************************************/

    /**
     * Constants for a local Postgresql server with the monopoly database
     */
    private static final String DB_URI = "jdbc:postgresql://localhost:5432/magnum_opus";
    private static final String DB_LOGIN_ID = "postgres";
    private static final String DB_PASSWORD = "Listen-Anywhere-6";
    private static final String PORT = "8085";

    /*
     * Utility method that does the database query, potentially throwing an SQLException,
     * returning a tile object (or null).
     */
    private Tile retrieveCanvas(
            int canvasID,
            int xCoordinate,
            int yCoordinate) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        Tile tile = null;
        try {
            connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            statement = connection.createStatement();
            rs = statement.executeQuery("SELECT `data`, version FROM Tile"
                    + " WHERE canvasID=" + canvasID
                    + " AND xCoordinate=" + xCoordinate
                    + " AND yCoordinate=" + yCoordinate);
            if (rs.next()) {
                tile = new Tile(rs.getString("data"), rs.getInt("version"));
            }
        } catch (SQLException e) {
            throw (e);
        } finally {
            rs.close();
            statement.close();
            connection.close();
        }
        return tile;
    }

    /** Main *****************************************************/

    /**
     * Run this main method to fire up the service.
     *
     * @param args command-line arguments (ignored)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServerFactory.create("http://localhost:" + PORT + "/");
        server.start();

        System.out.println("Server running...");
        System.out.println("Web clients should visit: http://localhost:" + PORT + "/monopoly");
        System.out.println("Android emulators should visit: http://LOCAL_IP_ADDRESS:" + PORT + "/monopoly");
        System.out.println("Hit return to stop...");
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        System.out.println("Stopping server...");
        server.stop(0);
        System.out.println("Server stopped...");
    }
}