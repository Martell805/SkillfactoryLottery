package ru.vovandiya.service.lottery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class TicketGenerationService {

    @Inject
    LotteryNumbersService numbersService;

    public void generateTickets(Draw draw, LotteryFormat format, int ticketsCount) {
        if (ticketsCount <= 0) {
            return;
        }

        for (int i = 0; i < ticketsCount; i++) {
            List<Integer> pickedNumbers = generatePickedNumbers(format);

            Ticket ticket = Ticket.builder()
                    .draw(draw)
                    .operation(null)
                    .pickedNumbers(numbersService.toString(pickedNumbers))
                    .prize(null)
                    .build();

            ticket.persist();
        }
    }

    private List<Integer> generatePickedNumbers(LotteryFormat format) {
        List<Integer> availableNumbers = new ArrayList<>();

        for (int number = format.minNumber(); number <= format.maxNumber(); number++) {
            availableNumbers.add(number);
        }

        Collections.shuffle(availableNumbers);

        return availableNumbers.subList(0, format.numbersToDraw());
    }
}