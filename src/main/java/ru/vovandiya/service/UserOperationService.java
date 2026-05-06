package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.User;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class UserOperationService {

    /**
     * Получить историю операций текущего пользователя за указанный период.
     */
    public List<Map<String, Object>> getMyOperations(User currentUser,
                                                     LocalDateTime from,
                                                     LocalDateTime to) {
        validateRange(from, to);
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        conditions.add("user.id = :userId");
        params.put("userId", currentUser.id);

        if (from != null) {
            conditions.add("timestamp >= :from");
            params.put("from", from);
        }
        if (to != null) {
            conditions.add("timestamp <= :to");
            params.put("to", to);
        }

        String where = String.join(" AND ", conditions);
        String jpql = where + " ORDER BY timestamp DESC, id DESC";
        List<Operation> operations = Operation.<Operation>find(jpql, params).list();

        List<Map<String, Object>> result = new ArrayList<>(operations.size());
        for (Operation o : operations) {
            result.add(operationToMap(o));
        }
        return result;
    }

    private void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new WebApplicationException("'from' must be before or equal to 'to'", 400);
        }
    }

    private Map<String, Object> operationToMap(Operation o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.id);
        m.put("timestamp", o.getTimestamp());
        return m;
    }
}
