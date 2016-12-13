package edu.calvin.cs262;

import com.google.gson.Gson;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.core.util.Base64;
import com.sun.net.httpserver.HttpServer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;

@Path("/equinox")
public class CanvasResource
{

    /**
     * a hello-world resource
     *
     * @return a simple string value
     */
    @SuppressWarnings("SameReturnValue")
    @GET
    @Path("/hello")
    @Produces("text/plain")
    public String getClichedMessage()
    {
        return "Hello, Jersey!";
    }

    /**
     * GET method that returns a particular tile
     *
     * @param canvasID    the id of the canvas
     * @param xCoordinate the x coordinate of the tile
     * @param yCoordinate the y coordinate of the tile
     * @param version     the tile version the client already has
     * @return a JSON version of the tile record, if any, with the given id
     */
    @GET
    @Path("/tile/{canvasID}/{xCoordinate}/{yCoordinate}/{version}")
    @Produces("application/json")
    public String getTile(
            @PathParam("canvasID") int canvasID,
            @PathParam("xCoordinate") int xCoordinate,
            @PathParam("yCoordinate") int yCoordinate,
            @PathParam("version") int version)
    {
        try
        {
            Tile tile = retrieveCanvasTile(canvasID, xCoordinate, yCoordinate);
            if (tile == null)
            {
                return null;
            }
            else if (tile.getVersion() <= version)
            {
                // No need to send duplicate data.
                tile.setData("");
            }
            return new Gson().toJson(tile);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * GET method that returns a particular canvas
     *
     * @param name          the name of the canvas
     *
     */
    @GET
    @Path("/search/canvas/{name}")
    @Produces("application/json")
    public String getCanvas(
            @PathParam("name") String name)
    {
        try{
            PaintCanvas canvas = retrieveCanvas(name);
            return new Gson().toJson(canvas);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * POST method that updates a particular tile
     *
     * @param canvasID    the id of the canvas
     * @param xCoordinate the x coordinate of the tile
     * @param yCoordinate the y coordinate of the tile
     * @return a JSON version of the tile record, if any, with the given id
     */
    @POST
    @Path("/update/tile/{canvasID}/{xCoordinate}/{yCoordinate}")
    @Consumes("application/json")
    @Produces("application/json")
    public String postUpdateTile(
            @PathParam("canvasID") int canvasID,
            @PathParam("xCoordinate") int xCoordinate,
            @PathParam("yCoordinate") int yCoordinate,
            String tileData)
    {
        try
        {
            Tile userUpdateTile = new Gson().fromJson(tileData, Tile.class);
            BufferedImage userUpdateImg = userUpdateTile.getData();

            Tile baseTile = retrieveCanvasTile(canvasID, xCoordinate, yCoordinate);
            BufferedImage baseImg = null;
            if (baseTile != null)
            {
                baseImg = baseTile.getData();
            }
            if (baseImg == null)
            {
                if (userUpdateImg == null)
                {
                    return null;
                }
                // First version of new tile.
                userUpdateTile.setVersion(1);
                upsertTile(userUpdateTile, canvasID, xCoordinate, yCoordinate);
                return tileData;
            }

            if (userUpdateImg == null)
            {
                // No (valid) update sent?
                return new Gson().toJson(baseTile);
            }

            // Compute and save updates. TODO: handle race condition.
            Graphics dc = baseImg.getGraphics();
            dc.drawImage(userUpdateImg, 0, 0, null);
            dc.dispose();

            baseTile.setData(baseImg);
            baseTile.setVersion(baseTile.getVersion() + 1);
            upsertTile(baseTile, canvasID, xCoordinate, yCoordinate);

            return new Gson().toJson(baseTile);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * POST method to create a new canvas
     *
     * @param canvasName  the name of the canvas
     * @return a JSON version of the tile record, if any, with the given id
     */
    @POST
    @Path("/create/canvas/{canvasName}")
    @Consumes("application/json")
    @Produces("application/json")
    public String postCreateCanvas(
            @PathParam("canvasName") String canvasName,
            String tileData)
    {
        try
        {
            int canvasID = createNewCanvas(canvasName);
            return "{\"key\":" + canvasID + "}";

        } catch (Exception e)
        {
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
    private Tile retrieveCanvasTile(
            int canvasID,
            int xCoordinate,
            int yCoordinate
    )
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Tile tile = null;
        try
        {
            connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            statement = connection.prepareStatement(
                    "SELECT data, version FROM Tile"
                    + " WHERE canvasID = ?"
                    + " AND xCoordinate = ?"
                    + " AND yCoordinate = ?"
            );
            statement.setInt(1, canvasID);
            statement.setInt(2, xCoordinate);
            //noinspection SuspiciousNameCombination
            statement.setInt(3, yCoordinate);
            rs = statement.executeQuery();
            if (rs.next())
            {
                tile = new Tile(
                        new String(Base64.encode(rs.getBytes("data"))),
                        rs.getInt("version")
                );
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (statement != null)
                {
                    statement.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return tile;
    }

    /*
   * Utility method that does the database query, potentially throwing an SQLException,
   * returning a canvas object (or null).
   */
    private PaintCanvas retrieveCanvas(
            String name
    )
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        PaintCanvas canvas = null;
        try
        {
            connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            statement = connection.prepareStatement(
                    "SELECT ID, name FROM Canvas"
                            + " WHERE name = ?"
            );
            statement.setString(1, name);
            rs = statement.executeQuery();
            if (rs.next())
            {
                canvas = new PaintCanvas(
                        rs.getInt("ID"),
                        rs.getString("name")
                );
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (statement != null)
                {
                    statement.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return canvas;
    }

    /*
     * Utility method to update a tile, or insert if it does not exist.
     */
    private void upsertTile(
            Tile tile,
            int canvasID,
            int xCoordinate,
            int yCoordinate
    )
    {
        Connection connection = null;
        PreparedStatement statement = null;
        try
        {
            connection = DriverManager.getConnection(DB_URI, DB_LOGIN_ID, DB_PASSWORD);
            statement = connection.prepareStatement(
                    "INSERT INTO Tile (canvasID, xCoordinate, yCoordinate, data, time, version)"
                            + " VALUES (?, ?, ?, ?, current_timestamp, ?)"
                            + " ON CONFLICT (canvasID, xCoordinate, yCoordinate)"
                            + " DO UPDATE SET"
                            + " data = EXCLUDED.data,"
                            + " time = EXCLUDED.time,"
                            + " version = EXCLUDED.version"
            );
            statement.setInt(1, canvasID);
            statement.setInt(2, xCoordinate);
            //noinspection SuspiciousNameCombination
            statement.setInt(3, yCoordinate);
            statement.setBytes(4, tile.getRawData());
            statement.setInt(5, tile.getVersion());
            statement.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (statement != null)
                {
                    statement.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    /* Main *****************************************************/

    /**
     * Run this main method to fire up the service.
     *
     * @param args command-line arguments (ignored)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServerFactory.create("http://localhost:" + PORT + "/");
        server.start();

        System.out.println("Server running...");
        System.out.println("Web clients should visit: http://localhost:" + PORT + "/equinox");
        System.out.println("Android emulators should visit: http://LOCAL_IP_ADDRESS:" + PORT + "/equinox");
        System.out.println("Hit return to stop...");
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        System.out.println("Stopping server...");
        server.stop(0);
        System.out.println("Server stopped...");
    }
}
