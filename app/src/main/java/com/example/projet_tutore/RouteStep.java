package com.example.projet_tutore;

public class RouteStep {
    private String type; // TRANSIT, WALKING, etc.
    private String title;
    private String details;
    private String duration;
    private String lineName;
    private String lineColor;
    private String vehicleType;

    public RouteStep(String type, String title, String details, String duration) {
        this.type = type;
        this.title = title;
        this.details = details;
        this.duration = duration;
    }

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getDuration() { return duration; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public String getLineColor() { return lineColor; }
    public void setLineColor(String lineColor) { this.lineColor = lineColor; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getStepIcon() {
        if (type.equals("TRANSIT")) {
            if (vehicleType != null) {
                switch (vehicleType.toLowerCase()) {
                    case "subway":
                    case "metro": return "🚇";
                    case "bus": return "🚌";
                    case "tram": return "🚊";
                    case "train": return "🚆";
                    default: return "🚍";
                }
            }
            return "🚍";
        } else if (type.equals("WALKING")) {
            return "🚶";
        }
        return "➡️";
    }
}