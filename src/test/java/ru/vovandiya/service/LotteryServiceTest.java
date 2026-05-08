package ru.vovandiya.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.dto.DrawNextResponse;
import ru.vovandiya.dto.DrawRemainingResponse;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Ticket;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LotteryServiceTest {

    @Inject
    LotteryService lotteryService;

    @Inject
    LotteryNumbersService lotteryNumbersService;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        Ticket.deleteAll();
        DrawResult.deleteAll();
        Draw.deleteAll();
    }

    @Test
    @Transactional
    void drawNextBarrelShouldMoveStatusFromNewToStarted() {
        Draw draw = createDraw("1-10:3", 100000);
        createDrawResult(draw, "", DrawStatus.NEW);

        DrawNextResponse response = lotteryService.drawNextBarrel(draw.id);

        DrawResult result = DrawResult.find("draw", draw).firstResult();
        List<Integer> drawnNumbers = lotteryNumbersService.parseNumbers(result.getDrawnNumbers());

        assertEquals(DrawStatus.STARTED, response.status());
        assertEquals(DrawStatus.STARTED, result.getStatus());
        assertEquals(1, drawnNumbers.size());
        assertNotNull(response.drawnNumber());
    }

    @Test
    @Transactional
    void drawNextBarrelShouldCompleteDrawWhenRequiredCountIsReached() {
        Draw draw = createDraw("1-3:2", 999);
        createDrawResult(draw, "", DrawStatus.NEW);

        createTicket(draw, "1,2");
        createTicket(draw, "1,3");
        createTicket(draw, "2,3");

        lotteryService.drawNextBarrel(draw.id);
        DrawNextResponse secondResponse = lotteryService.drawNextBarrel(draw.id);

        DrawResult result = DrawResult.find("draw", draw).firstResult();
        List<Integer> drawnNumbers = lotteryNumbersService.parseNumbers(result.getDrawnNumbers());

        assertEquals(DrawStatus.COMPLETE, secondResponse.status());
        assertEquals(DrawStatus.COMPLETE, result.getStatus());
        assertEquals(2, drawnNumbers.size());
        assertEquals(2, new HashSet<>(drawnNumbers).size());
    }

    @Test
    @Transactional
    void drawRemainingBarrelsShouldCompleteDrawAndCalculatePrizes() {
        Draw draw = createDraw("1-3:3", 999);
        createDrawResult(draw, "", DrawStatus.NEW);

        Ticket ticketA = createTicket(draw, "1,2,3");
        Ticket ticketB = createTicket(draw, "1,2,3");
        Ticket ticketC = createTicket(draw, "1,2,3");

        DrawRemainingResponse response = lotteryService.drawRemainingBarrels(draw.id);

        DrawResult result = DrawResult.find("draw", draw).firstResult();
        List<Integer> drawnNumbers = lotteryNumbersService.parseNumbers(result.getDrawnNumbers());

        assertEquals(DrawStatus.COMPLETE, response.status());
        assertEquals(DrawStatus.COMPLETE, result.getStatus());

        assertEquals(3, drawnNumbers.size());
        assertEquals(3, new HashSet<>(drawnNumbers).size());
        assertTrue(drawnNumbers.containsAll(List.of(1, 2, 3)));

        assertEquals(333, ticketA.getPrize());
        assertEquals(333, ticketB.getPrize());
        assertEquals(333, ticketC.getPrize());
    }

    @Test
    @Transactional
    void drawRemainingBarrelsShouldReturnEmptyNewNumbersWhenDrawAlreadyComplete() {
        Draw draw = createDraw("1-3:3", 999);
        createDrawResult(draw, "1,2,3", DrawStatus.COMPLETE);

        DrawRemainingResponse response = lotteryService.drawRemainingBarrels(draw.id);

        assertEquals(DrawStatus.COMPLETE, response.status());
        assertTrue(response.newNumbers().isEmpty());
        assertEquals(List.of(1, 2, 3), response.drawnNumbers());
    }

    @Test
    @Transactional
    void drawNextBarrelShouldThrowWhenDrawIsAlreadyComplete() {
        Draw draw = createDraw("1-3:3", 999);
        createDrawResult(draw, "1,2,3", DrawStatus.COMPLETE);

        assertThrows(
                IllegalStateException.class,
                () -> lotteryService.drawNextBarrel(draw.id)
        );
    }

    @Test
    @Transactional
    void drawNextBarrelShouldThrowWhenDrawDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryService.drawNextBarrel(999999L)
        );
    }

    @Test
    @Transactional
    void drawNextBarrelShouldThrowWhenDrawResultDoesNotExist() {
        Draw draw = createDraw("1-10:3", 100000);

        assertThrows(
                IllegalStateException.class,
                () -> lotteryService.drawNextBarrel(draw.id)
        );
    }

    @Test
    @Transactional
    void drawNextBarrelShouldThrowWhenDrawnNumbersContainDuplicates() {
        Draw draw = createDraw("1-10:3", 100000);
        createDrawResult(draw, "1,1", DrawStatus.STARTED);

        assertThrows(
                IllegalStateException.class,
                () -> lotteryService.drawNextBarrel(draw.id)
        );
    }

    @Test
    @Transactional
    void drawNextBarrelShouldThrowWhenDrawnNumberIsOutOfRange() {
        Draw draw = createDraw("1-10:3", 100000);
        createDrawResult(draw, "1,20", DrawStatus.STARTED);

        assertThrows(
                IllegalStateException.class,
                () -> lotteryService.drawNextBarrel(draw.id)
        );
    }

    @Test
    @Transactional
    void drawNextBarrelShouldThrowWhenThereAreTooManyDrawnNumbers() {
        Draw draw = createDraw("1-10:3", 100000);
        createDrawResult(draw, "1,2,3,4", DrawStatus.STARTED);

        assertThrows(
                IllegalStateException.class,
                () -> lotteryService.drawNextBarrel(draw.id)
        );
    }

    private Draw createDraw(String format, Integer prisePool) {
        Draw draw = Draw.builder()
                .format(format)
                .isInstantaneous(false)
                .isScheduled(false)
                .drawDate(LocalDateTime.now())
                .prisePool(prisePool)
                .build();

        draw.persist();
        return draw;
    }

    private DrawResult createDrawResult(Draw draw, String drawnNumbers, DrawStatus status) {
        DrawResult result = DrawResult.builder()
                .draw(draw)
                .drawnNumbers(drawnNumbers)
                .status(status)
                .build();

        result.persist();
        return result;
    }

    private Ticket createTicket(Draw draw, String pickedNumbers) {
        Ticket ticket = Ticket.builder()
                .draw(draw)
                .operation(null)
                .pickedNumbers(pickedNumbers)
                .prize(null)
                .build();

        ticket.persist();
        return ticket;
    }
}