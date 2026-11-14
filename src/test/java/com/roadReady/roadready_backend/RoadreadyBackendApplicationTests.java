package com.roadReady.roadready_backend;

import com.roadReady.roadready_backend.dto.PricingRequestDto;
import com.roadReady.roadready_backend.dto.PricingResponseDto;
import com.roadReady.roadready_backend.service.PricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RoadreadyBackendApplicationTests {

//	@Autowired
//	private PricingService pricingService;
//
//	@Test
//	void testPricingCalculation() {
//		// Arrange: Assume car ID 1 has a daily rate of 2500
//		PricingRequestDto request = new PricingRequestDto(
//				1L,
//				LocalDate.of(2026, 12, 1),
//				LocalDate.of(2026, 12, 4) // 3 days
//		);
//
//		// Act
//		PricingResponseDto response = pricingService.estimate(request);
//
//		// Assert
//		assertNotNull(response);
//		assertEquals(3, response.durationDays(), "Duration should be 3 days");
//
//		// 2500 * 3 = 7500 subtotal. 7500 * 0.18 tax = 1350. Total = 8850.
//		assertEquals(new BigDecimal("7500.00"), response.subtotal(), "Subtotal should be 7500");
//		assertEquals(new BigDecimal("1350.00"), response.tax(), "Tax should be 1350");
//		assertEquals(new BigDecimal("8850.00"), response.totalAmount(), "Total should be 8850");
//	}
}