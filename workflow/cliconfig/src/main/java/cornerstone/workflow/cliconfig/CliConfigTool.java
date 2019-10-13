package cornerstone.workflow.cliconfig;

import cornerstone.workflow.lib.config.ConfigEncryptDecrypt;

import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Tool to easily encrypt and decrypt key value pairs in configuration files.
 */
public class CliConfigTool {
    private static final String helpMessage =
            "This tool encrypts 'ENC_' prefixed and decrypts 'AES_' prefixed key, value pairs in a config file with the given AES key." +
                    "\nThe first line of the key file should store the AES key as a base64 string." +
                    "\n\nUsage:" +
                    "\njava -jar cli.jar <enc|e|dec|d> <--config-file|-c> CONFIG_FILE <--key-file|-k> KEY_FILE <--save|-s> SAVE_TO" +
                    "\njava -jar cli.jar enc -c raw.conf -k key.txt -s encrypted.conf" +
                    "\njava -jar cli.jar d -c encrypted.conf -k key.txt -s decrypted.txt";

    private String command, keyFile, configFile, saveTo;

    // Reads arguments passed from the command line.
    private void readArguments(final String[] args) {
        if ( args == null || args.length != 7) {
            System.err.println("Not enough arguments!");
            System.out.println(helpMessage);
            System.exit(1);
        }

        // The first argument should be the command i.e.: enc/e, dec/d
        command = args[0];
        if ( ! (command.equals("enc") || command.equals("e") || command.equals("dec") || command.equals("d"))) {
            System.err.println("First argument most be <enc|e|dec|d> !");
            System.exit(2);
        }

        // A command line switch should be followed by a value,
        // First the switch is parsed then the value
        boolean expectingValue = false;
        String commandLineSwitch = null;
        for ( int i = 1; i < args.length; i++){
            String arg = args[i];

            if ( ! expectingValue && arg != null && arg.startsWith("-")) {
                expectingValue = true;
                commandLineSwitch = arg;

            } else if (expectingValue){
                switch (commandLineSwitch){
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
            final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile(cliConfigTool.keyFile);

            switch (cliConfigTool.command){
                case "enc": case "e": {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cliConfigTool.saveTo))) {
                        final List<String> lines = ConfigEncryptDecrypt.encryptConfig(key, cliConfigTool.configFile);
                        for (String line : lines) {
                            bufferedWriter.write(line + "\n");
                        }
                    }
                    break;
                }

                case "dec": case "d": {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cliConfigTool.saveTo))) {
                        final Properties properties = ConfigEncryptDecrypt.decryptConfig(key, cliConfigTool.configFile);
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
