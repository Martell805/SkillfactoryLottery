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
        return (Ticket) Ticket.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Ticket not found", 404));
    }

    @Transactional
    public Ticket create(TicketRequest request) {
        Operation operation = null;
        if (request.operationId() != null) {
            operation = (Operation) Operation.findByIdOptional(request.operationId())
                    .orElseThrow(() -> new WebApplicationException("Operation not found", 404));
        }
        Ticket ticket = Ticket.builder()
                .draw(drawService.findById(request.drawId()))
                .operation(operation)
                .pickedNumbers(request.pickedNumbers())
                .prize(request.prize())
                .build();
        ticket.persist();
        return ticket;
    }

    @Transactional
    public Ticket update(Long id, TicketRequest request) {
        Ticket ticket = findById(id);
        ticket.setDraw(drawService.findById(request.drawId()));
        if (request.operationId() != null) {
            ticket.setOperation((Operation) Operation.findByIdOptional(request.operationId())
                    .orElseThrow(() -> new WebApplicationException("Operation not found", 404)));
        } else {
            ticket.setOperation(null);
        }
        ticket.setPickedNumbers(request.pickedNumbers());
        ticket.setPrize(request.prize());
        return ticket;
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = findById(id);
        ticket.delete();
    }
}
