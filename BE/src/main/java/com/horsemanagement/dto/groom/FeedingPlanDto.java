package com.horsemanagement.dto.groom;

import java.math.BigDecimal;

/**
 * DTO kế hoạch dinh dưỡng của ngựa (dbo.FEEDINGPLAN):
 * @param feedId  ID kế hoạch cho ăn
 * @param horseId ID chú ngựa
 * @param grain   Định lượng thức ăn hạt/tinh (kg)
 * @param grass   Định lượng cỏ tươi/khô (kg)
 * @param vitamin Định lượng chất bổ sung/vitamin (g)
 * @param meal    Bữa ăn trong ngày (Sáng, Trưa, Tối)
 */
public record FeedingPlanDto(int feedId, int horseId, BigDecimal grain, BigDecimal grass, BigDecimal vitamin, String meal) {}
