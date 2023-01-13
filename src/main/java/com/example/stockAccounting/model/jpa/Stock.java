package com.example.stockAccounting.model.jpa;

import lombok.Data;

import javax.persistence.*;

@Entity(name = "stock")
@Data
public class Stock {

    @Id
    @Column(name = "stock_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long stockId;

    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "material_id")
    private Material material;

    double cnt;

}
