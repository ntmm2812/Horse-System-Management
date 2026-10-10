package com.horsemanagement;

import com.horsemanagement.controller.VeterinarianTrainingLockController;
import com.horsemanagement.dto.CreateTrainingLockRequest;
import com.horsemanagement.dto.ReleaseTrainingLockRequest;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.Injury;
import com.horsemanagement.entity.MedicalRecord;
import com.horsemanagement.entity.Role;
import com.horsemanagement.entity.TrainingLock;
import com.horsemanagement.entity.User;
import com.horsemanagement.exception.TrainingLockConflictException;
import com.horsemanagement.exception.TrainingLockExceptionHandler;
import com.horsemanagement.mapper.TrainingLockMapper;
import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.repository.InjuryRepository;
import com.horsemanagement.repository.TrainingLockRepository;
import com.horsemanagement.repository.UserRepository;
import com.horsemanagement.service.VeterinarianTrainingLockService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VeterinarianTrainingLockApiTest {
    private TrainingLockRepository locks;
    private HorseRepository horses;
    private InjuryRepository injuries;
    private UserRepository users;
    private VeterinarianTrainingLockService service;
    private Horse horse;
    private Injury injury;
    private TrainingLock lock;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        locks = mock(TrainingLockRepository.class);
        horses = mock(HorseRepository.class);
        injuries = mock(InjuryRepository.class);
        users = mock(UserRepository.class);

        horse = new Horse();
        horse.setHorseId(3);
        horse.setName("Hồng Lôi");
        horse.setHealthStatus(Horse.HealthStatus.INJURED);
        horse.setReadinessStatus(Horse.ReadinessStatus.RESTING);
        horse.setTrainingLocked(true);
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
        var record = new MedicalRecord();
        record.setRecordId(1);
        record.setHorse(horse);
        injury = new Injury();
        injury.setInjuryId(1);
        injury.setBodyPart("Chân trước trái");
        injury.setMedicalRecord(record);
        lock = new TrainingLock();
        lock.setLockId(1);
        lock.setHorse(horse);
        lock.setLockedBy(vet);
        lock.setInjury(injury);
        lock.setLockLevel(TrainingLock.LockLevel.FULL);
        lock.setReason("Theo dõi hồi phục");
        lock.setLockedAt(LocalDateTime.of(2026, 10, 3, 8, 30));
        lock.setStatus(TrainingLock.Status.ACTIVE);

        when(horses.findByHorseId(3)).thenReturn(Optional.of(horse));
        when(horses.lockForTrainingLocks(3)).thenReturn(Optional.of(horse));
        when(horses.lockForTrainingLocks(999)).thenReturn(Optional.empty());
        when(horses.updateTrainingLocked(eq(3), anyBoolean())).thenReturn(1);
        when(injuries.findByInjuryId(1)).thenReturn(Optional.of(injury));
        when(injuries.findByInjuryId(999)).thenReturn(Optional.empty());
        when(users.findById(3)).thenReturn(Optional.of(vet));
        when(users.findById(4)).thenReturn(Optional.of(groom));
        when(users.findById(999)).thenReturn(Optional.empty());
        when(locks.findByLockId(1)).thenReturn(Optional.of(lock));
        when(locks.findByLockId(999)).thenReturn(Optional.empty());
        when(locks.findHorseIdByLockId(1)).thenReturn(Optional.of(3));
        when(locks.findHorseIdByLockId(999)).thenReturn(Optional.empty());
        when(locks.findAll(any(Specification.class), any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(List.of(lock), invocation.getArgument(1), 1));
        when(locks.findByHorse_HorseId(eq(3), any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(List.of(lock), invocation.getArgument(1), 1));
        when(locks.findByHorse_HorseIdAndStatusOrderByLockedAtDescLockIdDesc(3,
            TrainingLock.Status.ACTIVE)).thenReturn(List.of(lock));
        when(locks.saveAndFlush(any(TrainingLock.class))).thenAnswer(invocation -> {
            TrainingLock saved = invocation.getArgument(0);
            if (saved.getLockId() == null) saved.setLockId(42);
            return saved;
        });

        service = new VeterinarianTrainingLockService(locks, horses, injuries, users,
            new TrainingLockMapper());
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new VeterinarianTrainingLockController(service))
            .setControllerAdvice(new TrainingLockExceptionHandler())
            .setValidator(validator).build();
    }

    @Test
    void listsAndFiltersWithNewestFirstPagination() throws Exception {
        mvc.perform(get("/api/veterinarian/training-locks")
                .param("horseId", "3").param("status", "ACTIVE")
                .param("lockLevel", "FULL").param("page", "0").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].horseName").value("Hồng Lôi"))
            .andExpect(jsonPath("$.content[0].injuryBodyPart").value("Chân trước trái"));
        var page = ArgumentCaptor.forClass(Pageable.class);
        verify(locks).findAll(any(Specification.class), page.capture());
        assertThat(page.getValue().getPageSize()).isEqualTo(1);
        assertThat(page.getValue().getSort().getOrderFor("lockedAt").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        assertThat(page.getValue().getSort().getOrderFor("lockId").getDirection())
            .isEqualTo(Sort.Direction.DESC);
        mvc.perform(get("/api/veterinarian/training-locks").param("lockLevel", "INVALID"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void detailAndHorseHistoryReturn404WhenMissing() throws Exception {
        mvc.perform(get("/api/veterinarian/training-locks/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lockId").value(1))
            .andExpect(jsonPath("$.lockedByName").value("Lê Thu Hà"));
        mvc.perform(get("/api/veterinarian/training-locks/999"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/veterinarian/horses/3/training-locks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].lockId").value(1));
        mvc.perform(get("/api/veterinarian/horses/999/training-locks"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createsFullLockAndSynchronizesOnlyHorseFlag() throws Exception {
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":3,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"Theo dõi\",\"injuryId\":1}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.lockId").value(42))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
        var order = inOrder(horses, locks);
        order.verify(horses).lockForTrainingLocks(3);
        order.verify(locks).saveAndFlush(any(TrainingLock.class));
        order.verify(horses).updateTrainingLocked(3, true);
        assertThat(horse.getHealthStatus()).isEqualTo(Horse.HealthStatus.INJURED);
        assertThat(horse.getReadinessStatus()).isEqualTo(Horse.ReadinessStatus.RESTING);
    }

    @Test
    void permitsMultipleActiveLocksIncludingHeavyOnly() throws Exception {
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":3,\"lockLevel\":\"HEAVY_ONLY\","
                    + "\"reason\":\"Hạn chế tập nặng\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.lockLevel").value("HEAVY_ONLY"));
        verify(horses).updateTrainingLocked(3, true);
    }

    @Test
    void validatesHorseVeterinarianAndInjuryOwnership() throws Exception {
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":999,\"lockedBy\":3,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"Theo dõi\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":999,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"Theo dõi\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":4,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"Theo dõi\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":3,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"Theo dõi\",\"injuryId\":999}"))
            .andExpect(status().isNotFound());
        var otherHorse = new Horse();
        otherHorse.setHorseId(2);
        injury.getMedicalRecord().setHorse(otherHorse);
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":3,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"Theo dõi\",\"injuryId\":1}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void invalidBodyAndWriteConflictHaveAppropriateCodes() throws Exception {
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":3,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"  \"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":3,\"lockLevel\":\"UNKNOWN\","
                    + "\"reason\":\"Theo dõi\"}"))
            .andExpect(status().isBadRequest());
        when(locks.saveAndFlush(any(TrainingLock.class)))
            .thenThrow(new DataIntegrityViolationException("simulated FK race"));
        mvc.perform(post("/api/veterinarian/training-locks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"horseId\":3,\"lockedBy\":3,\"lockLevel\":\"FULL\","
                    + "\"reason\":\"Theo dõi\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void releasesActiveLockAndClearsFlagWhenItWasLast() throws Exception {
        mvc.perform(patch("/api/veterinarian/training-locks/1/release")
                .contentType(MediaType.APPLICATION_JSON).content("{\"releasedBy\":3}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RELEASED"))
            .andExpect(jsonPath("$.releasedBy").value(3));
        assertThat(lock.getReleasedAt()).isNotNull();
        var order = inOrder(horses, locks);
        order.verify(horses).lockForTrainingLocks(3);
        order.verify(locks).saveAndFlush(lock);
        order.verify(locks).existsByHorse_HorseIdAndStatus(3, TrainingLock.Status.ACTIVE);
        order.verify(horses).updateTrainingLocked(3, false);
        assertThat(horse.getHealthStatus()).isEqualTo(Horse.HealthStatus.INJURED);
        assertThat(horse.getReadinessStatus()).isEqualTo(Horse.ReadinessStatus.RESTING);
    }

    @Test
    void releasingOneOfMultipleActiveLocksLeavesHorseLocked() throws Exception {
        when(locks.existsByHorse_HorseIdAndStatus(3, TrainingLock.Status.ACTIVE))
            .thenReturn(true);
        mvc.perform(patch("/api/veterinarian/training-locks/1/release")
                .contentType(MediaType.APPLICATION_JSON).content("{\"releasedBy\":3}"))
            .andExpect(status().isOk());
        verify(horses).updateTrainingLocked(3, true);
    }

    @Test
    void rejectsAlreadyReleasedLockAndInvalidReleaser() throws Exception {
        lock.setStatus(TrainingLock.Status.RELEASED);
        mvc.perform(patch("/api/veterinarian/training-locks/1/release")
                .contentType(MediaType.APPLICATION_JSON).content("{\"releasedBy\":3}"))
            .andExpect(status().isConflict());
        lock.setStatus(TrainingLock.Status.ACTIVE);
        mvc.perform(patch("/api/veterinarian/training-locks/999/release")
                .contentType(MediaType.APPLICATION_JSON).content("{\"releasedBy\":3}"))
            .andExpect(status().isNotFound());
        mvc.perform(patch("/api/veterinarian/training-locks/1/release")
                .contentType(MediaType.APPLICATION_JSON).content("{\"releasedBy\":4}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/veterinarian/training-locks/1/release")
                .contentType(MediaType.APPLICATION_JSON).content("{\"releasedBy\":999}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void eligibilityFullAndStrictestOfMultipleLocksRestrictAllIntensities() throws Exception {
        var heavyOnly = secondActiveLock(TrainingLock.LockLevel.HEAVY_ONLY);
        when(locks.findByHorse_HorseIdAndStatusOrderByLockedAtDescLockIdDesc(3,
            TrainingLock.Status.ACTIVE)).thenReturn(List.of(heavyOnly, lock));
        mvc.perform(get("/api/veterinarian/horses/3/training-eligibility"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.healthStatus").value("INJURED"))
            .andExpect(jsonPath("$.readinessStatus").value("RESTING"))
            .andExpect(jsonPath("$.activeLocks.length()").value(2))
            .andExpect(jsonPath("$.restrictedByLocks.light").value(true))
            .andExpect(jsonPath("$.restrictedByLocks.moderate").value(true))
            .andExpect(jsonPath("$.restrictedByLocks.heavy").value(true));
    }

    @Test
    void eligibilityHeavyOnlyRestrictsOnlyHeavy() throws Exception {
        lock.setLockLevel(TrainingLock.LockLevel.HEAVY_ONLY);
        mvc.perform(get("/api/veterinarian/horses/3/training-eligibility"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.restrictedByLocks.light").value(false))
            .andExpect(jsonPath("$.restrictedByLocks.moderate").value(false))
            .andExpect(jsonPath("$.restrictedByLocks.heavy").value(true));
    }

    @Test
    void noActiveLockDoesNotClaimMedicalClearance() throws Exception {
        when(locks.findByHorse_HorseIdAndStatusOrderByLockedAtDescLockIdDesc(3,
            TrainingLock.Status.ACTIVE)).thenReturn(List.of());
        horse.setTrainingLocked(false);
        mvc.perform(get("/api/veterinarian/horses/3/training-eligibility"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activeLocks.length()").value(0))
            .andExpect(jsonPath("$.restrictedByLocks.heavy").value(false))
            .andExpect(jsonPath("$.healthStatus").value("INJURED"))
            .andExpect(jsonPath("$.readinessStatus").value("RESTING"));
    }

    @Test
    void transactionalProxyRollsBackWhenFlagCannotBeSynchronized() {
        when(horses.updateTrainingLocked(3, true)).thenReturn(0);
        var manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        var factory = new ProxyFactory(service);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new TransactionInterceptor(manager,
            new AnnotationTransactionAttributeSource()));
        var transactional = (VeterinarianTrainingLockService) factory.getProxy();
        var request = new CreateTrainingLockRequest(3, 3, TrainingLock.LockLevel.FULL,
            "Theo dõi", null, null);
        assertThatThrownBy(() -> transactional.create(request))
            .isInstanceOf(TrainingLockConflictException.class);
        verify(manager).rollback(any(TransactionStatus.class));
        verify(manager, never()).commit(any(TransactionStatus.class));
    }

    @Test
    void releaseTransactionRollsBackWhenFlagCannotBeSynchronized() {
        when(horses.updateTrainingLocked(3, false)).thenReturn(0);
        var manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        var factory = new ProxyFactory(service);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new TransactionInterceptor(manager,
            new AnnotationTransactionAttributeSource()));
        var transactional = (VeterinarianTrainingLockService) factory.getProxy();
        assertThatThrownBy(() -> transactional.release(1, new ReleaseTrainingLockRequest(3)))
            .isInstanceOf(TrainingLockConflictException.class);
        verify(manager).rollback(any(TransactionStatus.class));
        verify(manager, never()).commit(any(TransactionStatus.class));
    }

    private TrainingLock secondActiveLock(TrainingLock.LockLevel level) {
        var second = new TrainingLock();
        second.setLockId(2);
        second.setHorse(horse);
        second.setLockedBy(lock.getLockedBy());
        second.setLockLevel(level);
        second.setReason("Hạn chế tập nặng");
        second.setLockedAt(LocalDateTime.of(2026, 10, 8, 9, 0));
        second.setStatus(TrainingLock.Status.ACTIVE);
        return second;
    }
}
