package ca.knowtime;

public class Route
{
    boolean m_isFavourite;
    String m_id;
    String m_longName;
    String m_shortName;


    public Route()
    {
    }


    public Route( String longName, String shortName )
    {
        m_id = "";
        m_longName = longName;
        m_shortName = shortName;
        m_isFavourite = false;
    }


    public Route( String longName, String shortName, String id, boolean isFavourite )
    {
        m_id = "";
        m_longName = longName;
        m_shortName = shortName;
        m_isFavourite = isFavourite;
    }


    public String getId()
    {
        return m_id;
    }


    public void setId( String id )
    {
        m_id = id;
    }


    public String getLongName()
    {
        return m_longName;
    }


    public void setLongName( String longName )
    {
        m_longName = longName;
    }


    public String getShortName()
    {
        return m_shortName;
    }


    public void setShortName( String shortName )
    {
        m_shortName = shortName;
    }


    public boolean getFavourite()
    {
        return m_isFavourite;
    }


    public void setFavourite( Boolean isFavourite )
    {
        m_isFavourite = isFavourite;
    }
}
