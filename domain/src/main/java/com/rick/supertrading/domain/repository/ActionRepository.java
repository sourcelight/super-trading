package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.choice.Choice;
import com.rick.supertrading.domain.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {

    List<Action> findByExecutionIdOrderByActionTime(Long executionId);

    /**
     * GREEN-vs-RED aggregation for dashboards: counts per choice over a window.
     * Returns rows of {@code [Choice, Long]}.
     */
    @Query("""
            SELECT a.choice AS choice, COUNT(a) AS count
            FROM Action a
            WHERE a.actionTime BETWEEN :from AND :to
            GROUP BY a.choice
            """)
    List<ChoiceCount> countByChoiceBetween(@Param("from") Instant from, @Param("to") Instant to);

    /** Owner-scoped variant: only counts clicks reachable from the user's credentials. */
    @Query("""
            SELECT a.choice AS choice, COUNT(a) AS count
            FROM Action a
            WHERE a.actionTime BETWEEN :from AND :to
              AND a.execution.schedule.credential.owner.id = :ownerId
            GROUP BY a.choice
            """)
    List<ChoiceCount> countByChoiceForOwnerBetween(@Param("ownerId") Long ownerId,
                                                    @Param("from") Instant from,
                                                    @Param("to") Instant to);

    /** Projection for the choice aggregation query. */
    interface ChoiceCount {
        Choice getChoice();

        long getCount();
    }
}
