package com.horsemanagement.config;

/** Examples document the contract; POST/PATCH examples are not run on the shared database. */
public final class VeterinarianTrainingLockOpenApiExamples {
    private VeterinarianTrainingLockOpenApiExamples() {}

    public static final String LOCK = """
        {"lockId":1,"horseId":3,"horseName":"Hồng Lôi","injuryId":1,
        "injuryBodyPart":"Chân trước trái","lockedBy":3,"lockedByName":"Lê Thu Hà",
        "lockLevel":"FULL","reason":"Tạm nghỉ để theo dõi hồi phục",
        "lockedAt":"2026-10-03T08:30:00","expectedEndAt":"2026-10-17T08:30:00",
        "releasedAt":null,"releasedBy":null,"releasedByName":null,"status":"ACTIVE"}
        """;
    public static final String LOCK_LIST = """
        {"content":[{"lockId":1,"horseId":3,"horseName":"Hồng Lôi","injuryId":1,
        "injuryBodyPart":"Chân trước trái","lockedBy":3,"lockedByName":"Lê Thu Hà",
        "lockLevel":"FULL","reason":"Tạm nghỉ để theo dõi hồi phục",
        "lockedAt":"2026-10-03T08:30:00","expectedEndAt":"2026-10-17T08:30:00",
        "releasedAt":null,"releasedBy":null,"releasedByName":null,"status":"ACTIVE"}],
        "page":0,"size":20,"totalElements":1,"totalPages":1,"first":true,"last":true}
        """;
    public static final String CREATE = """
        {"horseId":3,"lockedBy":3,"lockLevel":"FULL",
        "reason":"Tạm nghỉ để theo dõi hồi phục","injuryId":1,
        "expectedEndAt":"2026-10-17T08:30:00"}
        """;
    public static final String RELEASE = """
        {"releasedBy":3}
        """;
    public static final String RELEASED_LOCK = """
        {"lockId":1,"horseId":3,"horseName":"Hồng Lôi","injuryId":1,
        "injuryBodyPart":"Chân trước trái","lockedBy":3,"lockedByName":"Lê Thu Hà",
        "lockLevel":"FULL","reason":"Tạm nghỉ để theo dõi hồi phục",
        "lockedAt":"2026-10-03T08:30:00","expectedEndAt":"2026-10-17T08:30:00",
        "releasedAt":"2026-10-15T10:00:00","releasedBy":3,
        "releasedByName":"Lê Thu Hà","status":"RELEASED"}
        """;
    public static final String ELIGIBILITY = """
        {"horseId":3,"horseName":"Hồng Lôi","healthStatus":"INJURED",
        "readinessStatus":"RESTING","isTrainingLocked":true,
        "activeLocks":[{"lockId":1,"horseId":3,"horseName":"Hồng Lôi",
        "injuryId":1,"injuryBodyPart":"Chân trước trái","lockedBy":3,
        "lockedByName":"Lê Thu Hà","lockLevel":"FULL",
        "reason":"Tạm nghỉ để theo dõi hồi phục","lockedAt":"2026-10-03T08:30:00",
        "expectedEndAt":"2026-10-17T08:30:00","releasedAt":null,
        "releasedBy":null,"releasedByName":null,"status":"ACTIVE"}],
        "restrictedByLocks":{"light":true,"moderate":true,"heavy":true}}
        """;
}
