package bg.sofia.uni.fmi.mjt.commandinterpreter;

import bg.sofia.uni.fmi.mjt.walletmanager.WalletManager;

public class CommandInterpreter {
    private Command command;

    private static final int SECOND_PARAMETER = 3;
    private static final int FIRST_PARAMETER = 2;

    private String getParameter(String userInput) {
        if (userInput.contains("--offering=")) {
            return userInput.substring(userInput.indexOf("--offering=") + "--offering=".length());
        }
        if (userInput.contains("--money=")) {
            return userInput.substring(userInput.indexOf("--money=") + "--money=".length());
        }
        //if more user commands that need handling are added, the respective if statements should be added.
        return userInput;
    }

    public CommandInterpreter(WalletManager manager, String userInput) {
        String[] keywords = userInput.strip().split(" ");
        if (keywords[0].equals("register")) {
            command = new RegisterUserCommand(manager, keywords[1], keywords[2]);
            return;
        }
        if (keywords[0].equals("login")) {
            command = new LoginCommand(manager, keywords[1], keywords[2]);
            return;
        }

        //Commands that are not login or register will have the following order:
        //First word is the username, second word is the user command and the rest are the user parameters(if any).
        String username = keywords[0];
        command = switch (keywords[1]) {
            case "deposit-money" -> new DepositMoneyCommand(manager, username, Double.parseDouble(keywords[2]));
            case "list-offerings" -> new ListOfferingsCommand(manager, username);
            case "buy" -> new BuyCommand(manager, username, getParameter(keywords[FIRST_PARAMETER]),
                Double.parseDouble(getParameter(keywords[SECOND_PARAMETER])));
            case "sell" -> new SellCommand(manager, username, getParameter(keywords[FIRST_PARAMETER]));
            case "get-wallet-summary" -> new GetWalletSummaryCommand(manager, username);
            case "get-wallet-overall-summary" -> new GetWalletOverallSummaryCommand(manager, username);
            default -> new IncorrectCommand();
        };
    }

    public Command getCommand() {
        return command;
    }
}
