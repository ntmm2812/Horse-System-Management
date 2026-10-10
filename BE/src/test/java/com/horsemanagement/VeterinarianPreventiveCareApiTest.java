package com.horsemanagement;

import com.horsemanagement.controller.VeterinarianPreventiveCareController;
import com.horsemanagement.dto.CompletePreventiveCareRequest;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.PreventiveCareSchedule;
import com.horsemanagement.entity.Role;
import com.horsemanagement.entity.User;
import com.horsemanagement.exception.PreventiveCareConflictException;
import com.horsemanagement.exception.PreventiveCareExceptionHandler;
import com.horsemanagement.mapper.PreventiveCareMapper;
import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.repository.PreventiveCareScheduleRepository;
import com.horsemanagement.repository.UserRepository;
import com.horsemanagement.service.VeterinarianPreventiveCareService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeterinarianPreventiveCareApiTest {
    private PreventiveCareScheduleRepository schedules;
    private HorseRepository horses;
    private UserRepository users;
    private VeterinarianPreventiveCareService service;
    private Horse horse;
    private PreventiveCareSchedule schedule;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        schedules = mock(PreventiveCareScheduleRepository.class);
        horses = mock(HorseRepository.class);
        users = mock(UserRepository.class);
        horse = new Horse();
        horse.setHorseId(3);
        horse.setName("Hồng Lôi");
        schedule = new PreventiveCareSchedule();
        schedule.setScheduleId(1);
        schedule.setHorse(horse);
        schedule.setCareType(PreventiveCareSchedule.CareType.VACCINATION);
        schedule.setDueDate(LocalDate.now().minusDays(1));
        schedule.setStatus(PreventiveCareSchedule.Status.PENDING);
        var veterinarianRole = new Role();
        veterinarianRole.setRoleCode("VETERINARIAN");
        var vet = new User();
        vet.setUserId(5);
        vet.setFullName("Lê Thu Hà");
        vet.setRole(veterinarianRole);
        var otherRole = new Role();
        otherRole.setRoleCode("GROOM");
        var groom = new User();
        groom.setUserId(6);
        groom.setRole(otherRole);
        when(horses.findByHorseId(3)).thenReturn(Optional.of(horse));
        when(users.findById(5)).thenReturn(Optional.of(vet));
        when(users.findById(6)).thenReturn(Optional.of(groom));
        when(schedules.findByScheduleId(1)).thenReturn(Optional.of(schedule));
        when(schedules.lockById(1)).thenReturn(Optional.of(schedule));
        when(schedules.findAll(any(Specification.class), any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(List.of(schedule), invocation.getArgument(1), 1));
        when(schedules.saveAndFlush(any(PreventiveCareSchedule.class))).thenAnswer(invocation -> {
            PreventiveCareSchedule saved = invocation.getArgument(0);
            if (saved.getScheduleId() == null) saved.setScheduleId(42);
            return saved;
        });
        service = new VeterinarianPreventiveCareService(schedules, horses, users, new PreventiveCareMapper());
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new VeterinarianPreventiveCareController(service))
            .setControllerAdvice(new PreventiveCareExceptionHandler())
            .setValidator(validator).build();
    }

    @Test
    void listSupportsFiltersPaginationAndReadOnlyPastDueIdentification() throws Exception {
        mvc.perform(get("/api/veterinarian/preventive-care")
                .param("horseId", "3").param("careType", "VACCINATION")
                .param("status", "PENDING").param("fromDueDate", "2020-01-01")
                .param("toDueDate", "2030-01-01").param("page", "1").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].horseName").value("Hồng Lôi"))
            .andExpect(jsonPath("$.content[0].pendingPastDue").value(true))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"));
        var pages = ArgumentCaptor.forClass(Pageable.class);
        verify(schedules).findAll(any(Specification.class), pages.capture());
        assertThat(pages.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pages.getValue().getPageSize()).isEqualTo(2);
        assertThat(pages.getValue().getSort().getOrderFor("dueDate").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pages.getValue().getSort().getOrderFor("scheduleId").getDirection()).isEqualTo(Sort.Direction.ASC);
        mvc.perform(get("/api/veterinarian/preventive-care").param("pendingPastDueOnly", "true"))
            .andExpect(status().isOk());
        assertThat(schedule.getStatus()).isEqualTo(PreventiveCareSchedule.Status.PENDING);
        verify(schedules, never()).saveAndFlush(any());
    }

    @Test
    void invalidFiltersAndPaginationReturn400() throws Exception {
        for (String query : List.of("?careType=INVALID", "?status=INVALID", "?page=-1",
            "?size=0", "?horseId=0", "?fromDueDate=2026-12-01&toDueDate=2026-11-01",
            "?pendingPastDueOnly=true&status=DONE")) {
            mvc.perform(get("/api/veterinarian/preventive-care" + query))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    void detailAndHorseHistoryHandleMissingRecords() throws Exception {
        mvc.perform(get("/api/veterinarian/preventive-care/1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.horseId").value(3));
        mvc.perform(get("/api/veterinarian/preventive-care/999"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/veterinarian/horses/3/preventive-care").param("status", "PENDING"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].scheduleId").value(1));
        mvc.perform(get("/api/veterinarian/horses/999/preventive-care"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createValidatesFieldsAndUsesDatabaseDefaultsExplicitly() throws Exception {
        mvc.perform(post("/api/veterinarian/preventive-care")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"careType\":\"DENTAL\",\"dueDate\":\"2026-11-01\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.remindBeforeDays").value(3));
        mvc.perform(post("/api/veterinarian/preventive-care")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":999,\"careType\":\"DENTAL\",\"dueDate\":\"2026-11-01\"}"))
            .andExpect(status().isNotFound());
        for (String body : List.of(
            "{\"horseId\":3,\"careType\":\"INVALID\",\"dueDate\":\"2026-11-01\"}",
            "{\"horseId\":3,\"careType\":\"DENTAL\",\"dueDate\":\"2026-11-01\",\"intervalDays\":0}",
            "{\"horseId\":3,\"careType\":\"DENTAL\",\"dueDate\":\"2026-11-01\",\"remindBeforeDays\":-1}",
            "{\"horseId\":3,\"careType\":\"DENTAL\",\"dueDate\":\"2026-11-01\",\"remindBeforeDays\":256}")) {
            mvc.perform(post("/api/veterinarian/preventive-care")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    void patchPreservesOmittedFieldsAndRejectsStatusOrHorseChange() throws Exception {
        schedule.setDescription("original");
        mvc.perform(patch("/api/veterinarian/preventive-care/1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"description\":null,\"notes\":\"rebook\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.notes").value("rebook"));
        assertThat(schedule.getDescription()).isNull();
        assertThat(schedule.getCareType()).isEqualTo(PreventiveCareSchedule.CareType.VACCINATION);
        assertThat(schedule.getDueDate()).isEqualTo(LocalDate.now().minusDays(1));
        for (String body : List.of("{\"horseId\":4}", "{\"status\":\"DONE\"}",
            "{\"careType\":null}", "{\"dueDate\":null}", "{\"remindBeforeDays\":null}",
            "{\"intervalDays\":0}", "{}")) {
            mvc.perform(patch("/api/veterinarian/preventive-care/1")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    void completionRequiresVeterinarianAndOpenStatus() throws Exception {
        mvc.perform(patch("/api/veterinarian/preventive-care/1/complete")
                .contentType(MediaType.APPLICATION_JSON).content("{\"performedBy\":6}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/preventive-care/1/complete")
                .contentType(MediaType.APPLICATION_JSON).content("{\"performedBy\":999}"))
            .andExpect(status().isNotFound());
        mvc.perform(patch("/api/veterinarian/preventive-care/1/complete")
                .contentType(MediaType.APPLICATION_JSON).content("{\"performedBy\":5}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DONE"))
            .andExpect(jsonPath("$.performedByName").value("Lê Thu Hà"));
        assertThat(schedule.getCompletedDate()).isEqualTo(LocalDate.now());
        mvc.perform(patch("/api/veterinarian/preventive-care/1/complete")
                .contentType(MediaType.APPLICATION_JSON).content("{\"performedBy\":5}"))
            .andExpect(status().isConflict());
    }

    @Test
    void cancellationRejectsCompletedOrAlreadyCancelledSchedules() throws Exception {
        mvc.perform(patch("/api/veterinarian/preventive-care/1/cancel"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(patch("/api/veterinarian/preventive-care/1/cancel"))
            .andExpect(status().isConflict());
        schedule.setStatus(PreventiveCareSchedule.Status.DONE);
        assertThatThrownBy(() -> service.cancel(1)).isInstanceOf(PreventiveCareConflictException.class);
    }
}
