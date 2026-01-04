package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.StoreSchedule;
import com.nomos.inventory.service.model.DayOfWeek; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {

    
    Optional<StoreSchedule> findByDayOfWeek(DayOfWeek dayOfWeek);
}
