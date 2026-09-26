package com.horsemanagement.service;

import com.horsemanagement.exception.ApiException;
import com.horsemanagement.dao.groom.IncidentDao;
import com.horsemanagement.security.Actor;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service xử lý đính kèm và xem ảnh báo cáo sự cố (Incident Report Image):
 * - Kiểm tra quyền sở hữu báo cáo của Groom
 * - Xác thực tệp ảnh an toàn: kích thước <= 5MB, độ phân giải <= 16 triệu pixels, đúng định dạng PNG/JPEG
 * - Mã hóa lại ảnh thành file PNG với tên tệp UUID ngẫu nhiên để chống tấn công Path Traversal / Malicious Upload
 * - Cơ chế TransactionSynchronization tự động xóa file trên đĩa nếu giao dịch cơ sở dữ liệu bị rollback
 */
@Service
public class IncidentImageService {

    private final IncidentDao incidents;
    private final AuditService audit;
    private final Path directory;

    public IncidentImageService(IncidentDao incidents, AuditService audit, @Value("${app.upload-dir}") String directory) {
        this.incidents = incidents;
        this.audit = audit;
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    /**
     * Tải lên và đính kèm ảnh vào báo cáo sự cố của Groom.
     * @param actor     nhân viên Groom đang đăng nhập
     * @param reportId  ID của báo cáo sự cố
     * @param file      file ảnh tải lên từ client
     */
    @Transactional
    public void upload(Actor actor, int reportId, MultipartFile file) throws IOException {
        // Kiểm tra xem báo cáo có thuộc về Groom này hay không
        var row = incidents.findOwned(actor.userId(), reportId);
        if (row.imagePath() != null) {
            throw ApiException.conflict("Báo cáo đã có ảnh đính kèm.");
        }
        if (file.isEmpty()) {
            throw new ApiException(400, "Ảnh không được rỗng.");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ApiException(413, "Ảnh tối đa 5 MB.");
        }

        BufferedImage image;
        // Đọc và kiểm tra tính hợp lệ của stream dữ liệu ảnh
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(file.getBytes()))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new ApiException(400, "Chỉ nhận ảnh PNG hoặc JPEG hợp lệ.");
            }
            var reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!java.util.Set.of("png", "jpeg", "jpg").contains(format)) {
                    throw new ApiException(400, "Chỉ nhận ảnh PNG hoặc JPEG.");
                }
                reader.setInput(input, true, true);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > 16000000L) {
                    throw new ApiException(400, "Ảnh tối đa 16 triệu điểm ảnh.");
                }
                image = reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (javax.imageio.IIOException ex) {
            throw new ApiException(400, "Dữ liệu ảnh bị lỗi.");
        }

        // Tạo thư mục lưu trữ nếu chưa có
        Files.createDirectories(directory);
        String key = UUID.randomUUID() + ".png";
        Path target = directory.resolve(key);

        // Lưu ảnh mới đã qua mã hóa lại (re-encoding)
        try {
            ImageIO.write(image, "png", target.toFile());
            if (incidents.attachImage(actor.userId(), reportId, key) != 1) {
                throw ApiException.conflict("Báo cáo vừa được cập nhật ảnh.");
            }
            // Ghi nhật ký kiểm toán (Audit log)
            audit.record(actor.userId(), "ATTACH_IMAGE", "INCIDENTREPORT", reportId);

            // Đồng bộ Transaction: nếu commit thất bại thì tự động xóa file trên đĩa
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        try {
                            Files.deleteIfExists(target);
                        } catch (IOException ignored) {}
                    }
                }
            });
        } catch (IOException | RuntimeException ex) {
            Files.deleteIfExists(target);
            throw ex;
        }
    }

    /**
     * Tải tài nguyên file ảnh để xem hoặc tải xuống.
     * @param actor    người dùng đang yêu cầu (Admin hoặc Groom)
     * @param reportId ID báo cáo sự cố
     * @param manager  true nếu là vai trò Manager/Admin, false nếu là Groom
     * @return Resource để xuất file qua response HTTP
     */
    public Resource load(Actor actor, int reportId, boolean manager) {
        var row = manager ? incidents.findById(reportId) : incidents.findOwned(actor.userId(), reportId);
        String key = (String) row.imagePath();
        if (key == null || !key.matches("[a-f0-9-]{36}\\.png")) {
            throw ApiException.notFound();
        }
        Path path = directory.resolve(key).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) {
            throw ApiException.notFound();
        }
        return new FileSystemResource(path);
    }
}
