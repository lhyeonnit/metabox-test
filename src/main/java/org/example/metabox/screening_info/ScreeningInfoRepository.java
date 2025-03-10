package org.example.metabox.screening_info;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScreeningInfoRepository extends JpaRepository<ScreeningInfo, Integer> {

    @Query("select si from ScreeningInfo si where si.screening.theater.id = :theaterId and si.date = :date")
    List<ScreeningInfo> findByTheaterId(@Param("theaterId") int theaterId, @Param("date") LocalDate date);
}
