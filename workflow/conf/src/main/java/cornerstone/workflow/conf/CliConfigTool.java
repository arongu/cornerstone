package cornerstone.workflow.conf;

public class CliConfigTool {
    public static void main(String[] args) {
        final ConfigTool configTool = new ConfigTool();
        try {
            configTool.readArguments(args);
        } catch (IllegalArgumentException e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
