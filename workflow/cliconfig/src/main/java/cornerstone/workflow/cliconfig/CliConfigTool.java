package cornerstone.workflow.cliconfig;

import cornerstone.workflow.lib.config.ConfigReaderWriter;

import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CliConfigTool {
    private static final String helpMessage = "Usage: java -jar cliconfig --config-file|-c <configFile> --key-file|-k <keyFile> --mode|m <enc|dec>";

    private String keyFile, configFile, mode, saveTo;

    private void readArguments(final String[] args) {
        if ( args == null || args.length != 8){
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

                    case "--save": case "-s": {
                        this.saveTo = arg;
                        break;
                    }

                    default : {
                        System.err.println("Illegal option: '" + arg + "'");
                        System.exit(2);
                    }
                }

                expectingValue = false;
            }
        }
    }

    private void saveConfig(final List<String> lines){
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveTo))){;
            for(String line : lines){
                bufferedWriter.write(line + "\n");
            }
        } catch (IOException e){
            System.err.println(e.getMessage());
            System.exit(3);
        }
    }

    public static void main(String[] args) {
        final CliConfigTool cliConfigTool = new CliConfigTool();
        cliConfigTool.readArguments(args);

        try {
            final SecretKey key = ConfigReaderWriter.loadAESKeyFromFile(cliConfigTool.keyFile);

            switch (cliConfigTool.mode){
                case "enc": case "e": {
                    final List<String> list = ConfigReaderWriter.encryptConfig(key, cliConfigTool.configFile);
                    cliConfigTool.saveConfig(list);
                    break;
                }

                case "dec": case "d": {
                    try {
                        final Properties properties = ConfigReaderWriter.decryptConfig(key, cliConfigTool.configFile);
                        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cliConfigTool.saveTo))) {
                            properties.store(bufferedWriter, null);
                        }
                    }
                    catch (IOException e){
                        System.err.println(e.getMessage());
                        System.exit(4);
                    }
                    break;
                }
            }

        } catch (IOException e){
            System.err.println(e.getMessage());
            System.exit(5);
        }
    }
}
