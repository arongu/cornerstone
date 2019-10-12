package cornerstone.workflow.cliconfig;

import cornerstone.workflow.lib.config.ConfigReaderWriter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CliConfigTool {
    private static final String helpMessage = "Usage: java -jar cliconfig --config-file|-c <configFile> --key-file|-k <keyFile> --mode|m <enc|dec>";

    private String keyFile, configFile, mode;

    public void readArguments(final String[] args) {
        if ( args == null || args.length != 6){
            System.err.println("Not enough arguments!");
            System.out.println(helpMessage);
            System.exit(1);
        }

        boolean expectingValue = false;
        String switchArg = null;

        for ( String arg : args){
            if ( ! expectingValue && arg != null && arg.startsWith("-")){
                expectingValue = true;
                switchArg = arg;    // the next argument should be a value an option usually followed by value
            } else if (expectingValue){
                switch (switchArg){
                    case "--config-file": case "-c": {
                        this.configFile = arg;
                        break;
                    }

                    case "--key-file" : case "-k": {
                        this.keyFile = arg;
                        break;
                    }

                    case "--mode": case "-m": {
                        this.mode = arg;
                        break;
                    }

                    default : {
                        throw new IllegalArgumentException("Illegal option: '" + arg + "'");
                    }
                }

                expectingValue = false;
            }
        }
    }

    public static void main(String[] args) {
        final CliConfigTool cliConfigTool = new CliConfigTool();
        cliConfigTool.readArguments(args);

        try {
            final SecretKey key = ConfigReaderWriter.loadAESKeyFromFile(cliConfigTool.keyFile);

            switch (cliConfigTool.mode){
                case "enc" : {
                    final List<String> list = ConfigReaderWriter.loadAndEncryptLines(key, cliConfigTool.configFile);
                    for (String em : list){
                        System.out.println(em);
                    }
                    break;
                }

                case "dec" : {
                    try {
                        final Properties properties = ConfigReaderWriter.loadEncryptedConfig(key, cliConfigTool.configFile);
//                        final Set<Map.Entry<Object, Object>> entries = properties.entrySet();
//                        for (Map.Entry<Object, Object> entry : entries) {
//                            System.out.println(entry.getKey() + " = " + entry.getValue());
//                        }
                    }
                    catch (IOException e){
                        System.err.println("gebasz " + e.getMessage());
                    }
                    break;
                }
            }

        } catch (IOException e){
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }
}
