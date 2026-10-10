package com.horsemanagement.mapper;

import com.horsemanagement.dto.HealthMetricDto;
import com.horsemanagement.dto.HorseDetailDto;
import com.horsemanagement.dto.HorseOverviewDto;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.HorseHealthMetric;
import org.springframework.stereotype.Component;

@Component
public class VeterinarianHorseMapper {
    public HorseOverviewDto toOverview(Horse horse) {
        return new HorseOverviewDto(horse.getHorseId(), horse.getName(), horse.getBreed(),
            horse.getGender(), horse.getHealthStatus(), horse.getReadinessStatus(),
            horse.isTrainingLocked(), horse.getOwner().getFullName(),
            horse.getManager() == null ? null : horse.getManager().getFullName());
    }

    public HorseDetailDto toDetail(Horse horse, HorseHealthMetric latestMetric) {
        return new HorseDetailDto(horse.getHorseId(), horse.getName(), horse.getRegistrationNo(),
            horse.getMicrochipNo(), horse.getBreed(), horse.getGender(), horse.getColor(),
            horse.getDateOfBirth(), horse.getCountryOfOrigin(), horse.getHeightCm(),
            horse.getCurrentWeightKg(), horse.getHealthStatus(), horse.getReadinessStatus(),
            horse.isTrainingLocked(), horse.getStatus(), horse.getPhotoUrl(),
            horse.getOwner().getFullName(),
            horse.getManager() == null ? null : horse.getManager().getFullName(),
            latestMetric == null ? null : toMetric(latestMetric));
    }

    public HealthMetricDto toMetric(HorseHealthMetric metric) {
        return new HealthMetricDto(metric.getMetricId(), metric.getWeightKg(),
            metric.getRestingHeartRate(), metric.getBodyTemperature(),
            metric.getRespiratoryRate(), metric.getRecordedAt(), metric.getNotes());
    }
}
