package com.horsemanagement;

import com.horsemanagement.controller.VeterinarianInjuryController;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.Injury;
import com.horsemanagement.entity.InjuryProgressLog;
import com.horsemanagement.entity.MedicalRecord;
import com.horsemanagement.entity.Role;
import com.horsemanagement.entity.User;
import com.horsemanagement.exception.InjuryExceptionHandler;
import com.horsemanagement.mapper.InjuryMapper;
import com.horsemanagement.repository.InjuryProgressLogRepository;
import com.horsemanagement.repository.InjuryRepository;
import com.horsemanagement.repository.MedicalRecordRepository;
import com.horsemanagement.repository.UserRepository;
import com.horsemanagement.service.VeterinarianInjuryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeterinarianInjuryApiTest {
    private InjuryRepository injuries;
    private InjuryProgressLogRepository progressLogs;
    private MedicalRecordRepository records;
    private UserRepository users;
    private Injury injury;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        injuries = mock(InjuryRepository.class);
        progressLogs = mock(InjuryProgressLogRepository.class);
        records = mock(MedicalRecordRepository.class);
        users = mock(UserRepository.class);
        var horse = new Horse();
        horse.setHorseId(3);
        horse.setName("Hồng Lôi");
        horse.setHealthStatus(Horse.HealthStatus.INJURED);
        var record = new MedicalRecord();
        record.setRecordId(1);
        record.setHorse(horse);
        injury = new Injury();
        injury.setInjuryId(1);
        injury.setMedicalRecord(record);
        injury.setBodyPart("Chân trước trái");
        injury.setBodySystem(Injury.BodySystem.MUSCLE);
        injury.setModelMeshId("front_left_leg");
        injury.setPositionX(new BigDecimal("0.2000"));
        injury.setSeverity(Injury.Severity.MODERATE);
        injury.setStatus(Injury.Status.ACTIVE);
        injury.setOccurredDate(LocalDate.of(2026, 10, 3));
        injury.setDescription("Theo dõi");

        var vetRole = new Role();
        vetRole.setRoleCode("VETERINARIAN");
        var vet = new User();
        vet.setUserId(3);
        vet.setFullName("Lê Thu Hà");
        vet.setRole(vetRole);
        var groomRole = new Role();
        groomRole.setRoleCode("GROOM");
        var groom = new User();
        groom.setUserId(4);
        groom.setRole(groomRole);
        var log = new InjuryProgressLog();
        log.setProgressId(7);
        log.setInjury(injury);
        log.setLoggedBy(vet);
        log.setLogDate(LocalDateTime.of(2026, 10, 8, 9, 0));
        log.setRecoveryPercent((short) 50);
        log.setPainLevel((short) 2);

        when(injuries.findByInjuryId(1)).thenReturn(Optional.of(injury));
        when(injuries.findByInjuryId(999)).thenReturn(Optional.empty());
        when(records.findByRecordId(1)).thenReturn(Optional.of(record));
        when(records.findByRecordId(999)).thenReturn(Optional.empty());
        when(users.findById(3)).thenReturn(Optional.of(vet));
        when(users.findById(4)).thenReturn(Optional.of(groom));
        when(users.findById(999)).thenReturn(Optional.empty());
        when(injuries.findAll(any(Specification.class), any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(List.of(injury), invocation.getArgument(1), 1));
        when(progressLogs.findByInjury_InjuryId(eq(1), any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(List.of(log), invocation.getArgument(1), 1));
        when(injuries.saveAndFlush(any(Injury.class))).thenAnswer(invocation -> {
            Injury saved = invocation.getArgument(0);
            if (saved.getInjuryId() == null) saved.setInjuryId(42);
            return saved;
        });
        when(progressLogs.saveAndFlush(any(InjuryProgressLog.class))).thenAnswer(invocation -> {
            InjuryProgressLog saved = invocation.getArgument(0);
            saved.setProgressId(43);
            return saved;
        });
        var service = new VeterinarianInjuryService(injuries, progressLogs, records, users,
            new InjuryMapper());
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new VeterinarianInjuryController(service))
            .setControllerAdvice(new InjuryExceptionHandler())
            .setValidator(validator).build();
    }

    @Test
    void listsInjuriesWithFiltersPaginationAndDescendingId() throws Exception {
        mvc.perform(get("/api/veterinarian/injuries")
                .param("horseId", "3").param("status", "ACTIVE")
                .param("severity", "MODERATE").param("bodySystem", "MUSCLE")
                .param("page", "0").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].horseName").value("Hồng Lôi"))
            .andExpect(jsonPath("$.totalElements").value(1));
        var page = ArgumentCaptor.forClass(Pageable.class);
        verify(injuries).findAll(any(Specification.class), page.capture());
        assertThat(page.getValue().getPageSize()).isEqualTo(1);
        assertThat(page.getValue().getSort().getOrderFor("injuryId").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        mvc.perform(get("/api/veterinarian/injuries").param("severity", "UNKNOWN"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/veterinarian/injuries").param("bodySystem", "UNKNOWN"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsFullInjuryDetailsAndMissingInjury404() throws Exception {
        mvc.perform(get("/api/veterinarian/injuries/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recordId").value(1))
            .andExpect(jsonPath("$.horseId").value(3))
            .andExpect(jsonPath("$.modelMeshId").value("front_left_leg"))
            .andExpect(jsonPath("$.positionX").value(0.2));
        mvc.perform(get("/api/veterinarian/injuries/999"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/veterinarian/injuries/999/progress"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createsActiveInjuryForExistingMedicalRecordOnly() throws Exception {
        mvc.perform(post("/api/veterinarian/injuries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"bodyPart\":\"Chân trước trái\","
                    + "\"bodySystem\":\"MUSCLE\",\"severity\":\"MODERATE\","
                    + "\"positionX\":0.2000}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.injuryId").value(42))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
        var saved = ArgumentCaptor.forClass(Injury.class);
        verify(injuries).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getMedicalRecord().getRecordId()).isEqualTo(1);
        assertThat(saved.getValue().getPositionX()).isEqualByComparingTo("0.2000");
        assertThat(injury.getMedicalRecord().getHorse().getHealthStatus())
            .isEqualTo(Horse.HealthStatus.INJURED);
    }

    @Test
    void rejectsUnknownRecordInvalidEnumsAndCoordinates() throws Exception {
        mvc.perform(post("/api/veterinarian/injuries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":999,\"bodyPart\":\"Leg\","
                    + "\"bodySystem\":\"MUSCLE\",\"severity\":\"MINOR\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/veterinarian/injuries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"bodyPart\":\"Leg\","
                    + "\"bodySystem\":\"UNKNOWN\",\"severity\":\"MINOR\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/injuries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"bodyPart\":\"Leg\","
                    + "\"bodySystem\":\"MUSCLE\",\"severity\":\"UNKNOWN\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/injuries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"bodyPart\":\"Leg\","
                    + "\"bodySystem\":\"MUSCLE\",\"severity\":\"MINOR\","
                    + "\"positionX\":123456.12345}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void patchPreservesOmittedFieldsAndClearsOnlyExplicitNull() throws Exception {
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RECOVERING\",\"positionX\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RECOVERING"))
            .andExpect(jsonPath("$.positionX").doesNotExist())
            .andExpect(jsonPath("$.modelMeshId").value("front_left_leg"));
        assertThat(injury.getPositionX()).isNull();
        assertThat(injury.getMedicalRecord().getHorse().getHealthStatus())
            .isEqualTo(Horse.HealthStatus.INJURED);
    }

    @Test
    void healingRequiresValidDateAndSeparateConfirmation() throws Exception {
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"HEALED\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"HEALED\",\"healedDate\":\"2026-10-02\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"HEALED\",\"healedDate\":\"2026-10-15\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("HEALED"));
        assertThat(injury.getHealedDate()).isEqualTo(LocalDate.of(2026, 10, 15));
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RECOVERING\"}"))
            .andExpect(status().isConflict());
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RECOVERING\",\"healedDate\":null}"))
            .andExpect(status().isOk());
        assertThat(injury.getHealedDate()).isNull();
    }

    @Test
    void patchRejectsMissingInjuryEmptyPatchAndInvalidRequiredFields() throws Exception {
        mvc.perform(patch("/api/veterinarian/injuries/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RECOVERING\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"bodyPart\":null}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/injuries/1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"severity\":\"UNKNOWN\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void recoveryHistoryUsesRequestedPaginationAndSort() throws Exception {
        mvc.perform(get("/api/veterinarian/injuries/1/progress")
                .param("page", "0").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].progressId").value(7))
            .andExpect(jsonPath("$.content[0].loggedByName").value("Lê Thu Hà"));
        var page = ArgumentCaptor.forClass(Pageable.class);
        verify(progressLogs).findByInjury_InjuryId(eq(1), page.capture());
        assertThat(page.getValue().getSort().getOrderFor("logDate").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        assertThat(page.getValue().getSort().getOrderFor("progressId").getDirection())
            .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void createsProgressAt100PercentWithoutHealingInjury() throws Exception {
        mvc.perform(post("/api/veterinarian/injuries/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loggedBy\":3,\"recoveryPercent\":100,\"painLevel\":0}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.progressId").value(43))
            .andExpect(jsonPath("$.recoveryPercent").value(100));
        var saved = ArgumentCaptor.forClass(InjuryProgressLog.class);
        verify(progressLogs).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getLogDate()).isNotNull();
        assertThat(saved.getValue().getLoggedBy().getRole().getRoleCode())
            .isEqualTo("VETERINARIAN");
        assertThat(injury.getStatus()).isEqualTo(Injury.Status.ACTIVE);
        assertThat(injury.getMedicalRecord().getHorse().getHealthStatus())
            .isEqualTo(Horse.HealthStatus.INJURED);
    }

    @Test
    void rejectsInvalidRecoveryPercentPainAndNonVeterinarian() throws Exception {
        mvc.perform(post("/api/veterinarian/injuries/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loggedBy\":3,\"recoveryPercent\":101}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/injuries/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loggedBy\":3,\"painLevel\":11}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/injuries/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loggedBy\":4}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/injuries/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loggedBy\":999}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void databaseWriteConflictReturns409() throws Exception {
        when(injuries.saveAndFlush(any(Injury.class)))
            .thenThrow(new DataIntegrityViolationException("simulated FK conflict"));
        mvc.perform(post("/api/veterinarian/injuries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordId\":1,\"bodyPart\":\"Leg\","
                    + "\"bodySystem\":\"MUSCLE\",\"severity\":\"MINOR\"}"))
            .andExpect(status().isConflict());
    }
}
