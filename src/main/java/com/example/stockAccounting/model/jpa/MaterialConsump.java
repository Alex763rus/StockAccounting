package com.example.stockAccounting.model.jpa;

import lombok.Data;

import javax.persistence.*;

@Entity(name = "material_consump")
@Data
public class MaterialConsump {

    @Id
    @Column(name = "material_consump_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long materialConsumpId;

    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "build_object_id")
    private BuildObject buildObject;

    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "material_id")
    private Material material;

    double cnt;

}
