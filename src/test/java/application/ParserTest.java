package application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"correct_sample.txt", "larger_correct.txt"})
    @DisplayName("Parsing of valid files")
    void correctParsing(String filename) {
        Parser parser = new Parser();
        Assertions.assertDoesNotThrow(() -> {
            parser.parseFile(filename);
        });

        // check if the file is parsed as expected
        Deque<Character> referenceStack = createReferenceStack(filename);
        assertTrue(compareDeque(parser.getElementStack(), referenceStack));
    }

    @Test
    @DisplayName("Check if the correct exception is thrown }")
    void missingClosingBracket() {
        ParsingException thrown = Assertions.assertThrows(ParsingException.class, () -> {
            new Parser().parseFile("missing_closed_bracket.txt");
        });

        assertEquals("Error parsing the file. } expected", thrown.getMessage());
    }

    @Test
    @DisplayName("Check if the correct exception is thrown {")
    void missingOpeningBracket() {
        ParsingException thrown = Assertions.assertThrows(ParsingException.class, () -> {
            new Parser().parseFile("missing_open_bracket.txt");
        });

        assertEquals("Error parsing the file. { expected", thrown.getMessage());
    }

    @Test
    @DisplayName("Error should be thrown in larger files too")
    void wrongParsingLargerFile() {
        ParsingException thrown = Assertions.assertThrows(ParsingException.class, () -> {
            new Parser().parseFile("larger_incorrect.txt");
        });
    }

    private boolean compareDeque(Deque<Character> deque1, Deque<Character> deque2) {
        if (deque1.size() != deque2.size()) {
            return false;
        }

        while (!deque1.isEmpty()) {
            char character1 = deque1.getFirst();
            char character2 = deque2.getFirst();
            deque1.pop();
            deque2.pop();

            if (character1 != character2) {
                return false;
            }
        }
        return true;
    }

    private Deque<Character> createReferenceStack(String filename) {
        try {
            Path path = Paths.get(Objects.requireNonNull(Parser.class.getClassLoader().getResource(filename)).toURI());
            return createReferenceStack(filename.toCharArray().length, path);
        } catch (URISyntaxException e) {
            System.err.println(e.getMessage());
        }
        return new ArrayDeque<>();
    }

    private Deque<Character> createReferenceStack(int defaultSize, Path path) {
        try (Stream<String> lineStream = Files.lines(path)) {
            Deque<Character> referenceStack = new ArrayDeque<>(defaultSize);
            lineStream.forEach(line -> {
                for (char c : line.toCharArray()) {
                    referenceStack.add(c);
                }
            });

            return referenceStack;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayDeque<>();
    }
}