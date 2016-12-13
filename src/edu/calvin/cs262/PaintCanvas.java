package edu.calvin.cs262;

/**
 * Created by jrd58 on 12/13/2016.
 */
public class PaintCanvas
{
    private int ID;
    private String name;


    PaintCanvas(int ID, String name)
    {
        this.ID = ID;
        this.name = name;
    }

    public int getID()
    {
        return ID;
    }

    public String getName()
    {
        return name;
    }

}

