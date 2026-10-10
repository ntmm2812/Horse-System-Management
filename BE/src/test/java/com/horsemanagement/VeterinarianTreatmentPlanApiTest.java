package com.horsemanagement;

import com.horsemanagement.controller.VeterinarianTreatmentPlanController;
import com.horsemanagement.dto.CreateTreatmentPlanRequest;
import com.horsemanagement.dto.PatchTreatmentPlanRequest;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.MedicalRecord;
import com.horsemanagement.entity.PrescriptionItem;
import com.horsemanagement.entity.Supply;
import com.horsemanagement.entity.SupplyCategory;
import com.horsemanagement.entity.TreatmentPlan;
import com.horsemanagement.exception.InvalidTreatmentPlanException;
import com.horsemanagement.exception.TreatmentPlanExceptionHandler;
import com.horsemanagement.mapper.TreatmentPlanMapper;
import com.horsemanagement.repository.MedicalRecordRepository;
import com.horsemanagement.repository.PrescriptionItemRepository;
import com.horsemanagement.repository.SupplyRepository;
import com.horsemanagement.repository.TreatmentPlanRepository;
import com.horsemanagement.service.VeterinarianTreatmentPlanService;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeterinarianTreatmentPlanApiTest {
    private Fixture fixture;
    private VeterinarianTreatmentPlanService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        fixture = new Fixture();
        service = fixture.service();
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new VeterinarianTreatmentPlanController(service))
            .setControllerAdvice(new TreatmentPlanExceptionHandler())
            .setValidator(validator).build();
    }

    @Test
    void listsTreatmentsWithPaginationAndFilters() throws Exception {
        mvc.perform(get("/api/veterinarian/treatment-plans")
                .param("recordId", "1").param("status", "ACTIVE")
                .param("page", "0").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].recordId").value(1))
            .andExpect(jsonPath("$.content[0].horseName").value("Hồng Lôi"))
            .andExpect(jsonPath("$.totalElements").value(1));
        assertThat(fixture.lastFilter).isNotNull();
        assertThat(fixture.lastPage.getPageSize()).isEqualTo(1);
        assertThat(fixture.lastPage.getSort().getOrderFor("startDate").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        assertThat(fixture.lastPage.getSort().getOrderFor("treatmentId").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        mvc.perform(get("/api/veterinarian/treatment-plans").param("status", "INVALID"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsTreatmentDetailsAndPrescriptionItems() throws Exception {
        mvc.perform(get("/api/veterinarian/treatment-plans/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.treatmentId").value(1))
            .andExpect(jsonPath("$.prescriptions[0].supplyName").value("Medicine A"));
        mvc.perform(get("/api/veterinarian/treatment-plans/1/prescriptions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].prescriptionItemId").value(7));
    }

    @Test
    void createsActiveTreatmentForExistingMedicalRecord() throws Exception {
        mvc.perform(post("/api/veterinarian/treatment-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"description\":\"Rest and monitor\","
                    + "\"startDate\":\"2026-10-03\",\"endDate\":\"2026-10-17\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.treatmentId").value(42))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
        assertThat(fixture.savedPlan.getMedicalRecord().getRecordId()).isEqualTo(1);
        assertThat(fixture.savedPlan.getEndDate()).isEqualTo(LocalDate.of(2026, 10, 17));
    }

    @Test
    void rejectsInvalidCreationAndDates() throws Exception {
        mvc.perform(post("/api/veterinarian/treatment-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"description\":\"  \",\"startDate\":\"2026-10-03\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/treatment-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"description\":\"Rest\","
                    + "\"startDate\":\"2026-10-17\",\"endDate\":\"2026-10-03\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/treatment-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":999,\"description\":\"Rest\","
                    + "\"startDate\":\"2026-10-03\"}"))
            .andExpect(status().isNotFound());
        assertThat(fixture.savedPlan).isNull();
    }

    @Test
    void patchUpdatesStatusButPreservesOmittedAndNullFields() throws Exception {
        mvc.perform(patch("/api/veterinarian/treatment-plans/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\",\"endDate\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.description").value("Existing treatment"));
        assertThat(fixture.plan.getEndDate()).isEqualTo(LocalDate.of(2026, 10, 17));
        assertThat(fixture.savedPlan).isSameAs(fixture.plan);
    }

    @Test
    void patchRejectsInvalidDateBlankDescriptionAndStatus() throws Exception {
        mvc.perform(patch("/api/veterinarian/treatment-plans/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"endDate\":\"2026-10-02\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/treatment-plans/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"  \"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/treatment-plans/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INVALID\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/treatment-plans/1")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void treatmentNotFoundReturns404() throws Exception {
        mvc.perform(get("/api/veterinarian/treatment-plans/999"))
            .andExpect(status().isNotFound());
        mvc.perform(patch("/api/veterinarian/treatment-plans/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/veterinarian/treatment-plans/999/prescriptions"))
            .andExpect(status().isNotFound());
    }

    @Test
    void addsPrescriptionForMedicineSupply() throws Exception {
        mvc.perform(post("/api/veterinarian/treatment-plans/1/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\":4,\"dosage\":\"Per veterinarian order\","
                    + "\"frequency\":\"Per written schedule\",\"durationDays\":7,"
                    + "\"route\":\"ORAL\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.prescriptionItemId").value(43))
            .andExpect(jsonPath("$.supplyName").value("Medicine A"));
        assertThat(fixture.savedItem.getTreatmentPlan()).isSameAs(fixture.plan);
        assertThat(fixture.savedItem.getSupply()).isSameAs(fixture.medicine);
    }

    @Test
    void rejectsMissingOrNonMedicineSupply() throws Exception {
        mvc.perform(post("/api/veterinarian/treatment-plans/1/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\":999,\"dosage\":\"Text\",\"frequency\":\"Daily\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/veterinarian/treatment-plans/1/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\":5,\"dosage\":\"Text\",\"frequency\":\"Daily\"}"))
            .andExpect(status().isBadRequest());
        assertThat(fixture.savedItem).isNull();
    }

    @Test
    void rejectsInvalidRouteDurationAndLengths() throws Exception {
        mvc.perform(post("/api/veterinarian/treatment-plans/1/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\":4,\"dosage\":\"Text\",\"frequency\":\"Daily\","
                    + "\"route\":\"UNKNOWN\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/treatment-plans/1/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\":4,\"dosage\":\"Text\",\"frequency\":\"Daily\","
                    + "\"durationDays\":0}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/treatment-plans/1/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\":4,\"dosage\":\"" + "x".repeat(101)
                    + "\",\"frequency\":\"Daily\"}"))
            .andExpect(status().isBadRequest());
        assertThat(fixture.savedItem).isNull();
    }

    @Test
    void databaseWriteConflictIsReportedAs409() throws Exception {
        fixture.failSave = true;
        mvc.perform(post("/api/veterinarian/treatment-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"description\":\"Rest\","
                    + "\"startDate\":\"2026-10-03\"}"))
            .andExpect(status().isConflict());
    }

    private static class Fixture {
        final Horse horse = new Horse();
        final MedicalRecord record = new MedicalRecord();
        final TreatmentPlan plan = new TreatmentPlan();
        final Supply medicine = new Supply();
        final Supply feed = new Supply();
        final PrescriptionItem item = new PrescriptionItem();
        TreatmentPlan savedPlan;
        PrescriptionItem savedItem;
        Pageable lastPage;
        Specification<TreatmentPlan> lastFilter;
        boolean failSave;

        Fixture() {
            horse.setHorseId(3);
            horse.setName("Hồng Lôi");
            record.setRecordId(1);
            record.setHorse(horse);
            plan.setTreatmentId(1);
            plan.setMedicalRecord(record);
            plan.setDescription("Existing treatment");
            plan.setStartDate(LocalDate.of(2026, 10, 3));
            plan.setEndDate(LocalDate.of(2026, 10, 17));
            plan.setStatus(TreatmentPlan.Status.ACTIVE);
            var medicineCategory = new SupplyCategory();
            medicineCategory.setCategoryType(SupplyCategory.CategoryType.MEDICINE);
            medicine.setSupplyId(4);
            medicine.setSupplyName("Medicine A");
            medicine.setCategory(medicineCategory);
            var feedCategory = new SupplyCategory();
            feedCategory.setCategoryType(SupplyCategory.CategoryType.FEED);
            feed.setSupplyId(5);
            feed.setCategory(feedCategory);
            item.setPrescriptionItemId(7);
            item.setTreatmentPlan(plan);
            item.setSupply(medicine);
            item.setDosage("Existing instructions");
            item.setFrequency("Daily");
        }

        VeterinarianTreatmentPlanService service() {
            var plans = (TreatmentPlanRepository) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {TreatmentPlanRepository.class}, (proxy, method, args) ->
                    switch (method.getName()) {
                        case "findAll" -> {
                            lastFilter = (Specification<TreatmentPlan>) args[0];
                            lastPage = (Pageable) args[1];
                            yield new PageImpl<>(List.of(plan), lastPage, 1);
                        }
                        case "findByTreatmentId" -> (Integer) args[0] == 1
                            ? Optional.of(plan) : Optional.empty();
                        case "saveAndFlush" -> {
                            if (failSave) throw new DataIntegrityViolationException("simulated FK race");
                            savedPlan = (TreatmentPlan) args[0];
                            if (savedPlan.getTreatmentId() == null) savedPlan.setTreatmentId(42);
                            yield savedPlan;
                        }
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
            var prescriptions = (PrescriptionItemRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {PrescriptionItemRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByTreatmentPlan_TreatmentIdOrderByPrescriptionItemIdAsc" ->
                        (Integer) args[0] == 1 ? List.of(item) : List.of();
                    case "saveAndFlush" -> {
                        if (failSave) throw new DataIntegrityViolationException("simulated FK race");
                        savedItem = (PrescriptionItem) args[0];
                        savedItem.setPrescriptionItemId(43);
                        yield savedItem;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
            var records = (MedicalRecordRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {MedicalRecordRepository.class},
                (proxy, method, args) -> {
                    if (!method.getName().equals("findById"))
                        throw new UnsupportedOperationException(method.getName());
                    return (Integer) args[0] == 1 ? Optional.of(record) : Optional.empty();
                });
            var supplies = (SupplyRepository) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {SupplyRepository.class}, (proxy, method, args) -> {
                    if (!method.getName().equals("findBySupplyId"))
                        throw new UnsupportedOperationException(method.getName());
                    return switch ((Integer) args[0]) {
                        case 4 -> Optional.of(medicine);
                        case 5 -> Optional.of(feed);
                        default -> Optional.empty();
                    };
                });
            return new VeterinarianTreatmentPlanService(plans, prescriptions, records, supplies,
                new TreatmentPlanMapper());
        }
    }
}
