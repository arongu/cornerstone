package cornerstone.workflow.cliconfig;

import cornerstone.workflow.lib.config.ConfigEncrypterDecrypter;

import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CliConfigTool {
    private static final String helpMessage =
            "Usage:\njava -jar cli.jar <enc|e|dec|d> <--config-file|-c> CONFIG_FILE <--key-file|-k> KEY_FILE <--save|-s> SAVE_TO" +
                    "\njava -jar cli.jar enc -c raw.conf -k key.txt -s encrypted.conf" +
                    "\njava -jar cli.jar d -c encrypted.conf -k key.txt -s decrypted.txt";

    private String command, keyFile, configFile, saveTo;

    private void readArguments(final String[] args) {
        if ( args == null || args.length != 7) {
            System.err.println("Not enough arguments!");
            System.out.println(helpMessage);
            System.exit(1);
        }

        command = args[0];
        if ( ! (command.equals("enc") || command.equals("e") || command.equals("dec") || command.equals("d"))) {
            System.err.println("First argument most be <enc|e|dec|d> !");
            System.exit(2);
        }


        boolean expectingValue = false;
        String switchArg = null;
        for ( int i = 1; i < args.length; i++){
            String arg = args[i];

            if ( ! expectingValue && arg != null && arg.startsWith("-")) {
                expectingValue = true;
                switchArg = arg;

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

                    case "--save": case "-s": {
                        this.saveTo = arg;
                        break;
                    }

                    default : {
                        System.err.println("Illegal option: '" + arg + "'");
                        System.exit(3);
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
            final SecretKey key = ConfigEncrypterDecrypter.loadAESKeyFromFile(cliConfigTool.keyFile);

            switch (cliConfigTool.command){
                case "enc": case "e": {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cliConfigTool.saveTo))) {
                        final List<String> lines = ConfigEncrypterDecrypter.encryptConfig(key, cliConfigTool.configFile);
                        for (String line : lines) {
                            bufferedWriter.write(line + "\n");
                        }
                    }
                    break;
                }

                case "dec": case "d": {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cliConfigTool.saveTo))) {
                        final Properties properties = ConfigEncrypterDecrypter.decryptConfig(key, cliConfigTool.configFile);
                        properties.store(bufferedWriter, null);
                    }
                    break;
                }

                default: {
                    System.err.println("Unrecognized command '" + cliConfigTool.command + "'");
                    System.exit(4);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(5);
        }
    }
}
