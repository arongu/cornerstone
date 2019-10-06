package cornerstone.workflow.cliconfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgumentParserTest {
    @Test
    @DisplayName("ArgumentParser: first element not switch, should throw IllegalArgumentException.")
    public void configToolArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            String[] args =  "myArgs --reuseIndex --dateRange".split("\\s+");
            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.readArguments(args);
        });
    }

    @Test
    @DisplayName("ArgumentParser: first element illegal switch, should throw IllegalArgumentException.")
    public void configToolArgumentTest2() {
        assertThrows(IllegalArgumentException.class, () -> {
            String[] args =  "--reuseIndex ".split("\\s+");
            ArgumentParser argumentParser = new ArgumentParser();
            argumentParser.readArguments(args);
        });
    }
}