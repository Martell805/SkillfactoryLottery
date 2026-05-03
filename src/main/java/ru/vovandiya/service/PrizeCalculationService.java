package ru.vovandiya.service.lottery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.Ticket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PrizeCalculationService {

    private static final int MIN_MATCHES_FOR_PRIZE = 2;

    @Inject
    LotteryNumbersService numbersService;

    @Transactional
    public void calculatePrizes(Draw draw, List<Integer> drawnNumbers) {
        List<Ticket> tickets = Ticket.list("draw", draw);

        Set<Integer> drawnSet = new HashSet<>(drawnNumbers);
        List<TicketPrizeInfo> winners = new ArrayList<>();

        for (Ticket ticket : tickets) {
            Set<Integer> picked = numbersService.parseNumbersToSet(ticket.getPickedNumbers());

            int matches = countMatches(picked, drawnSet);
            int weight = getPrizeWeight(matches);

            if (weight > 0) {
                winners.add(new TicketPrizeInfo(ticket, matches, weight));
            } else {
                ticket.setPrize(0);
            }
        }

        if (winners.isEmpty()) {
            for (Ticket ticket : tickets) {
                ticket.setPrize(0);
            }
            return;
        }

        int prizePool = draw.getPrisePool() == null ? 0 : draw.getPrisePool();
        int totalWeight = winners.stream()
                .mapToInt(TicketPrizeInfo::weight)
                .sum();

        int distributed = 0;

        for (TicketPrizeInfo winner : winners) {
            int prize = prizePool * winner.weight() / totalWeight;
            winner.ticket().setPrize(prize);
            distributed += prize;
        }

        int remainder = prizePool - distributed;

        if (remainder > 0) {
            TicketPrizeInfo bestWinner = winners.stream()
                    .max(
                            Comparator.comparingInt(TicketPrizeInfo::matches)
                                    .thenComparingInt(TicketPrizeInfo::weight)
                    )
                    .orElse(null);

            if (bestWinner != null) {
                bestWinner.ticket().setPrize(bestWinner.ticket().getPrize() + remainder);
            }
        }
    }

    private int countMatches(Set<Integer> picked, Set<Integer> drawn) {
        int count = 0;

        for (Integer number : picked) {
            if (drawn.contains(number)) {
                count++;
            }
        }

        return count;
    }

    private int getPrizeWeight(int matches) {
        if (matches < MIN_MATCHES_FOR_PRIZE) {
            return 0;
        }

        return matches * matches;
        };
    }

    private record TicketPrizeInfo(
            Ticket ticket,
            int matches,
            int weight
    ) {
    }
}