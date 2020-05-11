package rest;

public class Device
{
    int id=-1;
    String name="";
    int type=0;
    String token="";
    Device()
    {}
    Device(int id_, String name_, int type_, String token_)
    {
        id = id_;
        name = name_;
        type = type_;
        token = token_;
    }
    public int getid()
    {
        return id;
    }
    public String getname()
    {
        return name;
    }
    public int gettype()
    {
        return type;
    }
    public String gettoken()
    {
        return token;
    }
};
