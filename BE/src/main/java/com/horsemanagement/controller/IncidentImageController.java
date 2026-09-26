package com.horsemanagement.controller;

import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.common.*;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.groom.*;
import com.horsemanagement.security.Actor;
import com.horsemanagement.service.GroomService;
import com.horsemanagement.service.IncidentImageService;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xử lý upload và truy xuất ảnh sự cố ngựa:
 * - Groom upload ảnh đính kèm cho sự cố của mình
 * - Groom và Manager xem ảnh sự cố đã lưu
 * - Manager duyệt danh sách tất cả các sự cố do các Groom báo cáo
 */
@RestController
public class IncidentImageController {
    private final IncidentImageService images;
    private final GroomService service;

    public IncidentImageController(IncidentImageService images, GroomService service) {
        this.images = images;
        this.service = service;
    }

    /**
     * Groom upload ảnh chụp hiện trường sự cố (chỉ chấp nhận JPEG/PNG, tối đa 5MB).
     * Yêu cầu quyền: REPORT_INCIDENT
     */
    @PostMapping(value = "/api/groom/incidents/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    @PreAuthorize("hasAuthority('REPORT_INCIDENT')")
    public void upload(
            @AuthenticationPrincipal Actor actor,
            @PathVariable int id,
            @RequestPart("file") MultipartFile file) throws IOException {
        images.upload(actor, id, file);
    }

    /**
     * Groom tải/xem ảnh của sự cố do chính mình báo cáo.
     */
    @GetMapping("/api/groom/incidents/{id}/image") 
    @PreAuthorize("hasAuthority('REPORT_INCIDENT')")
    public ResponseEntity<Resource> groomImage(@AuthenticationPrincipal Actor actor, @PathVariable int id) {
        return response(images.load(actor, id, false));
    }

    /**
     * Club Manager xem ảnh của sự cố (Manager có quyền xem ảnh sự cố của toàn bộ câu lạc bộ).
     */
    @GetMapping("/api/manager/incidents/{id}/image") 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public ResponseEntity<Resource> managerImage(@AuthenticationPrincipal Actor actor, @PathVariable int id) {
        return response(images.load(actor, id, true));
    }

    /**
     * Club Manager xem danh sách toàn bộ các sự cố của tất cả các ngựa trong câu lạc bộ.
     */
    @GetMapping("/api/manager/incidents") 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public PageDto<IncidentDto> incidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.managerIncidents(page, size);
    }

    /**
     * Club Manager xem chi tiết một sự cố cụ thể.
     */
    @GetMapping("/api/manager/incidents/{id}") 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public IncidentDto incident(@PathVariable int id) {
        return service.managerIncident(id);
    }

    /**
     * Đóng gói ảnh trả về dưới dạng ResponseEntity với Content-Type image/png và không lưu cache trình duyệt.
     */
    private ResponseEntity<Resource> response(Resource resource) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .body(resource);
    }
}
