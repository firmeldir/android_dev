package ru.android_2019.citycam.webcams;

public class Camera
{
    public String name;
    private String url_image;
    public String time;
    public String location;

    public Camera(String name,String url_image, String time, String location)
    {
        this.name = name;
        this.url_image = url_image;
        this.time = time;
        this.location = location;
    }

    public String getUrl_image()
    {
        return url_image;
    }
}
