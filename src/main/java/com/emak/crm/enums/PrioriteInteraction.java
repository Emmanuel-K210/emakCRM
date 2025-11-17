package com.emak.crm.enums;

public enum PrioriteInteraction {
    BASSE("Basse", "secondary"),
    NORMALE("Normale", "primary"), 
    HAUTE("Haute", "warning"),
    URGENTE("Urgente", "danger");
    
    private final String libelle;
    private final String couleur;
    
    PrioriteInteraction(String libelle, String couleur) {
        this.libelle = libelle;
        this.couleur = couleur;
    }
    
    public String getLibelle() { return libelle; }
    public String getCouleur() { return couleur; }
}