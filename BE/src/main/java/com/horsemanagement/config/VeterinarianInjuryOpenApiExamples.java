package com.horsemanagement.config;

/** Safe OpenAPI examples; none imply that a write should be sent to the shared database. */
public final class VeterinarianInjuryOpenApiExamples {
    private VeterinarianInjuryOpenApiExamples() {}

    public static final String INJURY_LIST = """
        {"content":[{"injuryId":1,"recordId":1,"horseId":3,"horseName":"Hồng Lôi",
        "bodyPart":"Chân trước trái","bodySystem":"MUSCLE","severity":"MODERATE",
        "status":"RECOVERING","occurredDate":"2026-10-03","healedDate":null}],
        "page":0,"size":20,"totalElements":1,"totalPages":1,"first":true,"last":true}
        """;
    public static final String INJURY_DETAIL = """
        {"injuryId":1,"recordId":1,"horseId":3,"horseName":"Hồng Lôi",
        "bodyPart":"Chân trước trái","bodySystem":"MUSCLE","modelMeshId":"front_left_leg",
        "positionX":0.2000,"positionY":0.5000,"positionZ":0.1000,
        "injuryType":"Chấn thương phần mềm","severity":"MODERATE","status":"RECOVERING",
        "occurredDate":"2026-10-03","healedDate":null,"description":"Theo dõi phục hồi"}
        """;
    public static final String CREATE_INJURY = """
        {"recordId":1,"bodyPart":"Chân trước trái","bodySystem":"MUSCLE",
        "modelMeshId":"front_left_leg","positionX":0.2000,"positionY":0.5000,
        "positionZ":0.1000,"injuryType":"Chấn thương phần mềm","severity":"MODERATE",
        "occurredDate":"2026-10-03","description":"Theo dõi phục hồi"}
        """;
    public static final String PATCH_INJURY = """
        {"status":"HEALED","healedDate":"2026-10-15","description":"Đã xác nhận lành"}
        """;
    public static final String PROGRESS_LIST = """
        {"content":[{"progressId":2,"injuryId":1,"loggedBy":3,"loggedByName":"Lê Thu Hà",
        "logDate":"2026-10-08T09:00:00","recoveryPercent":50,"painLevel":2,
        "notes":"Tiếp tục theo dõi","imageUrl":null}],"page":0,"size":20,
        "totalElements":1,"totalPages":1,"first":true,"last":true}
        """;
    public static final String CREATE_PROGRESS = """
        {"loggedBy":3,"logDate":"2026-10-08T09:00:00","recoveryPercent":50,
        "painLevel":2,"notes":"Tiếp tục theo dõi","imageUrl":null}
        """;
    public static final String PROGRESS_CREATED = """
        {"progressId":2,"injuryId":1,"loggedBy":3,"loggedByName":"Lê Thu Hà",
        "logDate":"2026-10-08T09:00:00","recoveryPercent":50,"painLevel":2,
        "notes":"Tiếp tục theo dõi","imageUrl":null}
        """;
}
