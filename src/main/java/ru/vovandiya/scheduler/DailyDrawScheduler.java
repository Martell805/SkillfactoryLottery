package ru.vovandiya.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import ru.vovandiya.service.lottery.DrawService;

@ApplicationScoped
public class DailyDrawScheduler {

    @Inject
    DrawService drawService;

    @ConfigProperty(name = "lottery.daily.format")
    String dailyFormat;

    @ConfigProperty(name = "lottery.daily.prise-pool")
    Integer dailyPrisePool;

    @ConfigProperty(name = "lottery.daily.instantaneous")
    Boolean dailyInstantaneous;

    @Scheduled(cron = "{lottery.daily-draw.cron}")
    void createDailyDraw() {
        drawService.createDailyDraw(
                dailyFormat,
                dailyPrisePool,
                dailyInstantaneous
        );
    }
}