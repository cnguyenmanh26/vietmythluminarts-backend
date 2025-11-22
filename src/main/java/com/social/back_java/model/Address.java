package com.social.back_java.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {
    private String street;
    private String city;
    private String district;
    private String ward;
    private String country = "Vietnam";
    private String postalCode;
}
