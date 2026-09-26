package com.horsemanagement.dto.manager;

import java.math.BigDecimal;

/**
 * DTO thông tin đầy đủ của một chú ngựa (dbo.Horse):
 * @param horseId      ID định danh của chú ngựa
 * @param name         Tên chú ngựa
 * @param gender       Giới tính (Stallion, Mare, Gelding...)
 * @param age          Tuổi
 * @param weight       Cân nặng (kg)
 * @param lineage      Gia phả, dòng dõi
 * @param ownerId      ID chủ sở hữu
 * @param ownerName    Họ tên chủ sở hữu
 * @param archived     Trạng thái lưu trữ / xóa mềm (true: đã xóa mềm)
 * @param healthStatus Tình trạng sức khỏe hiện tại (Healthy, Sick, Recovering, Unknown...)
 */
public record HorseDto(int horseId, String name, String gender, int age, BigDecimal weight,
                      String lineage, int ownerId, String ownerName, boolean archived, String healthStatus) {}
