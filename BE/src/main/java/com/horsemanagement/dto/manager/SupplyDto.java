package com.horsemanagement.dto.manager;

/**
 * DTO thông tin vật tư trong kho (dbo.Supply):
 * @param supplyId        ID vật tư
 * @param itemName        Tên vật tư (Thức ăn, Yên cương, Thuốc men, v.v.)
 * @param type            Phân loại vật tư
 * @param quantityInStock Số lượng tồn kho hiện tại
 * @param managedBy       ID người quản lý chịu trách nhiệm
 * @param managerName     Tên người quản lý
 * @param version         Phiên bản phục vụ khóa lạc quan (Optimistic Locking)
 */
public record SupplyDto(int supplyId, String itemName, String type, int quantityInStock,
                       int managedBy, String managerName, int version) {}
