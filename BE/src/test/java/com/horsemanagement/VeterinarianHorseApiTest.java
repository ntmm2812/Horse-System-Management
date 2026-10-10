package com.horsemanagement;

import com.horsemanagement.controller.VeterinarianHorseController;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.HorseHealthMetric;
import com.horsemanagement.entity.User;
import com.horsemanagement.exception.VeterinarianExceptionHandler;
import com.horsemanagement.mapper.VeterinarianHorseMapper;
import com.horsemanagement.repository.HorseHealthMetricRepository;
import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.service.VeterinarianHorseService;
import jakarta.validation.Validation;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeterinarianHorseApiTest {
    private VeterinarianHorseService service;
    private MockMvc mvc;
    private Pageable lastHorsePage;
    private Pageable lastMetricPage;

    @BeforeEach
    void setUp() {
        var owner = new User();
        owner.setFullName("Owner A");
        var manager = new User();
        manager.setFullName("Manager B");
        var horse = new Horse();
        horse.setHorseId(1);
        horse.setName("Comet");
        horse.setOwner(owner);
        horse.setManager(manager);
        horse.setHealthStatus(Horse.HealthStatus.INJURED);
        horse.setReadinessStatus(Horse.ReadinessStatus.RESTING);
        var horseWithoutMetrics = new Horse();
        horseWithoutMetrics.setHorseId(2);
        horseWithoutMetrics.setName("Nova");
        horseWithoutMetrics.setOwner(owner);
        var metric = new HorseHealthMetric();
        metric.setMetricId(9L);
        metric.setWeightKg(new BigDecimal("455.25"));
        metric.setRecordedAt(LocalDateTime.of(2026, 10, 10, 8, 30));

        var horses = (HorseRepository) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[] {HorseRepository.class}, (proxy, method, args) -> switch (method.getName()) {
                case "findAll" -> {
                    lastHorsePage = (Pageable) args[0];
                    yield pageOf(horse, lastHorsePage);
                }
                case "findByHealthStatus" -> {
                    lastHorsePage = (Pageable) args[1];
                    yield args[0] == Horse.HealthStatus.INJURED
                        ? pageOf(horse, lastHorsePage)
                        : new PageImpl<>(List.of(), lastHorsePage, 0);
                }
                case "findByHorseId" -> switch ((Integer) args[0]) {
                    case 1 -> Optional.of(horse);
                    case 2 -> Optional.of(horseWithoutMetrics);
                    default -> Optional.empty();
                };
                case "existsById" -> (Integer) args[0] == 1 || (Integer) args[0] == 2;
                default -> throw new UnsupportedOperationException(method.getName());
            });
        var metrics = (HorseHealthMetricRepository) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] {HorseHealthMetricRepository.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "findFirstByHorse_HorseIdOrderByRecordedAtDescMetricIdDesc" ->
                    (Integer) args[0] == 1 ? Optional.of(metric) : Optional.empty();
                case "findByHorse_HorseId" -> {
                    lastMetricPage = (Pageable) args[1];
                    var hasMetric = (Integer) args[0] == 1;
                    yield new PageImpl<>(hasMetric && lastMetricPage.getPageNumber() == 0
                        ? List.of(metric) : List.of(), lastMetricPage, hasMetric ? 1 : 0);
                }
                default -> throw new UnsupportedOperationException(method.getName());
            });
        service = new VeterinarianHorseService(horses, metrics, new VeterinarianHorseMapper());
        mvc = MockMvcBuilders.standaloneSetup(new VeterinarianHorseController(service))
            .setControllerAdvice(new VeterinarianExceptionHandler()).build();
    }

    @Test
    void listsHorsesWithNamesAndPagination() throws Exception {
        mvc.perform(get("/api/veterinarian/horses").param("page", "0").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Comet"))
            .andExpect(jsonPath("$.content[0].ownerName").value("Owner A"))
            .andExpect(jsonPath("$.content[0].managerName").value("Manager B"))
            .andExpect(jsonPath("$.totalElements").value(1));
        assertThat(lastHorsePage.getPageSize()).isEqualTo(1);
        assertThat(lastHorsePage.getSort().getOrderFor("horseId").getDirection())
            .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void filtersByHealthStatus() throws Exception {
        mvc.perform(get("/api/veterinarian/horses").param("healthStatus", "INJURED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].healthStatus").value("INJURED"));
        mvc.perform(get("/api/veterinarian/horses").param("healthStatus", "ELIGIBLE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void returnsHorseProfileAndLatestMetric() throws Exception {
        mvc.perform(get("/api/veterinarian/horses/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.horseId").value(1))
            .andExpect(jsonPath("$.ownerName").value("Owner A"))
            .andExpect(jsonPath("$.latestHealthMetric.metricId").value(9));
    }

    @Test
    void nonexistentHorseReturns404() throws Exception {
        mvc.perform(get("/api/veterinarian/horses/999"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/veterinarian/horses/999/health-metrics"))
            .andExpect(status().isNotFound());
    }

    @Test
    void returnsMetricHistoryWithStableDescendingSort() throws Exception {
        mvc.perform(get("/api/veterinarian/horses/1/health-metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].metricId").value(9))
            .andExpect(jsonPath("$.content[0].weightKg").value(455.25));
        assertThat(lastMetricPage.getSort().getOrderFor("recordedAt").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        assertThat(lastMetricPage.getSort().getOrderFor("metricId").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        mvc.perform(get("/api/veterinarian/horses/1/health-metrics").param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void existingHorseWithoutMetricsProducesEmptyPage() {
        assertThat(service.getHealthMetrics(2, 0, 20).content()).isEmpty();
        assertThat(service.getHealthMetrics(2, 0, 20).totalElements()).isZero();
    }

    @Test
    void rejectsInvalidFilteringAndPagination() throws Exception {
        mvc.perform(get("/api/veterinarian/horses").param("healthStatus", "WRONG"))
            .andExpect(status().isBadRequest());
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator().forExecutables();
            var controller = new VeterinarianHorseController(service);
            var list = VeterinarianHorseController.class.getMethod("listHorses",
                Horse.HealthStatus.class, int.class, int.class);
            assertThat(validator.validateParameters(controller, list, new Object[] {null, -1, 20}))
                .hasSize(1);
            assertThat(validator.validateParameters(controller, list, new Object[] {null, 0, 0}))
                .hasSize(1);
            assertThat(validator.validateParameters(controller, list, new Object[] {null, 0, 101}))
                .hasSize(1);
        }
    }

    private static PageImpl<Horse> pageOf(Horse horse, Pageable pageable) {
        return new PageImpl<>(pageable.getPageNumber() == 0 ? List.of(horse) : List.of(), pageable, 1);
    }
}
