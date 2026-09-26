package com.horsemanagement.service;

import com.horsemanagement.dao.manager.HorseDao;
import com.horsemanagement.dao.manager.ReportDao;
import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.common.*;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.groom.*;
import com.horsemanagement.dto.manager.Requests.FinanceInput;
import com.horsemanagement.exception.ApiException;
import com.horsemanagement.security.Actor;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.horsemanagement.service.Pagination.offset;

/**
 * Service tổng hợp và trích xuất báo cáo (Reports):
 * - Báo cáo hiệu suất luyện tập và tỷ lệ hoàn thành công việc chăm sóc của đàn ngựa.
 * - Báo cáo tài chính doanh thu, chi phí, lợi nhuận ròng.
 * - Thêm mới bút toán thu/chi (Finance Entry) cho chuồng ngựa.
 */
@Service
public class ReportService {

    private final ReportDao reports;
    private final HorseDao horses;
    private final AuditService audit;

    public ReportService(ReportDao reports, HorseDao horses, AuditService audit) {
        this.reports = reports;
        this.horses = horses;
        this.audit = audit;
    }

    /**
     * Lấy báo cáo hiệu suất luyện tập và tỷ lệ hoàn thành công việc chăm sóc của đàn ngựa theo khoảng thời gian.
     */
    public PageDto<PerformanceDto> performance(LocalDate from, LocalDate to, int page, int size) {
        range(from, to);
        int skip = offset(page, size);
        return PageDto.of(reports.performance(from, to, skip, size), reports.countActiveHorses(), page, size);
    }

    /**
     * Tổng hợp tình hình tài chính (Thu, Chi, Lợi nhuận) và phân loại theo từng danh mục trong kỳ.
     */
    public FinanceReportDto finance(LocalDate from, LocalDate to) {
        range(from, to);
        var totals = reports.finance(from, to);
        return new FinanceReportDto(totals.income(), totals.expense(), totals.net(), totals.entryCount(), "VND", from, to, reports.categories(from, to));
    }

    /**
     * Danh sách phân trang các bút toán thu chi tài chính.
     */
    public PageDto<FinanceEntryDto> entries(LocalDate from, LocalDate to, int page, int size) {
        range(from, to);
        int skip = offset(page, size);
        return PageDto.of(reports.entries(from, to, skip, size), reports.countEntries(from, to), page, size);
    }

    /**
     * Tạo một bút toán thu hoặc chi tài chính mới.
     * @param actor người dùng đang thực hiện (Quản lý)
     * @param input thông tin thu/chi, số tiền, danh mục, liên kết ngựa (nếu có)
     */
    @Transactional
    public CreatedEntryDto createEntry(Actor actor, FinanceInput input) {
        if (input.horseId() != null) {
            horses.findActiveById(input.horseId());
        }
        int id = reports.insert(actor.userId(), input);
        audit.record(actor.userId(), "CREATE", "FinanceEntry", id);
        return new CreatedEntryDto(id);
    }

    /**
     * Kiểm tra tính hợp lệ của khoảng ngày (từ ngày <= đến ngày).
     */
    private static void range(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new ApiException(400, "Ngày kết thúc phải từ ngày bắt đầu trở đi.");
        }
    }
}
