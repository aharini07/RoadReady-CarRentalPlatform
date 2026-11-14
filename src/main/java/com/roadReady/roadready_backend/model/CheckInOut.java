package com.roadReady.roadready_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_out")
@Getter
@Setter
@NoArgsConstructor
public class CheckInOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkin_id")
    private Long checkinId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private RentalAgent agent;

    @Column(name = "pickup_condition", length = 255)
    private String pickupCondition;

    @Column(name = "return_condition", length = 255)
    private String returnCondition;

    @Column(name = "fuel_level_before", length = 50)
    private String fuelLevelBefore;

    @Column(name = "fuel_level_after", length = 50)
    private String fuelLevelAfter;

    @Column(name = "checkin_date")
    private LocalDateTime checkinDate;

    @Column(name = "checkout_date")
    private LocalDateTime checkoutDate;

    @Column(name = "remarks", length = 255)
    private String remarks;
}
