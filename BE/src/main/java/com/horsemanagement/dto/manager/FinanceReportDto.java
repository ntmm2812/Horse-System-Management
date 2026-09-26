package com.horsemanagement.dto.manager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO báo cáo tổng quan tài chính chuồng ngựa trong một kỳ:
 * @param income     Tổng doanh thu
 * @param expense    Tổng chi phí
 * @param net        Lợi nhuận ròng (income - expense)
 * @param entryCount Tổng số bút toán trong kỳ
 * @param currency   Đơn vị tiền tệ (mặc định "VND")
 * @param from       Từ ngày
 * @param to         Đến ngày
 * @param byCategory Phân rã chi tiết số tiền theo từng danh mục
 */
public record FinanceReportDto(BigDecimal income, BigDecimal expense, BigDecimal net, long entryCount,
                              String currency, LocalDate from, LocalDate to, List<FinanceCategoryDto> byCategory) {}
