package com.horsemanagement;

import com.horsemanagement.controller.VeterinarianMedicalRecordController;
import com.horsemanagement.dto.CreateMedicalRecordRequest;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.IncidentReport;
import com.horsemanagement.entity.MedicalRecord;
import com.horsemanagement.entity.Role;
import com.horsemanagement.entity.User;
import com.horsemanagement.exception.IncidentAlreadyLinkedException;
import com.horsemanagement.exception.InvalidMedicalRecordException;
import com.horsemanagement.exception.MedicalRecordExceptionHandler;
import com.horsemanagement.exception.VeterinarianExceptionHandler;
import com.horsemanagement.mapper.MedicalRecordMapper;
import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.repository.IncidentReportRepository;
import com.horsemanagement.repository.MedicalRecordRepository;
import com.horsemanagement.repository.UserRepository;
import com.horsemanagement.service.VeterinarianMedicalRecordService;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeterinarianMedicalRecordApiTest {
    private Fixture fixture;
    private VeterinarianMedicalRecordService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        fixture = new Fixture();
        service = fixture.service();
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new VeterinarianMedicalRecordController(service))
            .setControllerAdvice(new MedicalRecordExceptionHandler(),
                new VeterinarianExceptionHandler())
            .setValidator(validator).build();
    }

    @Test
    void listsRecordsWithPaginationAndDefaultSort() throws Exception {
        mvc.perform(get("/api/veterinarian/medical-records").param("page", "0")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].horseName").value("Hồng Lôi"))
            .andExpect(jsonPath("$.content[0].vetName").value("Dr Vet"))
            .andExpect(jsonPath("$.totalElements").value(1));
        assertThat(fixture.lastPage.getPageSize()).isEqualTo(1);
        assertThat(fixture.lastPage.getSort().getOrderFor("examDate").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        assertThat(fixture.lastPage.getSort().getOrderFor("recordId").getDirection())
            .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void acceptsHorseVetAndDateFilters() throws Exception {
        mvc.perform(get("/api/veterinarian/medical-records")
                .param("horseId", "1").param("vetId", "3")
                .param("fromDate", "2026-10-01").param("toDate", "2026-10-10"))
            .andExpect(status().isOk());
        assertThat(fixture.lastFilter).isNotNull();
        assertThatThrownBy(() -> service.list(null, null,
            LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 10), 0, 20))
            .isInstanceOf(InvalidMedicalRecordException.class);
    }

    @Test
    void returnsDetailsAnd404() throws Exception {
        mvc.perform(get("/api/veterinarian/medical-records/7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recordId").value(7))
            .andExpect(jsonPath("$.horseId").value(1))
            .andExpect(jsonPath("$.vetName").value("Dr Vet"));
        mvc.perform(get("/api/veterinarian/medical-records/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createsRecordWithoutIncidentAndChangesOnlyHealthStatus() throws Exception {
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":3,\"healthStatusAfter\":\"MONITORING\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.recordId").value(42))
            .andExpect(jsonPath("$.incidentId").doesNotExist())
            .andExpect(jsonPath("$.healthStatusAfter").value("MONITORING"));
        assertThat(fixture.saved.getExamDate()).isNotNull();
        assertThat(fixture.updatedStatus).isEqualTo(Horse.HealthStatus.MONITORING);
        assertThat(fixture.horse.isTrainingLocked()).isTrue();
        assertThat(fixture.horse.getReadinessStatus()).isEqualTo(Horse.ReadinessStatus.RESTING);
        assertThat(fixture.horse.getHealthStatus()).isEqualTo(Horse.HealthStatus.INJURED);
    }

    @Test
    void createsRecordWithMatchingUnlinkedIncidentAndExplicitExamDate() throws Exception {
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":3,\"incidentId\":10,"
                    + "\"examDate\":\"2026-10-10T09:15:00\",\"reason\":\"Follow-up\","
                    + "\"healthStatusAfter\":\"MONITORING\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.incidentId").value(10));
        assertThat(fixture.saved.getReason()).isEqualTo("Follow-up");
        assertThat(fixture.saved.getExamDate())
            .isEqualTo(LocalDateTime.of(2026, 10, 10, 9, 15));
    }

    @Test
    void rejectsUnknownHorseAndVet() throws Exception {
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":999,\"vetId\":3,\"healthStatusAfter\":\"MONITORING\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":999,\"healthStatusAfter\":\"MONITORING\"}"))
            .andExpect(status().isNotFound());
        assertThat(fixture.saved).isNull();
    }

    @Test
    void rejectsNonVeterinarianRoleAndInvalidStatus() throws Exception {
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":4,\"healthStatusAfter\":\"MONITORING\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":3,\"healthStatusAfter\":\"WRONG\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":3}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsWrongHorseIncidentAndAlreadyLinkedIncident() throws Exception {
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":3,\"incidentId\":11,"
                    + "\"healthStatusAfter\":\"MONITORING\"}"))
            .andExpect(status().isBadRequest());
        fixture.incidentAlreadyLinked = true;
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":3,\"incidentId\":10,"
                    + "\"healthStatusAfter\":\"MONITORING\"}"))
            .andExpect(status().isConflict());
        assertThat(fixture.saved).isNull();
    }

    @Test
    void validatesRequiredFieldsIdsAndReasonLength() throws Exception {
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":0,\"vetId\":3,\"healthStatusAfter\":\"INJURED\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":1,\"vetId\":3,\"reason\":\""
                    + "x".repeat(256) + "\",\"healthStatusAfter\":\"INJURED\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateIncidentRaceBecomesConflict() {
        fixture.uniqueRace = true;
        assertThatThrownBy(() -> service.create(request(1, 3, 10)))
            .isInstanceOf(IncidentAlreadyLinkedException.class);
        assertThat(fixture.updatedStatus).isNull();
    }

    @Test
    void transactionRollsBackWhenHealthUpdateFails() {
        fixture.failHealthUpdate = true;
        var manager = new RecordingTransactionManager();
        var proxy = new ProxyFactory(service);
        proxy.addAdvice(new TransactionInterceptor(manager,
            new AnnotationTransactionAttributeSource()));
        var transactional = (VeterinarianMedicalRecordService) proxy.getProxy();

        assertThatThrownBy(() -> transactional.create(request(1, 3, null)))
            .isInstanceOf(IllegalStateException.class);
        assertThat(fixture.saved).isNotNull();
        assertThat(manager.rollbacks).isEqualTo(1);
        assertThat(manager.commits).isZero();
        assertThat(manager.readOnly).isFalse();
    }

    private static CreateMedicalRecordRequest request(int horseId, int vetId, Integer incidentId) {
        return new CreateMedicalRecordRequest(horseId, vetId, incidentId, null,
            null, null, null, Horse.HealthStatus.MONITORING, null);
    }

    private static class RecordingTransactionManager implements PlatformTransactionManager {
        int commits;
        int rollbacks;
        boolean readOnly;

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            readOnly = definition.isReadOnly();
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            commits++;
        }

        @Override
        public void rollback(TransactionStatus status) {
            rollbacks++;
        }
    }

    private class Fixture {
        final Horse horse = new Horse();
        final User vet = new User();
        final User nonVet = new User();
        MedicalRecord saved;
        Horse.HealthStatus updatedStatus;
        Pageable lastPage;
        Specification<MedicalRecord> lastFilter;
        boolean incidentAlreadyLinked;
        boolean uniqueRace;
        boolean failHealthUpdate;

        Fixture() {
            horse.setHorseId(1);
            horse.setName("Hồng Lôi");
            horse.setHealthStatus(Horse.HealthStatus.INJURED);
            horse.setReadinessStatus(Horse.ReadinessStatus.RESTING);
            horse.setTrainingLocked(true);
            var vetRole = new Role();
            vetRole.setRoleCode("VETERINARIAN");
            vet.setUserId(3);
            vet.setFullName("Dr Vet");
            vet.setRole(vetRole);
            var groomRole = new Role();
            groomRole.setRoleCode("GROOM");
            nonVet.setUserId(4);
            nonVet.setRole(groomRole);
        }

        VeterinarianMedicalRecordService service() {
            var existing = new MedicalRecord();
            existing.setRecordId(7);
            existing.setHorse(horse);
            existing.setVet(vet);
            existing.setExamDate(LocalDateTime.of(2026, 10, 8, 8, 0));
            existing.setHealthStatusAfter(Horse.HealthStatus.INJURED);

            var horseRepository = (HorseRepository) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {HorseRepository.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> (Integer) args[0] == 1 ? Optional.of(horse) : Optional.empty();
                    case "updateHealthStatus" -> {
                        if (failHealthUpdate) throw new IllegalStateException("simulated update failure");
                        updatedStatus = (Horse.HealthStatus) args[1];
                        yield 1;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
            var userRepository = (UserRepository) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {UserRepository.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> switch ((Integer) args[0]) {
                        case 3 -> Optional.of(vet);
                        case 4 -> Optional.of(nonVet);
                        default -> Optional.empty();
                    };
                    default -> throw new UnsupportedOperationException(method.getName());
                });
            var incidentRepository = (IncidentReportRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {IncidentReportRepository.class},
                (proxy, method, args) -> {
                    if (!method.getName().equals("findById")) {
                        throw new UnsupportedOperationException(method.getName());
                    }
                    if ((Integer) args[0] != 10 && (Integer) args[0] != 11) {
                        return Optional.empty();
                    }
                    var incident = new IncidentReport();
                    incident.setIncidentId((Integer) args[0]);
                    if ((Integer) args[0] == 10) {
                        incident.setHorse(horse);
                    } else {
                        var other = new Horse();
                        other.setHorseId(2);
                        incident.setHorse(other);
                    }
                    return Optional.of(incident);
                });
            var recordRepository = (MedicalRecordRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {MedicalRecordRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findAll" -> {
                        lastFilter = (Specification<MedicalRecord>) args[0];
                        lastPage = (Pageable) args[1];
                        yield new PageImpl<>(List.of(existing), lastPage, 1);
                    }
                    case "findByRecordId" -> (Integer) args[0] == 7
                        ? Optional.of(existing) : Optional.empty();
                    case "existsByIncident_IncidentId" -> incidentAlreadyLinked;
                    case "saveAndFlush" -> {
                        if (uniqueRace) {
                            throw new DataIntegrityViolationException("duplicate incident",
                                new SQLException("unique index", "23000", 2601));
                        }
                        saved = (MedicalRecord) args[0];
                        saved.setRecordId(42);
                        yield saved;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
            return new VeterinarianMedicalRecordService(recordRepository, horseRepository,
                userRepository, incidentRepository, new MedicalRecordMapper());
        }
    }
}
