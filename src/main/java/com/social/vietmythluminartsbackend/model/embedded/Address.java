package com.social.vietmythluminartsbackend.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded address document for user shipping/billing information
 * Used in User model and Order model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /**
     * Full name of recipient (for shipping)
     * Optional in user profile, required in orders
     */
    private String fullName;

    /**
     * Phone number for delivery contact
     * Optional in user profile, required in orders
     */
    private String phone;

    /**
     * Street address
     * e.g., "123 Nguyen Hue"
     */
    private String street;

    /**
     * Ward/Commune
     * e.g., "Ben Nghe"
     */
    private String ward;

    /**
     * District
     * e.g., "District 1", "Cầu Giấy"
     */
    private String district;

    /**
     * City/Province
     * e.g., "Ho Chi Minh City", "Hà Nội"
     */
    private String city;

    /**
     * Country
     * Default: "Vietnam"
     */
    @Builder.Default
    private String country = "Vietnam";

    /**
     * Postal code
     * e.g., "700000"
     */
    private String postalCode;
}

