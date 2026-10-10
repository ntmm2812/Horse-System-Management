package com.horsemanagement.config;

public final class VeterinarianPreventiveCareOpenApiExamples {
    private VeterinarianPreventiveCareOpenApiExamples() {}
    public static final String SCHEDULE = """
        {"scheduleId":8,"horseId":2,"horseName":"Hong Loi","careType":"VACCINATION",
         "description":"Annual vaccination","dueDate":"2026-11-01","intervalDays":365,
         "remindBeforeDays":3,"status":"PENDING","completedDate":null,
         "performedBy":null,"performedByName":null,"notes":null,"pendingPastDue":false}
        """;
    public static final String PAGE = """
        {"content":[],"page":0,"size":20,"totalElements":0,"totalPages":0,"first":true,"last":true}
        """;
    public static final String CREATE = """
        {"horseId":2,"careType":"VACCINATION","description":"Annual vaccination",
         "dueDate":"2026-11-01","intervalDays":365,"remindBeforeDays":3,"notes":"Veterinary appointment"}
        """;
    public static final String PATCH = """
        {"dueDate":"2026-11-03","description":"Rescheduled appointment","notes":null}
        """;
    public static final String COMPLETE = """
        {"performedBy":5,"completedDate":"2026-10-10","notes":"Completed by veterinarian"}
        """;
}
