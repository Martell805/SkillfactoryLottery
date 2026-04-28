package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.TicketRequest;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.Ticket;

import java.util.List;

@ApplicationScoped
public class TicketService {

    @Inject
    DrawService drawService;

    public List<Ticket> listAll() {
        return Ticket.listAll();
    }

    public Ticket findById(Long id) {
        requirePositiveId(id, "id");
        return (Ticket) Ticket.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Ticket not found", 404));
    }

    @Transactional
    public Ticket create(TicketRequest request) {
        validateRequest(request);
        Operation operation = null;
        if (request.operationId() != null) {
            operation = findOperationById(request.operationId());
        }
        Ticket ticket = Ticket.builder()
                .draw(drawService.findById(requirePositiveId(request.drawId(), "drawId")))
                .operation(operation)
                .pickedNumbers(requireText(request.pickedNumbers(), "pickedNumbers"))
                .prize(requireNonNegative(request.prize(), "prize"))
                .build();
        ticket.persist();
        return ticket;
    }

    @Transactional
    public Ticket update(Long id, TicketRequest request) {
        validateRequest(request);
        Ticket ticket = findById(id);
        ticket.setDraw(drawService.findById(requirePositiveId(request.drawId(), "drawId")));
        if (request.operationId() != null) {
            ticket.setOperation(findOperationById(request.operationId()));
        } else {
            ticket.setOperation(null);
        }
        ticket.setPickedNumbers(requireText(request.pickedNumbers(), "pickedNumbers"));
        ticket.setPrize(requireNonNegative(request.prize(), "prize"));
        return ticket;
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = findById(id);
        ticket.delete();
    }

    private void validateRequest(TicketRequest request) {
        if (request == null) {
            throw new WebApplicationException("Request body is required", 400);
        }
    }

    private Operation findOperationById(Long operationId) {
        Long validatedId = requirePositiveId(operationId, "operationId");
        return (Operation) Operation.findByIdOptional(validatedId)
                .orElseThrow(() -> new WebApplicationException("Operation not found", 404));
    }

    private Long requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new WebApplicationException(fieldName + " must be positive", 400);
        }
        return id;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new WebApplicationException(fieldName + " is required", 400);
        }
        return value.trim();
    }

    private Integer requireNonNegative(Integer value, String fieldName) {
        if (value == null) {
            throw new WebApplicationException(fieldName + " is required", 400);
        }
        if (value < 0) {
            throw new WebApplicationException(fieldName + " must not be negative", 400);
        }
        return value;
    }
}
