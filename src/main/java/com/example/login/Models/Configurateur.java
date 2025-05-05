package com.example.login.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Getter
@Setter
@Entity
public class Configurateur {

    @Id
    @Column(name = "id_configurateur")
    private String idConfigurateur;  // Renamed to match column name
    
    // Add this field to match the second column in your database
    @Column(name = "id_configuration")
    private String idConfiguration;

    private String nom;
    private String prenom;
    @Column(name = "email", unique = true)
private String email;
    private String telephone;
    private String nomUtilisateur;
    
    @Column(name = "mot_de_passe")
    private String motDePasse;
    
    @Column(name = "etat_compte")
    private String etatCompte;
    
    @Column(name = "date_creation")
    private Timestamp dateCreation;
    
    @Column(name = "date_modification")
    private Timestamp dateModification;

    @OneToOne
    @JoinColumn(name = "id_configurateur", referencedColumnName = "idEmploye", insertable = false, updatable = false)
    @JsonIgnoreProperties({"administrateur", "configurateur", "rh"})
    private EmployeSimple employeSimple;
 
    @ManyToMany
    @JoinTable(
        name = "administrateur_configurateur", 
        joinColumns = @JoinColumn(name = "id_configurateur"), 
        inverseJoinColumns = @JoinColumn(name = "id_administrateur")
    )
    private List<Administrateur> administrateurs;
    
    @ManyToMany
    @JoinTable(
        name = "societe_configurateur", 
        joinColumns = @JoinColumn(name = "id_configurateur"), 
        inverseJoinColumns = @JoinColumn(name = "id_societe")
    )
    private List<Societe> societes;
}