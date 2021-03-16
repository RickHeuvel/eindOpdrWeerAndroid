package com.example.eindopdrweer;

public class CityModel {

    private String name;
    private Double temp;
    private String tempUnit;
    private String imgUrl;
    private double lat;
    private double lon;



    public CityModel(String name, String tempUnit) {
        this.name = name;
        this.tempUnit = tempUnit;
    }
    public CityModel(String tempUnit){
        this.tempUnit = tempUnit;
    }

    public String getName(){
        return name;
    }
    public double getLat(){
        return lat;
    }
    public double getLon(){
        return lon;
    }
    public String getTemp()
    {
        if (temp != null){
            String s;
            switch (tempUnit){
                case "imperial":
                    s = temp.toString()+"°F";
                    return s;
                case "metric":
                    s = temp.toString()+"℃";
                    return s;
                case "":
                     s = temp.toString()+"°K";
                     return s;
                     default:
                         return tempUnit;
            }
        }else{
            return "no temp found";
        }
    }
    public String getImgUrl() {

        return imgUrl;
    }

    public void setTemp(Double temp){
        this.temp = temp;
    }
    public void setImgUrl(String url) {
        this.imgUrl = "https://openweathermap.org/img/wn/"+url+"@2x.png";
    }
    public void setLat(double lat){
        this.lat = lat;
    }
    public void setLon(double lon){
        this.lon = lon;
    }



}
