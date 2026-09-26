package com.horsemanagement.service;

import com.horsemanagement.dao.groom.GroomDao;
import com.horsemanagement.dao.groom.IncidentDao;
import com.horsemanagement.dao.groom.IncidentDao.IncidentRow;
import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.common.*;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.groom.*;
import com.horsemanagement.exception.ApiException;
import com.horsemanagement.security.Actor;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.horsemanagement.service.Pagination.offset;

/**
 * Service xử lý toàn bộ nghiệp vụ của Groom (Nhân viên chuồng trại):
 * - Tra cứu chuồng trại và ô chuồng được phân công trong ngày
 * - Tra cứu lịch sinh hoạt và khẩu phần ăn của ngựa
 * - Xác nhận hoàn thành công việc chăm sóc
 * - Tạo và tra cứu báo cáo sự cố ngựa
 */
@Service
public class GroomService {
    private final GroomDao groom;
    private final IncidentDao incidents;
    private final AuditService audit;

    public GroomService(GroomDao groom, IncidentDao incidents, AuditService audit) {
        this.groom = groom;
        this.incidents = incidents;
        this.audit = audit;
    }

    /**
     * Lấy danh sách chuồng trại và ô chuồng của các con ngựa mà Groom đang đăng nhập được giao trong ngày.
     */
    public PageDto<StableDto> stables(Actor actor, LocalDate date, int page, int size) {
        int skip = offset(page, size);
        return PageDto.of(groom.stables(actor.userId(), date, skip, size), groom.countStables(actor.userId(), date), page, size);
    }

    /**
     * Xem khẩu phần ăn chi tiết của ngựa:
     * Kiểm tra đảm bảo con ngựa này phải được phân công cho Groom trong ngày yêu cầu.
     */
    public List<FeedingPlanDto> feeding(Actor actor, int horseId, LocalDate date) {
        requireAssignment(actor, horseId, date);
        return groom.feeding(horseId);
    }

    /**
     * Xem danh sách các công việc chăm sóc (Care Tasks) được giao cho Groom theo ngày.
     */
    public PageDto<CareTaskDto> tasks(Actor actor, LocalDate date, Boolean completed, int page, int size) {
        int skip = offset(page, size);
        return PageDto.of(groom.tasks(actor.userId(), date, completed, skip, size), groom.countTasks(actor.userId(), date, completed), page, size);
    }

    /**
     * Xem lịch trình hoạt động/tập luyện trong ngày của các con ngựa Groom phụ trách.
     */
    public List<ScheduleDto> schedule(Actor actor, LocalDate date) { 
        return groom.schedule(actor.userId(), date); 
    }

    /**
     * Xác nhận hoàn thành một công việc chăm sóc:
     * - Kiểm tra đúng công việc của Groom đăng nhập.
     * - Không cho phép hoàn thành trước ngày diễn ra công việc.
     * - Ghi lại nhật ký kiểm toán (COMPLETE).
     */
    @Transactional
    public CareTaskDto complete(Actor actor, int id) {
        var task = groom.task(actor.userId(), id);
        if (task.date().isAfter(LocalDate.now())) {
            throw ApiException.conflict("Chưa đến ngày thực hiện công việc.");
        }
        if (groom.complete(actor.userId(), id) == 1) {
            audit.record(actor.userId(), "COMPLETE", "CARETASK", id);
        }
        return groom.task(actor.userId(), id);
    }

    /**
     * Tạo báo cáo sự cố ngựa:
     * - Kiểm tra con ngựa gặp sự cố phải nằm trong danh sách phân công của Groom trong ngày đó.
     * - Ghi lại nhật ký kiểm toán (CREATE).
     */
    @Transactional
    public IncidentDto incident(Actor actor, IncidentInput input) {
        requireAssignment(actor, input.horseId(), input.date());
        int id = incidents.insert(actor.userId(), input);
        audit.record(actor.userId(), "CREATE", "INCIDENTREPORT", id);
        return incident(actor, id);
    }

    /**
     * Xem chi tiết sự cố do chính Groom đăng nhập báo cáo.
     */
    public IncidentDto incident(Actor actor, int id) { 
        return response(incidents.findOwned(actor.userId(), id), "groom"); 
    }

    /**
     * Danh sách các sự cố do chính Groom này đã tạo.
     */
    public PageDto<IncidentDto> incidents(Actor actor, int page, int size) {
        int skip = offset(page, size);
        return PageDto.of(incidents.findOwned(actor.userId(), skip, size).stream().map(r -> response(r, "groom")).toList(),
                incidents.countOwned(actor.userId()), page, size);
    }

    /**
     * Xem danh sách toàn bộ sự cố trên hệ thống (Dành riêng cho Club Manager).
     */
    public PageDto<IncidentDto> managerIncidents(int page, int size) {
        int skip = offset(page, size);
        return PageDto.of(incidents.findAll(skip, size).stream().map(r -> response(r, "manager")).toList(),
                incidents.countAll(), page, size);
    }

    /**
     * Xem chi tiết sự cố bất kỳ (Dành riêng cho Club Manager).
     */
    public IncidentDto managerIncident(int id) { 
        return response(incidents.findById(id), "manager"); 
    }

    /**
     * Định dạng đường dẫn imageUrl tương đối theo vai trò (groom hoặc manager).
     */
    private static IncidentDto response(IncidentRow row, String scope) {
        String image = row.imagePath() == null ? null : ("/api/" + scope + "/incidents/" + row.reportId() + "/image");
        return new IncidentDto(row.reportId(), row.horseId(), row.horseName(), row.groomId(), row.incidentType(), row.description(), row.date(), image);
    }

    /**
     * Kiểm tra quyền phân công: Đảm bảo Groom được giao phụ trách con ngựa này trong ngày chỉ định.
     */
    private void requireAssignment(Actor actor, int horseId, LocalDate date) {
        if (!groom.isAssigned(actor.userId(), horseId, date)) {
            throw ApiException.notFound();
        }
    }
}
