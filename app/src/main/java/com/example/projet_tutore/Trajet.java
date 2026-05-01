package com.example.projet_tutore;

public class Trajet {

    public String id;
    public String driverName;
    public String initials;

    public String depart;
    public String destination;
    public String departLower;
    public String destinationLower;

    public String date;
    public String time;
    public int places;
    public double price;
    public String status;

    public double departLat;
    public double departLng;
    public double destinationLat;
    public double destinationLng;

    public String routePolyline;
    public String routeDistance;
    public String routeDuration;
    public String travelMode;

    public Trajet() {}

    public Trajet(String driverName, String initials,
                  String depart, String destination,
                  String departLower, String destinationLower,
                  String date, String time,
                  int places, double price,
                  String status) {

        this.driverName = driverName;
        this.initials = initials;
        this.depart = depart;
        this.destination = destination;
        this.departLower = departLower;
        this.destinationLower = destinationLower;
        this.date = date;
        this.time = time;
        this.places = places;
        this.price = price;
        this.status = status;
    }
}