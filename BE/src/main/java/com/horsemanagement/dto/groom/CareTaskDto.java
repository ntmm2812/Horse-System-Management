package com.horsemanagement.dto.groom;

import java.time.LocalDate;

/**
 * DTO nhiệm vụ chăm sóc ngựa của Groom:
 * @param taskId    ID nhiệm vụ (dbo.CARETASK)
 * @param horseId   ID chú ngựa cần chăm sóc
 * @param horseName Tên chú ngựa
 * @param taskType  Loại công việc (Chải lông, Tắm rửa, Cho ăn, Vệ sinh móng, v.v.)
 * @param completed Trạng thái: true (đã xong), false (chưa làm)
 * @param date      Ngày thực hiện
 */
public record CareTaskDto(int taskId, int horseId, String horseName, String taskType, boolean completed, LocalDate date) {}
