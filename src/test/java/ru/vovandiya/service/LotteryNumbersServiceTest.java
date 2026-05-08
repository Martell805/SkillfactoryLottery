package ru.vovandiya.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LotteryNumbersServiceTest {

    @Inject
    LotteryNumbersService lotteryNumbersService;

    @Test
    void parseNumbersShouldReturnEmptyListWhenInputIsNull() {
        List<Integer> result = lotteryNumbersService.parseNumbers(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void parseNumbersShouldReturnEmptyListWhenInputIsBlank() {
        List<Integer> result = lotteryNumbersService.parseNumbers("   ");

        assertTrue(result.isEmpty());
    }

    @Test
    void parseNumbersShouldParseCommaSeparatedNumbers() {
        List<Integer> result = lotteryNumbersService.parseNumbers("1,2,3,10");

        assertEquals(List.of(1, 2, 3, 10), result);
    }

    @Test
    void parseNumbersShouldIgnoreSpaces() {
        List<Integer> result = lotteryNumbersService.parseNumbers("1, 2, 3, 10");

        assertEquals(List.of(1, 2, 3, 10), result);
    }

    @Test
    void parseNumbersShouldThrowWhenNumberIsInvalid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryNumbersService.parseNumbers("1,2,a")
        );
    }

    @Test
    void parseNumbersToSetShouldRemoveDuplicates() {
        Set<Integer> result = lotteryNumbersService.parseNumbersToSet("1,2,2,3");

        assertEquals(Set.of(1, 2, 3), result);
    }

    @Test
    void toStringShouldConvertNumbersToCommaSeparatedString() {
        String result = lotteryNumbersService.toString(List.of(1, 2, 3));

        assertEquals("1,2,3", result);
    }

    @Test
    void toStringShouldReturnEmptyStringWhenCollectionIsEmpty() {
        String result = lotteryNumbersService.toString(List.of());

        assertEquals("", result);
    }

    @Test
    void validateNumbersShouldPassForCorrectNumbers() {
        LotteryFormat format = new LotteryFormat(1, 10, 5);

        assertDoesNotThrow(
                () -> lotteryNumbersService.validateNumbers("1,2,3,4,5", format)
        );
    }

    @Test
    void validateNumbersShouldThrowWhenCountIsWrong() {
        LotteryFormat format = new LotteryFormat(1, 10, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryNumbersService.validateNumbers("1,2,3", format)
        );
    }

    @Test
    void validateNumbersShouldThrowWhenNumbersAreDuplicated() {
        LotteryFormat format = new LotteryFormat(1, 10, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryNumbersService.validateNumbers("1,2,2,3,4", format)
        );
    }

    @Test
    void validateNumbersShouldThrowWhenNumberIsOutOfRange() {
        LotteryFormat format = new LotteryFormat(1, 10, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryNumbersService.validateNumbers("1,2,3,4,11", format)
        );
    }
}