package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.Ticket;
import ru.vovandiya.model.User;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class UserTicketService {

    @Inject
    DrawService drawService;

    /**
     * Покупка подготовленного заранее билета по id.
     * Создаётся Operation и привязывается к билету.
     */
    @Transactional
    public Ticket buyExistingTicket(Long ticketId, User currentUser) {
        Ticket ticket = findTicketById(ticketId);
        if (ticket.getOperation() != null) {
            throw new WebApplicationException("Ticket is already purchased", 409);
        }
        Operation operation = Operation.builder()
                .user(currentUser)
                .timestamp(LocalDateTime.now())
                .build();
        operation.persist();
        ticket.setOperation(operation);
        return ticket;
    }

    /**
     * Покупка билета с созданием в рамках тиража.
     * pickedNumbers — выбранные пользователем числа (например "5,12,33").
     */
    @Transactional
    public Ticket buyNewTicket(Long drawId, String pickedNumbers, User currentUser) {
        if (pickedNumbers == null || pickedNumbers.isBlank()) {
            throw new WebApplicationException("pickedNumbers is required", 400);
        }
        Draw draw = drawService.findById(requirePositiveId(drawId, "drawId"));

        Operation operation = Operation.builder()
                .user(currentUser)
                .timestamp(LocalDateTime.now())
                .build();
        operation.persist();

        Ticket ticket = Ticket.builder()
                .draw(draw)
                .operation(operation)
                .pickedNumbers(pickedNumbers.trim())
                .prize(0)
                .build();
        ticket.persist();
        return ticket;
    }

    /**
     * Получить свои билеты с фильтрами по времени покупки и тиражу.
     */
    public List<Map<String, Object>> getMyTickets(User currentUser,
                                                  Long drawId,
                                                  LocalDateTime from,
                                                  LocalDateTime to) {
        validateRange(from, to);
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        conditions.add("operation.user.id = :userId");
        params.put("userId", currentUser.id);

        if (drawId != null) {
            conditions.add("draw.id = :drawId");
            params.put("drawId", drawId);
        }
        if (from != null) {
            conditions.add("operation.timestamp >= :from");
            params.put("from", from);
        }
        if (to != null) {
            conditions.add("operation.timestamp <= :to");
            params.put("to", to);
        }

        String where = String.join(" AND ", conditions);
        String jpql = where + " ORDER BY operation.timestamp DESC, id DESC";
        List<Ticket> tickets = Ticket.<Ticket>find(jpql, params).list();

        List<Map<String, Object>> result = new ArrayList<>(tickets.size());
        for (Ticket t : tickets) {
            result.add(ticketToMap(t));
        }
        return result;
    }

    private Ticket findTicketById(Long ticketId) {
        return (Ticket) Ticket.findByIdOptional(requirePositiveId(ticketId, "ticketId"))
                .orElseThrow(() -> new WebApplicationException("Ticket not found", 404));
    }

    private Long requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new WebApplicationException(fieldName + " must be positive", 400);
        }
        return id;
    }

    private void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new WebApplicationException("'from' must be before or equal to 'to'", 400);
        }
    }

    private Map<String, Object> ticketToMap(Ticket t) {
        Map<String, Object> m = new LinkedHashMap<>();
        Draw d = t.getDraw();
        Operation op = t.getOperation();
        m.put("id", t.id);
        m.put("drawId", d != null ? d.id : null);
        m.put("drawFormat", d != null ? d.getFormat() : null);
        m.put("drawDate", d != null ? d.getDrawDate() : null);
        m.put("operationId", op != null ? op.id : null);
        m.put("purchasedAt", op != null ? op.getTimestamp() : null);
        m.put("pickedNumbers", t.getPickedNumbers());
        m.put("prize", t.getPrize());
        return m;
    }
}
