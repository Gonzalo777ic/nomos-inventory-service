package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Announcement;
import com.nomos.inventory.service.model.AnnouncementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    
    List<Announcement> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime dateTime, LocalDateTime anotherDateTime);

    
    List<Announcement> findByIsActiveTrueAndType(AnnouncementType type);
}
