package cornerstone.workflow.conf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigToolTest {
    @Test
    @DisplayName("ConfigTool argument test, first element is not a switch, should throw IllegalArgumentException.")
    public void configToolArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            String[] args =  "myArgs --reuseIndex --dateRange".split("\\s+");
            ConfigTool configTool = new ConfigTool();
            configTool.readArguments(args);
        });
    }

    @Test
    @DisplayName("ConfigTool argument test, first element is an illegal switch, should throw IllegalArgumentException.")
    public void configToolArgumentTest2() {
        assertThrows(IllegalArgumentException.class, () -> {
            String[] args =  "--reuseIndex ".split("\\s+");
            ConfigTool configTool = new ConfigTool();
            configTool.readArguments(args);
        });
    }

    @Test
    @DisplayName("ConfigTool argument test, first element is an illegal switch, should throw IllegalArgumentException.")
    public void configToolArgumentTest3() {
        assertThrows(IllegalArgumentException.class, () -> {
            String[] args =  "--reuseIndex ".split("\\s+");
            ConfigTool configTool = new ConfigTool();
            configTool.readArguments(args);
        });
    }
}