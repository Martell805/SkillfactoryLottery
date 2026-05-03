package ru.vovandiya.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.service.lottery.LotteryService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ApplicationScoped
public class DailyLotteryScheduler {

    @Inject
    LotteryService lotteryService;

    @Transactional
    @Scheduled(cron = "{lottery.daily-run.cron}")
    void runDailyLottery() {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        Draw draw = Draw.find(
                "isScheduled = ?1 and drawDate >= ?2 and drawDate < ?3 order by drawDate desc",
                true,
                startOfDay,
                startOfNextDay
        ).firstResult();

        if (draw == null) {
            return;
        }

        DrawResult result = DrawResult.find("draw", draw).firstResult();

        if (result == null || result.getStatus() == DrawStatus.COMPLETE) {
            return;
        }

        lotteryService.drawRemainingBarrels(draw.id);
    }
}