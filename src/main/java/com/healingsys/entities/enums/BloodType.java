package com.healingsys.entities.enums;

public enum BloodType {
    NULL_NEGATIVE("0-"),
    NULL_POSITIVE("0+"),
    A_NEGATIVE("A-"),
    A_POSITIVE("A+"),
    B_NEGATIVE("B-"),
    B_POSITIVE("B+"),
    AB_NEGATIVE("AB-"),
    AB_POSITIVE("AB+");

    private final String bloodType;

    BloodType(String s) {
        this.bloodType = s;
    }

    @Override
    public String toString() {
        return this.bloodType;
    }
}
