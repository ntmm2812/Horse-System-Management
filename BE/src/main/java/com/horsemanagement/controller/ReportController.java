package com.horsemanagement.controller;

import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.common.*;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.groom.*;
import com.horsemanagement.dto.manager.Requests.FinanceInput;
import com.horsemanagement.security.Actor;
import com.horsemanagement.service.ReportService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller phục vụ các chức năng Báo cáo & Tài chính cho Club Manager:
 * - Báo cáo hiệu suất tập luyện và chăm sóc ngựa
 * - Báo cáo tổng hợp tài chính (Thu - Chi - Lợi nhuận ròng)
 * - Quản lý các khoản mục thu chi (Finance Entries)
 */
@RestController
@RequestMapping("/api/manager")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) { 
        this.service = service; 
    }

    /**
     * Báo cáo hiệu suất và thể lực của từng con ngựa trong khoảng thời gian [from, to].
     * Tổng hợp số buổi tập, tốc độ trung bình, nhịp tim trung bình, số việc chăm sóc hoàn thành.
     * Yêu cầu quyền: VIEW_REPORTS
     */
    @GetMapping("/reports/performance") 
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public PageDto<PerformanceDto> performance(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) { 
        return service.performance(from, to, page, size); 
    }

    /**
     * Báo cáo tài chính tổng hợp trong khoảng thời gian [from, to].
     * Tính tổng thu (income), tổng chi (expense), chênh lệch ròng (net), và phân loại theo nhóm chi phí.
     */
    @GetMapping("/reports/finance") 
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public FinanceReportDto finance(@RequestParam LocalDate from, @RequestParam LocalDate to) { 
        return service.finance(from, to); 
    }

    /**
     * Tra cứu danh sách các khoản thu/chi chi tiết trong khoảng thời gian [from, to].
     */
    @GetMapping("/finance-entries") 
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public PageDto<FinanceEntryDto> entries(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) { 
        return service.entries(from, to, page, size); 
    }

    /**
     * Nhập một khoản thu hoặc chi mới vào hệ thống (INCOME hoặc EXPENSE).
     * Yêu cầu quyền: MANAGE_FINANCE
     */
    @PostMapping("/finance-entries") 
    @ResponseStatus(HttpStatus.CREATED) 
    @PreAuthorize("hasAuthority('MANAGE_FINANCE')")
    public CreatedEntryDto createEntry(@AuthenticationPrincipal Actor actor, @Valid @RequestBody FinanceInput input) { 
        return service.createEntry(actor, input); 
    }
}
