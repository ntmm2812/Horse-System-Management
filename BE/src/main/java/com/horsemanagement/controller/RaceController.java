package com.horsemanagement.controller;

import com.horsemanagement.dao.manager.RaceDao;
import com.horsemanagement.dto.manager.RaceDto.*;
import com.horsemanagement.dto.manager.Requests.*;
import com.horsemanagement.exception.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * FILE: RaceController.java — API quản lý Cuộc đua & Kết quả thi đấu
 * BASE URL: /api/manager/races
 * QUYỀN: MANAGE_RACES (gán cho role Admin/Manager)
 *
 * DANH SÁCH ENDPOINT:
 *   GET    /api/manager/races                          → Danh sách tất cả cuộc đua
 *   POST   /api/manager/races                          → Tạo cuộc đua mới
 *   GET    /api/manager/races/{raceId}                 → Chi tiết cuộc đua + kết quả
 *   PUT    /api/manager/races/{raceId}                 → Sửa thông tin cuộc đua
 *   DELETE /api/manager/races/{raceId}                 → Xóa cuộc đua (nếu chưa có kết quả)
 *   POST   /api/manager/races/{raceId}/registrations   → Đăng ký ngựa tham gia
 *   DELETE /api/manager/races/{raceId}/registrations/{horseId} → Hủy đăng ký
 *   PUT    /api/manager/races/{raceId}/results/{horseId}  → Nhập / Cập nhật kết quả 1 ngựa
 *   DELETE /api/manager/races/{raceId}/results/{horseId}  → Xóa kết quả (reset về chưa có)
 * ============================================================
 */
@RestController
@RequestMapping("/api/manager/races")
public class RaceController {

    private final RaceDao raceDao;

    public RaceController(RaceDao raceDao) {
        this.raceDao = raceDao;
    }

    // ──────────────────────────────────────────────
    // PHẦN 1: CRUD CUỘC ĐUA
    // ──────────────────────────────────────────────

