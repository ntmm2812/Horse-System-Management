package com.horsemanagement.service;

import com.horsemanagement.exception.ApiException;

/**
 * Tiện ích hỗ trợ phân trang (Pagination Utility):
 * - Kiểm tra tính hợp lệ của tham số page (trang hiện tại, 0-indexed) và size (số phần tử trên mỗi trang).
 * - Tính toán số dòng cần bỏ qua (OFFSET) trong câu truy vấn SQL.
 */
public final class Pagination {

    private Pagination() {}

    /**
     * Tính toán giá trị OFFSET cho SQL: offset = page * size.
     * @param page số thứ tự trang (bắt đầu từ 0)
     * @param size số lượng bản ghi trên một trang (từ 1 đến 100)
     * @return số dòng cần bỏ qua
     */
    public static int offset(int page, int size) {
        if (page < 0 || page > 100000 || size < 1 || size > 100) {
            throw new ApiException(400, "page phải từ 0 đến 100000; size từ 1 đến 100.");
        }
        return page * size;
    }
}
