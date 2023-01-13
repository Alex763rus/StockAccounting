package com.example.stockAccounting.model.jpa;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity(name = "material")
@Data
public class Material {
    @Id
    @Column(name = "material_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long materialId;

    @Column(name = "name")
    private String name;

//    @OneToMany(mappedBy="material", fetch= FetchType.EAGER)
//    private List<Stock> stocks;

    @OneToMany(mappedBy="material", fetch= FetchType.EAGER)
    private List<MaterialConsump> materialConsumpList;

    @Override
    public String toString() {
        return "Material{" +
                "materialId=" + materialId +
                ", name='" + name + '\'' +
                '}';
    }
}
