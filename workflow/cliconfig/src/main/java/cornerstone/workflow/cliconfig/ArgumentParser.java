package cornerstone.workflow.cliconfig;

public class ArgumentParser {
    private String configFile, keyFile, mode;

    public String getConfigFile() {
        return configFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getMode() {
        return mode;
    }

    public void readArguments(final String[] args) throws IllegalArgumentException{
        if ( args == null || args.length == 0){
            throw new IllegalArgumentException("No argument to work with!");
        }

        if ( args.length != 6){
            throw new IllegalArgumentException("Argument number mismatch!");
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
}
