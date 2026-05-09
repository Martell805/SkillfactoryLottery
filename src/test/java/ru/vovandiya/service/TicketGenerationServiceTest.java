package ru.vovandiya.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.dto.LotteryFormat;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TicketGenerationServiceTest {

    @Inject
    TicketGenerationService ticketGenerationService;

    @Inject
    LotteryNumbersService numbersService;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        Ticket.deleteAll();
        DrawResult.deleteAll();
        Draw.deleteAll();
    }

    @Test
    @Transactional
    void generateTicketsShouldCreateRequiredNumberOfTickets() {
        Draw draw = createDraw("1-10:7", 100000);
        LotteryFormat format = new LotteryFormat(1, 10, 7);

        ticketGenerationService.generateTickets(draw, format, 10);

        long count = Ticket.count("draw", draw);

        assertEquals(10, count);
    }

    @Test
    @Transactional
    void generatedTicketsShouldContainCorrectCountOfNumbers() {
        Draw draw = createDraw("1-10:7", 100000);
        LotteryFormat format = new LotteryFormat(1, 10, 7);

        ticketGenerationService.generateTickets(draw, format, 10);

        List<Ticket> tickets = Ticket.list("draw", draw);

        for (Ticket ticket : tickets) {
            List<Integer> numbers = numbersService.parseNumbers(ticket.getPickedNumbers());

            assertEquals(7, numbers.size());
        }
    }

    @Test
    @Transactional
    void generatedTicketsShouldContainUniqueNumbers() {
        Draw draw = createDraw("1-10:7", 100000);
        LotteryFormat format = new LotteryFormat(1, 10, 7);

        ticketGenerationService.generateTickets(draw, format, 10);

        List<Ticket> tickets = Ticket.list("draw", draw);

        for (Ticket ticket : tickets) {
            List<Integer> numbers = numbersService.parseNumbers(ticket.getPickedNumbers());
            Set<Integer> uniqueNumbers = Set.copyOf(numbers);

            assertEquals(numbers.size(), uniqueNumbers.size());
        }
    }

    @Test
    @Transactional
    void generatedTicketsShouldContainNumbersFromAllowedRange() {
        Draw draw = createDraw("1-10:7", 100000);
        LotteryFormat format = new LotteryFormat(1, 10, 7);

        ticketGenerationService.generateTickets(draw, format, 10);

        List<Ticket> tickets = Ticket.list("draw", draw);

        for (Ticket ticket : tickets) {
            List<Integer> numbers = numbersService.parseNumbers(ticket.getPickedNumbers());

            for (Integer number : numbers) {
                assertTrue(number >= 1);
                assertTrue(number <= 10);
            }
        }
    }

    @Test
    @Transactional
    void generateTicketsShouldDoNothingWhenTicketsCountIsZero() {
        Draw draw = createDraw("1-10:7", 100000);
        LotteryFormat format = new LotteryFormat(1, 10, 7);

        ticketGenerationService.generateTickets(draw, format, 0);

        assertEquals(0, Ticket.count("draw", draw));
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
}