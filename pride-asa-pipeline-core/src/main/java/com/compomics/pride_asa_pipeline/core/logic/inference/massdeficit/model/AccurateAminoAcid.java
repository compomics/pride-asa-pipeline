/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pride_asa_pipeline.core.logic.inference.massdeficit.model;

import java.math.BigDecimal;

/**
 *
 * @author Kenneth
 */
public enum AccurateAminoAcid {
    A(71.037114),
    R(156.101111),
    N(114.042928),
    D(115.026944),
    C(103.009214),
    E(129.042594),
    Q(128.058578),
    G(57.021464),
    H(137.058912),
    I(113.084064),
    L(113.084064),
    K(128.094963),
    M(131.040514),
    F(147.068414),
    P(97.052764),
    S(87.032029),
    SP(166.998359),
    T(101.047679),
    TP(181.014009),
    U(150.953635),
    W(186.079313),
    Y(163.063329),
    YP(243.029659),
    V(99.068414);

    private final BigDecimal accurate_mass;

    private AccurateAminoAcid(double accurate_mass) {
        this.accurate_mass = new BigDecimal(accurate_mass);
    }

    public BigDecimal getAccurateMass() {
        return accurate_mass;
    }

}
