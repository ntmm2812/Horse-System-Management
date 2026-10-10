package com.horsemanagement.service;

import com.horsemanagement.dto.HealthMetricDto;
import com.horsemanagement.dto.HorseDetailDto;
import com.horsemanagement.dto.HorseOverviewDto;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.exception.HorseNotFoundException;
import com.horsemanagement.mapper.VeterinarianHorseMapper;
import com.horsemanagement.repository.HorseHealthMetricRepository;
import com.horsemanagement.repository.HorseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VeterinarianHorseService {
    private final HorseRepository horseRepository;
    private final HorseHealthMetricRepository metricRepository;
    private final VeterinarianHorseMapper mapper;

    public VeterinarianHorseService(HorseRepository horseRepository,
                                    HorseHealthMetricRepository metricRepository,
                                    VeterinarianHorseMapper mapper) {
        this.horseRepository = horseRepository;
        this.metricRepository = metricRepository;
        this.mapper = mapper;
    }

    public PageResponse<HorseOverviewDto> listHorses(Horse.HealthStatus healthStatus,
                                                      int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("horseId").ascending());
        var horses = healthStatus == null
            ? horseRepository.findAll(pageable)
            : horseRepository.findByHealthStatus(healthStatus, pageable);
        return PageResponse.from(horses.map(mapper::toOverview));
    }

    public HorseDetailDto getHorse(Integer horseId) {
        var horse = horseRepository.findByHorseId(horseId)
            .orElseThrow(() -> new HorseNotFoundException(horseId));
        var latestMetric = metricRepository
            .findFirstByHorse_HorseIdOrderByRecordedAtDescMetricIdDesc(horseId).orElse(null);
        return mapper.toDetail(horse, latestMetric);
    }

    public PageResponse<HealthMetricDto> getHealthMetrics(Integer horseId, int page, int size) {
        if (!horseRepository.existsById(horseId)) {
            throw new HorseNotFoundException(horseId);
        }
        var pageable = PageRequest.of(page, size,
            Sort.by(Sort.Order.desc("recordedAt"), Sort.Order.desc("metricId")));
        return PageResponse.from(metricRepository.findByHorse_HorseId(horseId, pageable)
            .map(mapper::toMetric));
    }
}
