package com.example.login.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "societe")
public class Societe {

    @Id
    @Column(name = "id_societe")
    private String idSociete;
    
    @Column(name = "nom_societe")
    private String nomSociete;
    
    private String adresse;
    
    private String ville;
    
    @Column(name = "identifiant_fiscal")
    private String identifiantFiscal;
    
    @Column(name = "numero_cnss")
    private String numeroCnss;
    
    @Column(name = "numero_ice")
    private String numeroIce;
    
    @Column(name = "numero_rc")
    private String numeroRc;
    
    @Column(name = "date_debut")
    @Temporal(TemporalType.DATE)
    private Date dateDebut;
    
    @Column(name = "date_fin")
    @Temporal(TemporalType.DATE)
    private Date dateFin;
    
    private String nom;
    
    private String rib;
    
    private String bic;
    
    // Add many-to-many relationship with Configurateur
    @ManyToMany(mappedBy = "societes")
    private List<Configurateur> configurateurs;
}