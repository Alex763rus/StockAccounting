package com.example.stockAccounting.model.jpa;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity(name = "buildObject")
@Data
public class BuildObject {
    @Id
    @Column(name = "build_object_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long buildObjectId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy="buildObject", fetch= FetchType.EAGER)
    private List<MaterialConsump> materialConsumpList;

    @Override
    public String toString() {
        return buildObjectId +") " + name;
    }
}
