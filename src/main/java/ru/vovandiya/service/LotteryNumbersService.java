package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

@ApplicationScoped
public class LotteryNumbersService {

    public List<Integer> parseNumbers(String numbers) {
        if (numbers == null || numbers.isBlank()) {
            return List.of();
        }

        return Arrays.stream(numbers.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Integer::parseInt)
                .toList();
    }

    public Set<Integer> parseNumbersToSet(String numbers) {
        return new HashSet<>(parseNumbers(numbers));
    }

    public String toString(Collection<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return "";
        }

        return numbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    public void validateNumbers(String numbers, LotteryFormat format) {
        List<Integer> parsed = parseNumbers(numbers);

        if (parsed.size() != format.numbersToDraw()) {
            throw new IllegalArgumentException("Picked numbers count must be " + format.numbersToDraw());
        }

        Set<Integer> unique = Set.copyOf(parsed);

        if (unique.size() != parsed.size()) {
            throw new IllegalArgumentException("Picked numbers must be unique");
        }

        for (Integer number : parsed) {
            if (number < format.minNumber() || number > format.maxNumber()) {
                throw new IllegalArgumentException("Number out of allowed range: " + number);
            }
        }
    }
}