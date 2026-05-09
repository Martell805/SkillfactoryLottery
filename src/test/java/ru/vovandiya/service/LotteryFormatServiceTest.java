package ru.vovandiya.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import ru.vovandiya.dto.LotteryFormat;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LotteryFormatServiceTest {

    @Inject
    LotteryFormatService lotteryFormatService;

    @Test
    void parseShouldReturnCorrectFormat() {
        LotteryFormat format = lotteryFormatService.parse("1-30:10");

        assertEquals(1, format.minNumber());
        assertEquals(30, format.maxNumber());
        assertEquals(10, format.numbersToDraw());
    }

    @Test
    void parseShouldTrimSpaces() {
        LotteryFormat format = lotteryFormatService.parse(" 1-10:7 ");

        assertEquals(1, format.minNumber());
        assertEquals(10, format.maxNumber());
        assertEquals(7, format.numbersToDraw());
    }

    @Test
    void parseShouldThrowWhenFormatIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse(null)
        );
    }

    @Test
    void parseShouldThrowWhenFormatIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse("   ")
        );
    }

    @Test
    void parseShouldThrowWhenFormatHasNoCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse("1-30")
        );
    }

    @Test
    void parseShouldThrowWhenRangeIsInvalid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse("30-1:10")
        );
    }

    @Test
    void parseShouldThrowWhenNumbersToDrawIsGreaterThanRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse("1-5:10")
        );
    }

    @Test
    void parseShouldThrowWhenNumbersAreNotNumeric() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse("a-b:c")
        );
    }

    @Test
    void parseShouldThrowWhenMinNumberIsZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse("0-10:5")
        );
    }

    @Test
    void parseShouldThrowWhenNumbersToDrawIsZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> lotteryFormatService.parse("1-10:0")
        );
    }
}