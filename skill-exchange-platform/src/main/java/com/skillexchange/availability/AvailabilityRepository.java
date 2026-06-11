package com.skillexchange.availability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    List<Availability> findByUserId(UUID userId);
}
