package cornerstone.workflow.cliconfig;

public class CliConfigTool {
    public static void main(String[] args) {
        final ArgumentParser argumentParser = new ArgumentParser();
        try {
            argumentParser.readArguments(args);
        } catch (IllegalArgumentException e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
