<<<<<<< HEAD
package com.courtlink.booking.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateCourtRequest {
    private String name;
    private String description;
    private BigDecimal pricePerHour;
    private boolean available = true;
=======
package com.courtlink.booking.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateCourtRequest {
    private String name;
    private String description;
    private BigDecimal pricePerHour;
    private boolean available = true;
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 