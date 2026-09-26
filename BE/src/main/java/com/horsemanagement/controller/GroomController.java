package com.horsemanagement.controller;

import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.common.*;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.groom.*;
import com.horsemanagement.dto.groom.IncidentInput;
import com.horsemanagement.security.Actor;
import com.horsemanagement.service.GroomService;
import com.horsemanagement.service.ManagerService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller dành riêng cho nhân viên chuồng trại (Role 'Groom'):
 * - Xem chuồng trại & ô chuồng được phân công
 * - Xem lịch trình hoạt động/tập luyện trong ngày
 * - Xem khẩu phần ăn của ngựa
 * - Xác nhận hoàn thành các công việc chăm sóc (Care Tasks)
 * - Lập báo cáo sự cố (Incident Reports)
 * - Tra cứu số lượng vật tư kho
 */
@RestController
@RequestMapping("/api/groom")
public class GroomController {
    private final GroomService service;
    private final ManagerService manager;

    public GroomController(GroomService service, ManagerService manager) {
        this.service = service;
        this.manager = manager;
    }

    /**
     * Xem danh sách các chuồng và ô chuồng của ngựa mà Groom được phân công trong ngày.
     * Yêu cầu quyền: VIEW_STABLE
     */
    @GetMapping("/stables") 
    @PreAuthorize("hasAuthority('VIEW_STABLE')")
    public PageDto<StableDto> stables(
            @AuthenticationPrincipal Actor actor,
            @RequestParam(required=false) LocalDate date,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return service.stables(actor, day(date), page, size);
    }

    /**
     * Xem lịch sinh hoạt / tập luyện của các con ngựa do Groom phụ trách trong ngày.
     */
    @GetMapping("/schedule") 
    @PreAuthorize("hasAuthority('VIEW_STABLE')")
    public List<ScheduleDto> schedule(
            @AuthenticationPrincipal Actor actor,
            @RequestParam(required=false) LocalDate date) {
        return service.schedule(actor, day(date));
    }

    /**
     * Xem khẩu phần ăn chi tiết (ngũ cốc, cỏ, vitamin, bữa ăn) của một con ngựa được giao.
     * Yêu cầu quyền: VIEW_FEEDING
     */
    @GetMapping("/horses/{id}/feeding-plan") 
    @PreAuthorize("hasAuthority('VIEW_FEEDING')")
    public List<FeedingPlanDto> feeding(
            @AuthenticationPrincipal Actor actor,
            @PathVariable int id,
            @RequestParam(required=false) LocalDate date) {
        return service.feeding(actor, id, day(date));
    }

    /**
     * Xem danh sách các công việc chăm sóc (cho ăn, tắm chải, dọn chuồng...) cần làm trong ngày.
     * Yêu cầu quyền: COMPLETE_CARE
     */
    @GetMapping("/tasks") 
    @PreAuthorize("hasAuthority('COMPLETE_CARE')")
    public PageDto<CareTaskDto> tasks(
            @AuthenticationPrincipal Actor actor,
            @RequestParam(required=false) LocalDate date,
            @RequestParam(required=false) Boolean completed,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return service.tasks(actor, day(date), completed, page, size);
    }

    /**
     * Xác nhận đã hoàn thành một công việc chăm sóc.
     * Chỉ cho phép xác nhận công việc được giao cho chính Groom đang đăng nhập.
     */
    @PostMapping("/tasks/{id}/complete") 
    @PreAuthorize("hasAuthority('COMPLETE_CARE')")
    public CareTaskDto complete(@AuthenticationPrincipal Actor actor, @PathVariable int id) {
        return service.complete(actor, id);
    }

    /**
     * Tạo mới một báo cáo sự cố bất thường về ngựa (chấn thương, bỏ ăn, đau ốm...).
     * Yêu cầu quyền: REPORT_INCIDENT
     */
    @PostMapping("/incidents") 
    @ResponseStatus(HttpStatus.CREATED) 
    @PreAuthorize("hasAuthority('REPORT_INCIDENT')")
    public IncidentDto incident(@AuthenticationPrincipal Actor actor, @Valid @RequestBody IncidentInput input) {
        return service.incident(actor, input);
    }

    /**
     * Xem danh sách các sự cố do chính Groom này đã báo cáo.
     */
    @GetMapping("/incidents") 
    @PreAuthorize("hasAuthority('REPORT_INCIDENT')")
    public PageDto<IncidentDto> incidents(
            @AuthenticationPrincipal Actor actor,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return service.incidents(actor, page, size);
    }

    /**
     * Xem chi tiết một báo cáo sự cố cụ thể.
     */
    @GetMapping("/incidents/{id}") 
    @PreAuthorize("hasAuthority('REPORT_INCIDENT')")
    public IncidentDto incident(@AuthenticationPrincipal Actor actor, @PathVariable int id) {
        return service.incident(actor, id);
    }

    /**
     * Tra cứu danh sách vật tư còn tồn trong kho để phục vụ chăm sóc ngựa (chỉ xem, không được xuất/nhập).
     * Yêu cầu quyền: VIEW_SUPPLIES
     */
    @GetMapping("/supplies") 
    @PreAuthorize("hasAuthority('VIEW_SUPPLIES')")
    public PageDto<SupplyDto> supplies(
            @RequestParam(defaultValue="") String search,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return manager.supplies(search, page, size);
    }

    /**
     * Hàm phụ trợ lấy ngày: Nếu client không truyền thì lấy ngày hiện tại của máy chủ.
     */
    private static LocalDate day(LocalDate date) {
        return date == null ? LocalDate.now() : date;
    }
}
