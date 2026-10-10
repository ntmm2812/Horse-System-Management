package com.horsemanagement.config;

/** Compile-time JSON examples for the six existing Veterinarian operations. */
public final class VeterinarianOpenApiExamples {
    private VeterinarianOpenApiExamples() {
    }

    public static final String HORSE_LIST = """
        {"content":[{"horseId":3,"name":"Hồng Lôi","breed":"Thoroughbred",\
        "gender":"GELDING","healthStatus":"INJURED","readinessStatus":"RESTING",\
        "trainingLocked":true,"ownerName":"Bùi Thanh Tâm","managerName":"Trần Quốc Huy"}],\
        "page":0,"size":20,"totalElements":1,"totalPages":1,"first":true,"last":true}
        """;

    public static final String HORSE_DETAIL = """
        {"horseId":3,"name":"Hồng Lôi","registrationNo":"DEMO-REG-003",\
        "microchipNo":"DEMO-CHIP-003","breed":"Thoroughbred","gender":"GELDING",\
        "color":"Nâu","dateOfBirth":"2021-03-15","countryOfOrigin":"Việt Nam",\
        "heightCm":162.0,"currentWeightKg":510.00,"healthStatus":"INJURED",\
        "readinessStatus":"RESTING","trainingLocked":true,"status":"ACTIVE",\
        "photoUrl":null,"ownerName":"Bùi Thanh Tâm","managerName":"Trần Quốc Huy",\
        "latestHealthMetric":null}
        """;

    public static final String METRIC_LIST = """
        {"content":[{"metricId":2,"weightKg":510.00,"restingHeartRate":36,\
        "bodyTemperature":37.5,"respiratoryRate":12,"recordedAt":"2026-10-08T06:00:00",\
        "notes":"Routine measurement"}],"page":0,"size":20,"totalElements":1,\
        "totalPages":1,"first":true,"last":true}
        """;

    public static final String MEDICAL_LIST = """
        {"content":[{"recordId":1,"horseId":3,"horseName":"Hồng Lôi",\
        "vetId":3,"vetName":"Lê Thu Hà","examDate":"2026-10-03T08:00:00",\
        "reason":"Follow-up examination","healthStatusAfter":"INJURED"}],\
        "page":0,"size":20,"totalElements":1,"totalPages":1,"first":true,"last":true}
        """;

    public static final String MEDICAL_DETAIL = """
        {"recordId":1,"horseId":3,"horseName":"Hồng Lôi","vetId":3,\
        "vetName":"Lê Thu Hà","incidentId":1,"examDate":"2026-10-03T08:00:00",\
        "reason":"Follow-up examination","symptoms":"Uneven gait",\
        "diagnosis":"Soft tissue injury","healthStatusAfter":"INJURED",\
        "notes":"Example medical note"}
        """;

    public static final String MEDICAL_CREATE_REQUEST = """
        {"horseId":3,"vetId":3,"incidentId":null,"examDate":"2026-10-10T09:00:00",\
        "reason":"Routine examination","symptoms":"Normal appetite",\
        "diagnosis":"No acute concern","healthStatusAfter":"MONITORING",\
        "notes":"Example request; do not submit to the shared database"}
        """;

    public static final String MEDICAL_CREATED = """
        {"recordId":42,"horseId":3,"horseName":"Hồng Lôi","vetId":3,\
        "vetName":"Lê Thu Hà","incidentId":null,"examDate":"2026-10-10T09:00:00",\
        "reason":"Routine examination","symptoms":"Normal appetite",\
        "diagnosis":"No acute concern","healthStatusAfter":"MONITORING",\
        "notes":"Example response"}
        """;
}
