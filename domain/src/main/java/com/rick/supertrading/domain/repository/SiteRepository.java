package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {

    Optional<Site> findByBaseUrl(String baseUrl);

    boolean existsByBaseUrl(String baseUrl);
}
