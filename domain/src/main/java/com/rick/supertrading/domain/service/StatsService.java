package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.choice.Choice;
import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.repository.ActionRepository;
import com.rick.supertrading.domain.repository.ActionRepository.ChoiceCount;
import com.rick.supertrading.domain.service.dto.ChoiceStatsView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * GREEN-vs-RED analytics for dashboards. ADMINs aggregate globally; regular users
 * see only clicks reachable from their own credentials.
 */
@Service
public class StatsService {

    private final ActionRepository actions;

    public StatsService(ActionRepository actions) {
        this.actions = actions;
    }

    @Transactional(readOnly = true)
    public ChoiceStatsView choices(AppUser user, Instant from, Instant to) {
        List<ChoiceCount> rows = user.isAdmin()
                ? actions.countByChoiceBetween(from, to)
                : actions.countByChoiceForOwnerBetween(user.getId(), from, to);

        long green = 0;
        long red = 0;
        for (ChoiceCount row : rows) {
            if (row.getChoice() == Choice.GREEN) {
                green = row.getCount();
            } else if (row.getChoice() == Choice.RED) {
                red = row.getCount();
            }
        }
        return new ChoiceStatsView(from, to, green, red);
    }
}
