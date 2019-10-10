package cornerstone.workflow.cliconfig;

import cornerstone.workflow.lib.config.ConfigReaderWriter;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Base64;

public class CliConfigTool {
    public static void main(String[] args) {
        final ArgumentParser argumentParser = new ArgumentParser();
        try {
            argumentParser.readArguments(args);
            String configFile = argumentParser.getConfigFile();
            String keyFile =  argumentParser.getKeyFile();
            String mode = argumentParser.getMode();

            final SecretKey key = ConfigReaderWriter.loadAESKeyFromFile(keyFile);
            System.out.println(Hex.encodeHexString(key.getEncoded()));


        } catch (IllegalArgumentException e){
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e){
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }
}
