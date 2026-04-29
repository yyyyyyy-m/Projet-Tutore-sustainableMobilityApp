package com.example.projet_tutore;

public class TDB {
    private String Type;
    private String ligne;
    private String signal;
    private String depart;
    private String arrivee;
    private String duree;
    private int numStops;
    private String couleur;

    public TDB(String TypeV, String ligne, String signal,
               String depart, String arrivee, String duree,
               int numStops, String couleur) {
        this.Type = TypeV;
        this.ligne = ligne;
        this.signal = signal;
        this.depart = depart;
        this.arrivee = arrivee;
        this.duree = duree;
        this.numStops = numStops;
        this.couleur = couleur;
    }

    // Getters
    public String getType() { return Type; }
    public String getLigne() { return ligne; }
    public String getSignal() { return signal; }
    public String getDepart() { return depart; }
    public String getArrivee() { return arrivee; }
    public String getDuree() { return duree; }
    public int getNumStops() { return numStops; }
    public String getCouleur() { return couleur; }
}