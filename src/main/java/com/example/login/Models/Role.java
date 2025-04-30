package com.example.login.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Entity
public class Role {

    @Id
    private String idRole;

    private String nomRole;

    private String description;

    @OneToMany(mappedBy = "role")
    private List<EmployeSimple> employes;

}