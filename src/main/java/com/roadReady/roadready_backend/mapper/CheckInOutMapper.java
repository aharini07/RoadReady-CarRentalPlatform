package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.CheckInOutResponseDto;
import com.roadReady.roadready_backend.model.CheckInOut;

@Component
public class CheckInOutMapper {

    public CheckInOutResponseDto toResponseDto(CheckInOut c) {
        return new CheckInOutResponseDto(
                c.getCheckinId(),
                c.getReservation().getReservationId(),
                c.getAgent() == null ? null : c.getAgent().getAgentId(),
                c.getAgent() == null ? null : c.getAgent().getName(),
                c.getPickupCondition(),
                c.getReturnCondition(),
                c.getFuelLevelBefore(),
                c.getFuelLevelAfter(),
                c.getCheckinDate(),
                c.getCheckoutDate(),
                c.getRemarks()
        );
    }
}