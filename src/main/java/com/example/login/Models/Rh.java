package com.example.login.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Getter
@Setter
@Entity
public class Rh {

    @Id
    private String idEmploye;

    private String nom;
    private String prenom;
    private String email;
    private Date dateCreation;
    private Date dateModification;

    @OneToOne
    @JoinColumn(name = "id_employe", referencedColumnName = "idEmploye", insertable = false, updatable = false)
    @JsonIgnoreProperties({"administrateur", "configurateur", "rh"})
    private EmployeSimple employeSimple;

}

