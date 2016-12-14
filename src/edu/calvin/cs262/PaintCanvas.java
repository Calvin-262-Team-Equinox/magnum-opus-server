package edu.calvin.cs262;

/**
 * This class is used to create an object that stores a canvas' name and ID#, in order to allow searching for a canvas by name.
 *      The data in this class is used when a canvas is retrieved.
 * Created by jrd58 on 12/13/2016.
 */
public class PaintCanvas
{
    private int ID;
    private String name;

    //Constructor
    PaintCanvas(int ID, String name)
    {
        this.ID = ID;
        this.name = name;
    }

    //Get a canvas' ID
    public int getID()
    {
        return ID;
    }

    //Get a canvas' name
    public String getName()
    {
        return name;
    }

}

