package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Announcement;
import com.nomos.inventory.service.model.AnnouncementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Announcement.
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * Busca todos los anuncios activos en una fecha y hora específicas.
     * @param dateTime La fecha y hora para verificar la vigencia.
     * @return Lista de anuncios activos.
     */
    List<Announcement> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime dateTime, LocalDateTime anotherDateTime);

    /**
     * Busca todos los anuncios activos por tipo.
     * @param type El tipo de anuncio.
     * @return Lista de anuncios que coinciden con el tipo y están activos.
     */
    List<Announcement> findByIsActiveTrueAndType(AnnouncementType type);
}
