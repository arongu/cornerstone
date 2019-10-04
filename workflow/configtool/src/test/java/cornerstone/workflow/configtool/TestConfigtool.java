package cornerstone.workflow.configtool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TestConfigtool {
    @ParameterizedTest
    @ValueSource(strings = {"egy", "ketto"})
    public void testRun(){
        final Configtool configtool = new Configtool();
    }

    @Test
    public void mainTest() throws Exception {
        String programArgs = "myArgs --reuseIndex --dateRange";
        Configtool.main(programArgs.split("\\s+"));

    }
}
