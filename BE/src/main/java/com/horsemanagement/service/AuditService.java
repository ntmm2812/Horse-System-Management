package com.horsemanagement.service;

import com.horsemanagement.dao.manager.AuditDao;
import org.springframework.stereotype.Service;

/**
 * Service ghi nhận nhật ký kiểm toán hệ thống (Audit Log):
 * - Ghi lại mọi hành động quan trọng (Thêm, Sửa, Xóa, Phê duyệt, Đính kèm ảnh).
 * - Phục vụ việc truy vết, bảo mật và đối soát của Quản lý chuồng ngựa.
 */
@Service
public class AuditService {

    private final AuditDao audit;

    public AuditService(AuditDao audit) {
        this.audit = audit;
    }

    /**
     * Ghi một bản ghi nhật ký kiểm toán.
     * @param userId   ID người dùng thực hiện thao tác
     * @param action   Hành động thực hiện (CREATE, UPDATE, DELETE, v.v.)
     * @param table    Tên bảng / đối tượng bị tác động (Horse, User, Supply, IncidentReport, v.v.)
     * @param targetId ID của đối tượng bị tác động
     */
    public void record(int userId, String action, String table, int targetId) {
        audit.insert(userId, action, table, targetId);
    }
}
