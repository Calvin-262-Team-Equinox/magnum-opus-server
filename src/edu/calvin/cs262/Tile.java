package edu.calvin.cs262;

/**
 * A Tile class (POJO) for the tile relation
 */
public class Tile
{

    private int version;
    private String data;

    Tile()
    { /* a default constructor, required by Gson */ }

    Tile(String data, int version)
    {
        this.data = data;
        this.version = version;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

}
