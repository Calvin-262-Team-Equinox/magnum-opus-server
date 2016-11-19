package edu.calvin.cs262;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    public byte[] getRawData()
    {
        try
        {
            return Base64.decode(data);
        } catch (Base64DecodingException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public BufferedImage getData()
    {
        try
        {
            return ImageIO.read(new ByteArrayInputStream(Base64.decode(data)));
        } catch (Base64DecodingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public void setData(BufferedImage img)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(img, "png", stream);
            data = Base64.encode(stream.toByteArray());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
