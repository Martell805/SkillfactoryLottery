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
        requirePositiveId(id, "id");
        return (Operation) Operation.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Operation not found", 404));
    }

    @Transactional
    public Operation create(OperationRequest request) {
        validateRequest(request);
        Operation operation = Operation.builder()
                .user(adminUserService.findById(requirePositiveId(request.userId(), "userId")))
                .timestamp(requireTimestamp(request.timestamp()))
                .build();
        operation.persist();
        return operation;
    }

    @Transactional
    public Operation update(Long id, OperationRequest request) {
        validateRequest(request);
        Operation operation = findById(id);
        operation.setUser(adminUserService.findById(requirePositiveId(request.userId(), "userId")));
        operation.setTimestamp(requireTimestamp(request.timestamp()));
        return operation;
    }

    @Transactional
    public void delete(Long id) {
        Operation operation = findById(id);
        operation.delete();
    }

    private void validateRequest(OperationRequest request) {
        if (request == null) {
            throw new WebApplicationException("Request body is required", 400);
        }
    }

    private Long requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new WebApplicationException(fieldName + " must be positive", 400);
        }
        return id;
    }

    private java.time.LocalDateTime requireTimestamp(java.time.LocalDateTime timestamp) {
        if (timestamp == null) {
            throw new WebApplicationException("timestamp is required", 400);
        }
        return timestamp;
    }
}
