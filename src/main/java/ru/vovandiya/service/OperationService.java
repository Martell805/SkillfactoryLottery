package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.OperationRequest;
import ru.vovandiya.model.Operation;

import java.util.List;

@ApplicationScoped
public class OperationService {

    @Inject
    AdminUserService adminUserService;

    public List<Operation> listAll() {
        return Operation.listAll();
    }

    public Operation findById(Long id) {
        return (Operation) Operation.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Operation not found", 404));
    }

    @Transactional
    public Operation create(OperationRequest request) {
        Operation operation = Operation.builder()
                .user(adminUserService.findById(request.userId()))
                .timestamp(request.timestamp())
                .build();
        operation.persist();
        return operation;
    }

    @Transactional
    public Operation update(Long id, OperationRequest request) {
        Operation operation = findById(id);
        operation.setUser(adminUserService.findById(request.userId()));
        operation.setTimestamp(request.timestamp());
        return operation;
    }

    @Transactional
    public void delete(Long id) {
        Operation operation = findById(id);
        operation.delete();
    }
}
