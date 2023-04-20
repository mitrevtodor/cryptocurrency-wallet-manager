package bg.sofia.uni.fmi.mjt.commandinterpreter;

public class IncorrectCommand implements Command {
    @Override
    public String execute() {
        return "Command is incorrect. Please enter a valid command.";
    }
}