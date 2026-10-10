package com.horsemanagement.config;

/** Documentation examples only; no request is sent to the shared database. */
public final class VeterinarianTreatmentOpenApiExamples {
    private VeterinarianTreatmentOpenApiExamples() {
    }

    public static final String TREATMENT_LIST = """
        {"content":[{"treatmentId":1,"recordId":1,"horseId":3,"horseName":"Hồng Lôi",\
        "description":"Rest and monitor progress","startDate":"2026-10-03",\
        "endDate":"2026-10-17","status":"ACTIVE","createdAt":"2026-10-03T08:30:00"}],\
        "page":0,"size":20,"totalElements":1,"totalPages":1,"first":true,"last":true}
        """;

    public static final String TREATMENT_DETAIL = """
        {"treatmentId":1,"recordId":1,"horseId":3,"horseName":"Hồng Lôi",\
        "description":"Rest and monitor progress","startDate":"2026-10-03",\
        "endDate":"2026-10-17","status":"ACTIVE","createdAt":"2026-10-03T08:30:00",\
        "prescriptions":[{"prescriptionItemId":1,"treatmentId":1,"supplyId":4,\
        "supplyName":"Example medicine","dosage":"Per veterinarian order",\
        "frequency":"Per written care schedule","durationDays":7,"route":"ORAL",\
        "instructions":"Follow veterinarian instructions"}]}
        """;

    public static final String CREATE_TREATMENT = """
        {"recordId":1,"description":"Rest and monitor progress",\
        "startDate":"2026-10-03","endDate":"2026-10-17"}
        """;

    public static final String PATCH_TREATMENT = """
        {"description":"Continue monitoring","endDate":"2026-10-24","status":"COMPLETED"}
        """;

    public static final String PRESCRIPTION_LIST = """
        [{"prescriptionItemId":1,"treatmentId":1,"supplyId":4,\
        "supplyName":"Example medicine","dosage":"Per veterinarian order",\
        "frequency":"Per written care schedule","durationDays":7,"route":"ORAL",\
        "instructions":"Follow veterinarian instructions"}]
        """;

    public static final String CREATE_PRESCRIPTION = """
        {"supplyId":4,"dosage":"Per veterinarian order",\
        "frequency":"Per written care schedule","durationDays":7,"route":"ORAL",\
        "instructions":"Follow veterinarian instructions"}
        """;

    public static final String PRESCRIPTION_CREATED = """
        {"prescriptionItemId":42,"treatmentId":1,"supplyId":4,\
        "supplyName":"Example medicine","dosage":"Per veterinarian order",\
        "frequency":"Per written care schedule","durationDays":7,"route":"ORAL",\
        "instructions":"Follow veterinarian instructions"}
        """;
}
