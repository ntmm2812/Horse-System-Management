package com.horsemanagement.dto.common;

import java.util.List;

/**
 * Cấu trúc dữ liệu phản hồi phân trang chung cho toàn bộ hệ thống (Generic Pagination Response):
 * @param <T>        Kiểu dữ liệu của từng phần tử trong danh sách
 * @param items      Danh sách các phần tử của trang hiện tại
 * @param total      Tổng số bản ghi trong cơ sở dữ liệu thỏa mãn điều kiện
 * @param page       Số thứ tự trang hiện tại (0-indexed)
 * @param size       Số lượng bản ghi tối đa trên một trang
 * @param totalPages Tổng số trang có thể xem
 */
public record PageDto<T>(List<T> items, long total, int page, int size, long totalPages) {

    /**
     * Phương thức tiện ích để khởi tạo đối tượng PageDto và tự động tính toán totalPages.
     */
    public static <T> PageDto<T> of(List<T> items, long total, int page, int size) {
        return new PageDto<>(List.copyOf(items), total, page, size, (total + size - 1) / size);
    }
}
