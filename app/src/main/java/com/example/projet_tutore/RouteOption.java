package com.example.projet_tutore;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RouteOption {
    private String mode;
    private String duration;
    private String distance;
    private String summary;
    private List<LatLng> polylinePoints;
    private int numberOfTransfers;
    private String price;
    private String arrivalTime;
    private List<RouteStep> steps;
    private String documentId;
    private String communityMode;

    public RouteOption(String mode, String duration, String distance, String summary,
                       List<LatLng> polylinePoints) {
        this.mode = mode;
        this.duration = duration;
        this.distance = distance;
        this.summary = summary;
        this.polylinePoints = polylinePoints;
        this.numberOfTransfers = 0;
        this.steps = new ArrayList<>();
        calculateArrivalTime();
    }

    // Calculer l'heure d'arrivée estimée
    private void calculateArrivalTime() {
        try {
            Calendar cal = Calendar.getInstance();
            int minutes = parseDurationToMinutes(duration);
            cal.add(Calendar.MINUTE, minutes);

            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            arrivalTime = String.format("%02d:%02d", hour, minute);
        } catch (Exception e) {
            arrivalTime = "--:--";
        }
    }

    private int parseDurationToMinutes(String duration) {
        int total = 0;
        if (duration.contains("hour")) {
            String[] parts = duration.split("hour");
            total += Integer.parseInt(parts[0].trim()) * 60;
            if (parts.length > 1 && parts[1].contains("min")) {
                String mins = parts[1].replaceAll("[^0-9]", "").trim();
                if (!mins.isEmpty()) {
                    total += Integer.parseInt(mins);
                }
            }
        } else if (duration.contains("min")) {
            String mins = duration.replaceAll("[^0-9]", "").trim();
            if (!mins.isEmpty()) {
                total = Integer.parseInt(mins);
            }
        }
        return total;
    }

    // Getters et Setters
    public String getMode() { return mode; }
    public String getDuration() { return duration; }
    public String getDistance() { return distance; }
    public String getSummary() { return summary; }
    public List<LatLng> getPolylinePoints() { return polylinePoints; }
    public int getNumberOfTransfers() { return numberOfTransfers; }
    public void setNumberOfTransfers(int numberOfTransfers) {
        this.numberOfTransfers = numberOfTransfers;
    }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getArrivalTime() { return arrivalTime; }
    public List<RouteStep> getSteps() { return steps; }
    public void setSteps(List<RouteStep> steps) { this.steps = steps; }

    public String getModeIcon() {
        switch (mode.toLowerCase()) {
            case "transit": return "🚇";
            case "walking": return "🚶";
            case "bicycling": return "🚴";
            case "driving": return "🚗";
            case "user": return "🌱";
            default: return "📍";
        }
    }

    public String getModeName() {
        switch (mode.toLowerCase()) {
            case "transit": return "Transport en commun";
            case "walking": return "À pied";
            case "bicycling": return "À vélo";
            case "driving": return "En voiture";
            case "user": return "Proposition utilisateur";
            default: return "Autre";
        }
    }

    public String getExtraInfo() {
        if (mode.equalsIgnoreCase("transit") && numberOfTransfers > 0) {
            return numberOfTransfers + " correspondance" + (numberOfTransfers > 1 ? "s" : "");
        } else if (mode.equalsIgnoreCase("walking")) {
            return "Marche directe";
        } else if (mode.equalsIgnoreCase("bicycling")) {
            return "Vélo direct";
        } else if (mode.equalsIgnoreCase("user")) {
            return "Partagée par la communauté";
        }
        return "";
    }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getCommunityMode() {
        return communityMode;
    }

    public void setCommunityMode(String communityMode) {
        this.communityMode = communityMode;
    }
}