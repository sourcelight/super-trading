package com.rick.supertrading.web.controller;

import com.rick.supertrading.domain.service.StatsService;
import com.rick.supertrading.domain.service.dto.ChoiceStatsView;
import com.rick.supertrading.security.CurrentUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** GREEN-vs-RED aggregation for dashboards, ownership-scoped. Spec §6.3. */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService stats;
    private final CurrentUser currentUser;

    public StatsController(StatsService stats, CurrentUser currentUser) {
        this.stats = stats;
        this.currentUser = currentUser;
    }

    @GetMapping("/choices")
    public ChoiceStatsView choices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Instant upper = to != null ? to : Instant.now();
        Instant lower = from != null ? from : upper.minus(7, ChronoUnit.DAYS);
        return stats.choices(currentUser.require(), lower, upper);
    }
}
