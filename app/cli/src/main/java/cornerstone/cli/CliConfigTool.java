package cornerstone.cli;

import cornerstone.lib.config.ConfigEncryptDecrypt;
import cornerstone.lib.crypto.AESEncryptDecrypt;
import cornerstone.lib.crypto.AESEncryptDecrypt.AESToolException;

import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.List;

/**
 * Tool to easily encrypt and decrypt key value pairs in configuration files.
 */
public class CliConfigTool {
    private static final String helpMessage =
            "This tool encrypts 'ENC_' prefixed and decrypts 'AES_' prefixed key, value pairs in a config file with the given AES key." +
                    "\nThe first line of the key file should store the AES key as a base64 string." +
                    "\n\nTo generate a CBC-AES-256 HMAC key run:" +
                    "\n  java -jar cli.jar g|gen --password <password> --salt <salt> --save|-s <SAVE_TO>" +
                    "\n  java -jar cli.jar   gen --password 'password' --salt 'abcdef0123456790' --save key.conf" +
                    "\n\nUsage:" +
                    "\n  java -jar cli.jar enc|e|dec|d --config-file|-c <CONFIG_FILE> --key-file|-k <KEY_FILE> --save|-s <SAVE_TO>" +
                    "\n  java -jar cli.jar enc -c rawconfig.conf -k key.txt -s encrypted.conf" +
                    "\n  java -jar cli.jar   d -c encrypted.conf -k key.txt -s decrypted.txt";


    private String keyFile, configFile, saveTo;
    private String command, password, salt;

    /**
     * Reads arguments passed from the command line.
     * @param args Arguments passed from the command line.
     */
    public void readArguments(final String[] args) {
        if ( args == null || args.length != 7) {
            System.err.println("Not enough arguments!\n");
            System.out.println(helpMessage);
            System.exit(1);
        }

        // The first argument should be the command i.e.: enc/e, dec/d
        command = args[0];
        if (! (command.equals("enc") || command.equals("e")  ||
               command.equals("dec") || command.equals("d")  ||
               command.equals("gen") || command.equals("g"))) {

            System.err.println("First argument must be <enc|e|dec|d> !");
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
                    case "--config-file" : case "-c" : {
                        this.configFile = arg;
                        break;
                    }

                    case "--key-file" : case "-k" : {
                        this.keyFile = arg;
                        break;
                    }

                    case "--save" : case "-s" : {
                        this.saveTo = arg;
                        break;
                    }

                    case "--password" : case "-p" : {
                        this.password = arg;
                        break;
                    }

                    case "--salt" : case "-sa" : {
                        this.salt = arg;
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
        final CliConfigTool cli = new CliConfigTool();
        cli.readArguments(args);

        try {
            switch (cli.command){
                case "enc" : case "e" : {
                    final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile(cli.keyFile);
                    try ( final BufferedWriter bw = new BufferedWriter(new FileWriter(cli.saveTo))) {
                        final List<String> lines = ConfigEncryptDecrypt.encryptConfig(key, cli.configFile);
                        for (final String line : lines) {
                            bw.write(line + "\n");
                        }
                    }
                    break;
                }

                case "dec" : case "d" : {
                    final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile(cli.keyFile);
                    try ( final BufferedWriter bw = new BufferedWriter(new FileWriter(cli.saveTo))) {
                        ConfigEncryptDecrypt.decryptConfig(key, cli.configFile).store(bw, null);
                    }
                    break;
                }

                case "gen" : case "g" : {
                    try ( final BufferedWriter bw = new BufferedWriter(new FileWriter(cli.saveTo))) {
                        try {
                            final Key keySpec = AESEncryptDecrypt.derive256BitAESKeyWithHmacSHA256(cli.password, cli.salt);
                            final String base64key = Base64.getEncoder().encodeToString(keySpec.getEncoded());
                            bw.write(base64key);

                        } catch (final AESToolException e) {
                            System.err.println(e.getMessage());
                            System.exit(4);
                        }
                    }
                    break;
                }

                default: {
                    System.err.println("Unrecognized command '" + cli.command + "'");
                    System.exit(5);
                }
            }

        } catch (final IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(6);
        }
    }
}