    /**
     * Lấy danh sách tất cả cuộc đua.
     * Trả về: RaceID, tên, ngày, giờ, địa điểm, cự ly, số ngựa đăng ký, cờ có kết quả chưa.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public List<RaceInfo> listRaces() {
        return raceDao.listRaces();
    }

    /**
     * Xem chi tiết 1 cuộc đua kèm bảng kết quả toàn bộ ngựa tham gia.
     * Trả về 404 nếu không tìm thấy.
     */
    @GetMapping("/{raceId}")
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public RaceWithResultsDto getRace(@PathVariable int raceId) {
        return raceDao.raceWithResults(raceId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy cuộc đua ID: " + raceId));
    }

    /**
     * Tạo mới một cuộc đua.
     * Body: { raceName, date, time, venue?, distanceMeters? }
     * Trả về 201 Created kèm RaceID vừa tạo.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public java.util.Map<String, Integer> createRace(@Valid @RequestBody RaceInput input) {
        int raceId = raceDao.createRace(input);
        return java.util.Map.of("raceId", raceId);
    }

    /**
     * Cập nhật thông tin cuộc đua (tên, ngày, giờ, địa điểm, cự ly).
     * Nên chỉ gọi trước khi nhập kết quả.
     */
    @PutMapping("/{raceId}")
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public java.util.Map<String, String> updateRace(
            @PathVariable int raceId, @Valid @RequestBody RaceInput input) {
        int affected = raceDao.updateRace(raceId, input);
        if (affected == 0) throw ApiException.notFound("Không tìm thấy cuộc đua ID: " + raceId);
        return java.util.Map.of("message", "Cập nhật thông tin cuộc đua thành công.");
    }

    /**
     * Xóa cuộc đua (chỉ nếu chưa nhập kết quả — kiểm tra trong ràng buộc DB).
     * Trả về 204 No Content khi thành công.
     */
    @DeleteMapping("/{raceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public void deleteRace(@PathVariable int raceId) {
        int affected = raceDao.deleteRace(raceId);
        if (affected == 0) throw ApiException.notFound("Không tìm thấy cuộc đua ID: " + raceId);
    }

    // ──────────────────────────────────────────────
    // PHẦN 2: ĐĂNG KÝ NGỰA THAM GIA
    // ──────────────────────────────────────────────

    /**
     * Đăng ký 1 ngựa vào cuộc đua.
     * Body: { horseId }
     * Idempotent — không lỗi nếu đã đăng ký rồi.
     */
    @PostMapping("/{raceId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public java.util.Map<String, String> registerHorse(
            @PathVariable int raceId, @Valid @RequestBody RaceRegistrationInput input) {
        if (!raceDao.raceExists(raceId))
            throw ApiException.notFound("Không tìm thấy cuộc đua ID: " + raceId);
        raceDao.registerHorse(raceId, input.horseId());
        return java.util.Map.of("message", "Đã đăng ký ngựa vào cuộc đua.");
    }

    /**
     * Hủy đăng ký ngựa khỏi cuộc đua.
     * Chỉ cho phép nếu ngựa đó chưa có kết quả thi đấu được nhập.
     * Trả về 404 nếu không tìm thấy hoặc đã có kết quả.
     */
    @DeleteMapping("/{raceId}/registrations/{horseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public void cancelRegistration(@PathVariable int raceId, @PathVariable int horseId) {
        int affected = raceDao.cancelRegistration(raceId, horseId);
        if (affected == 0)
            throw ApiException.badRequest(
                "Không thể hủy đăng ký: ngựa không tồn tại trong cuộc đua hoặc đã có kết quả.");
    }

    // ──────────────────────────────────────────────
    // PHẦN 3: NHẬP & CẬP NHẬT KẾT QUẢ THI ĐẤU
    // ──────────────────────────────────────────────

    /**
     * Nhập hoặc cập nhật kết quả thi đấu cho 1 ngựa trong 1 cuộc đua.
     * Body: { position, finishTime?, prizeMoney? }
     * - position   : Vị trí xếp hạng (1 = nhất), bắt buộc.
     * - finishTime : Định dạng "mm:ss.sss" (ví dụ "02:35.420"), tùy chọn.
     * - prizeMoney : Tiền thưởng VNĐ (số thực ≥ 0), tùy chọn.
     */
    @PutMapping("/{raceId}/results/{horseId}")
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public java.util.Map<String, String> upsertResult(
            @PathVariable int raceId,
            @PathVariable int horseId,
            @Valid @RequestBody RaceResultInput input) {
        // Kiểm tra cặp (HorseID, RaceID) phải tồn tại trước khi nhập kết quả
        if (!raceDao.registrationExists(raceId, horseId))
            throw ApiException.notFound(
                "Ngựa ID " + horseId + " chưa đăng ký vào cuộc đua ID " + raceId);
        int affected = raceDao.upsertResult(raceId, horseId, input);
        if (affected == 0)
            throw ApiException.notFound("Không thể cập nhật kết quả. Kiểm tra lại RaceID và HorseID.");
        return java.util.Map.of("message",
                "Đã lưu kết quả: Ngựa #" + horseId + " xếp vị trí " + input.position());
    }

    /**
     * Xóa kết quả thi đấu của 1 ngựa (đặt lại Position/FinishTime/PrizeMoney về NULL).
     * Dùng khi nhập sai cần sửa lại từ đầu.
     */
    @DeleteMapping("/{raceId}/results/{horseId}")
    @PreAuthorize("hasAuthority('MANAGE_RACES')")
    public java.util.Map<String, String> clearResult(
            @PathVariable int raceId, @PathVariable int horseId) {
        int affected = raceDao.clearResult(raceId, horseId);
        if (affected == 0)
            throw ApiException.notFound("Không tìm thấy kết quả để xóa.");
        return java.util.Map.of("message", "Đã xóa kết quả của ngựa #" + horseId + " trong cuộc đua #" + raceId);
    }
}
