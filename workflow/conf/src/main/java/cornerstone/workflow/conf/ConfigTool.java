package cornerstone.workflow.conf;

import java.util.regex.Pattern;

public class ConfigTool {
    private static final Pattern patternEncrypt = Pattern.compile("^([a-zA-Z0-9-_]+)(?:\\s*)=(?:\\s*)ENC_(.+)$");
    private static final Pattern patternDecrypt = Pattern.compile("^([a-zA-Z0-9-_]+)(?:\\s*)=(?:\\s*)AES_(.+)$");

    private String configFile, keyFile;

    public void readArguments(final String[] args) throws IllegalArgumentException{
        boolean valueExpected = false;

        if (args == null){
            throw new IllegalArgumentException("No argument to work with!");
        }

        if ( args.length % 2 != 0){
            throw new IllegalArgumentException("Argument number mismatch!");
        }

        String switchArg = null;
        for ( String arg : args){
            if ( ! valueExpected && arg != null && arg.startsWith("--")){
                valueExpected = true;
                switchArg = arg;
            } else if (valueExpected){
                switch (switchArg){
                    case "--config-file" : {
                        this.configFile = arg;
                        break;
                    }

                    case "--key-file" : {
                        this.keyFile = arg;
                        break;
                    }

                    case "--help" : {
                        System.out.println("Help message....");
                        break;
                    }

                    default : {
                        throw new IllegalArgumentException("Illegal argument: '" + arg + "'");
                    }
                }

                valueExpected = false;
            }
        }
    }
}
