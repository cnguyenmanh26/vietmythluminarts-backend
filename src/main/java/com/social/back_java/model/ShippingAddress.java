package com.social.back_java.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ShippingAddress {
    private String fullName;
    private String phone;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country = "Vietnam";
}
