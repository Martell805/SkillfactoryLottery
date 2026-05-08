package ru.vovandiya.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Ticket;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PrizeCalculationServiceTest {

    @Inject
    PrizeCalculationService prizeCalculationService;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        Ticket.deleteAll();
        DrawResult.deleteAll();
        Draw.deleteAll();
    }

    @Test
    @Transactional
    void calculatePrizesShouldDistributePrizePoolBySquareWeights() {
        Draw draw = createDraw("1-10:7", 100000);

        Ticket ticketA = createTicket(draw, "1,2,8,9,10,3,4"); // 4 matches, weight 16
        Ticket ticketB = createTicket(draw, "1,2,3,4,5,8,9");  // 5 matches, weight 25
        Ticket ticketC = createTicket(draw, "1,2,3,4,5,6,7");  // 7 matches, weight 49

        List<Integer> drawnNumbers = List.of(1, 2, 3, 4, 5, 6, 7);

        prizeCalculationService.calculatePrizes(draw, drawnNumbers);

        assertEquals(17777, ticketA.getPrize());
        assertEquals(27777, ticketB.getPrize());
        assertEquals(54446, ticketC.getPrize());

        int total = ticketA.getPrize() + ticketB.getPrize() + ticketC.getPrize();
        assertEquals(100000, total);
    }

    @Test
    @Transactional
    void calculatePrizesShouldSetZeroPrizeWhenMatchesLessThanTwo() {
        Draw draw = createDraw("1-10:7", 100000);

        Ticket ticket = createTicket(draw, "1,8,9,10,3,4,5");

        List<Integer> drawnNumbers = List.of(1, 2, 6, 7, 11, 12, 13);

        prizeCalculationService.calculatePrizes(draw, drawnNumbers);

        assertEquals(0, ticket.getPrize());
    }

    @Test
    @Transactional
    void calculatePrizesShouldSetZeroWhenThereAreNoWinners() {
        Draw draw = createDraw("1-10:7", 100000);

        Ticket ticketA = createTicket(draw, "1,8,9,10,11,12,13");
        Ticket ticketB = createTicket(draw, "2,8,9,10,11,12,13");

        List<Integer> drawnNumbers = List.of(1, 2, 3, 4, 5, 6, 7);

        prizeCalculationService.calculatePrizes(draw, drawnNumbers);

        assertEquals(0, ticketA.getPrize());
        assertEquals(0, ticketB.getPrize());
    }

    @Test
    @Transactional
    void calculatePrizesShouldSplitEquallyWhenWeightsAreEqual() {
        Draw draw = createDraw("1-10:7", 999);

        Ticket ticketA = createTicket(draw, "1,2,3,8,9,10,4"); // 4 matches
        Ticket ticketB = createTicket(draw, "1,2,3,8,9,10,4"); // 4 matches
        Ticket ticketC = createTicket(draw, "1,2,3,8,9,10,4"); // 4 matches

        List<Integer> drawnNumbers = List.of(1, 2, 3, 4, 5, 6, 7);

        prizeCalculationService.calculatePrizes(draw, drawnNumbers);

        assertEquals(333, ticketA.getPrize());
        assertEquals(333, ticketB.getPrize());
        assertEquals(333, ticketC.getPrize());

        int total = ticketA.getPrize() + ticketB.getPrize() + ticketC.getPrize();
        assertEquals(999, total);
    }

    @Test
    @Transactional
    void calculatePrizesShouldHandleEmptyTicketList() {
        Draw draw = createDraw("1-10:7", 100000);

        assertDoesNotThrow(
                () -> prizeCalculationService.calculatePrizes(
                        draw,
                        List.of(1, 2, 3, 4, 5, 6, 7)
                )
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