import models.Expense;
import models.User;
import services.BalanceService;
import services.ExpenseService;
import services.UserService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SplitWise {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream("src/input.txt"));

        UserService userService = new UserService();
        BalanceService balanceService = new BalanceService(userService);
        ExpenseService expenseService = new ExpenseService(userService, balanceService);

        userService.addUser("u1", "User1", "user1@split.com", "9957955269");
        userService.addUser("u2", "User2", "user2@split.com", "9957955269");
        userService.addUser("u3", "User3", "user3@split.com", "9957955269");
        userService.addUser("u4", "User4", "user4@split.com", "9957955269");

        while (scanner.hasNext()) {
            List<String> commandLine = List.of(scanner.nextLine().split(" "));
            if (commandLine.isEmpty()) continue;
            String command = commandLine.get(0);
            if (command.equalsIgnoreCase("SHOW")) {
                System.out.println(commandLine.stream().reduce(SplitWise::joinString).get());
                parseAndExecuteShowCommand(commandLine, userService, balanceService);
                System.out.println();
            } else if (command.equalsIgnoreCase("EXPENSE")) {
                parseAndExecuteExpenseCommand(commandLine, expenseService);
            } else if (command.equalsIgnoreCase("PASSBOOK")) {
                System.out.println(commandLine.stream().reduce(SplitWise::joinString).get());
                parseAndExecutePassbookCommand(commandLine, expenseService, userService);
                System.out.println();
            } else {
                System.out.println("Invalid Command: " + command);
                System.out.println();
            }
        }
    }

    private static String joinString(String s1, String s2) {
        return s1 + " " + s2;
    }

    private static void parseAndExecuteShowCommand(List<String> commandLine, UserService userService, BalanceService balanceService) {
        if (commandLine.size() > 1) {
            String userId = commandLine.get(1);
            User user = userService.findUser(userId);
            if (user == null) {
                System.out.println("User doesn't exist");
            }
            printBalances(user, balanceService.getUserBalances(userId));
        } else {
            printBalances(balanceService.getAllBalances());
        }
    }

    private static void parseAndExecuteExpenseCommand(List<String> commandLine, ExpenseService expenseService) {
        String loanerId;
        double totalAmount;
        int numUsers;
        List<String> loaneeIds = new ArrayList<>();
        String expenseType;
        List<Double> shares = new ArrayList<>();
        String name = "";
        String note = "";
        String imageUri = "";

        try {
            loanerId = commandLine.get(1);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Id of person who paid is not present");
            return;
        }

        try {
            totalAmount = Double.parseDouble(commandLine.get(2));
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Amount not present");
            return;
        } catch (NumberFormatException ex) {
            System.out.println("Amount should be a double");
            return;
        }

        try {
            numUsers = Integer.parseInt(commandLine.get(3));
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Number of users not present");
            return;
        } catch (NumberFormatException ex) {
            System.out.println("Number of users should be an int");
            return;
        }

        for (int i = 0; i < numUsers; i++) {
            try {
                loaneeIds.add(commandLine.get(4 + i));
            } catch (IndexOutOfBoundsException ex) {
                System.out.println("Required number of users not present");
                return;
            }
        }

        try {
            expenseType = commandLine.get(4 + numUsers);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Expense type not present");
            return;
        }

        if (!expenseType.equalsIgnoreCase("EQUAL")) {
            for (int i = 0; i < numUsers; i++) {
                try {
                    shares.add(Double.parseDouble(commandLine.get(5 + numUsers + i)));
                } catch (IndexOutOfBoundsException ex) {
                    System.out.println("Required number of shares not present");
                    return;
                } catch (NumberFormatException ex) {
                    System.out.println("Shares should be of double type");
                    return;
                }
            }

            if (commandLine.size() > 5 + 2 * numUsers) {
                name = commandLine.get(5 + 2 * numUsers);
            }

            if (commandLine.size() > 6 + 2 * numUsers) {
                note = commandLine.get(6 + 2 * numUsers);
            }

            if (commandLine.size() > 7 + 2 * numUsers) {
                imageUri = commandLine.get(7 + 2 * numUsers);
            }
        } else {
            if (commandLine.size() > 5 + numUsers) {
                name = commandLine.get(5 + numUsers);
            }

            if (commandLine.size() > 6 + numUsers) {
                note = commandLine.get(6 + numUsers);
            }

            if (commandLine.size() > 7 + numUsers) {
                imageUri = commandLine.get(7 + numUsers);
            }
        }

        try {
            expenseService.addExpense(loanerId, totalAmount, loaneeIds, expenseType, shares, name, note, imageUri);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private static void printBalances(Map<User, Map<User, Double>> balances) {
        if (balances.isEmpty()) {
            System.out.println("No balances");
        }
        for (Map.Entry<User, Map<User, Double>> entry : balances.entrySet()) {
            printBalances(entry.getKey(), entry.getValue());
        }
    }

    private static void printBalances(User user, Map<User, Double> balances) {
        if (balances.isEmpty()) {
            System.out.println("No balances");
        }
        for (Map.Entry<User, Double> entry : balances.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.printf("%s owes %s: %.2f\n", entry.getKey().getName(), user.getName(), entry.getValue());
            } else {
                System.out.printf("%s owes %s: %.2f\n", user.getName(), entry.getKey().getName(), -entry.getValue());
            }
        }
    }

    private static void parseAndExecutePassbookCommand(List<String> commandLine, ExpenseService expenseService, UserService userService) {
        try {
            String userId = commandLine.get(1);
            List<Expense> expenses = expenseService.getUserExpenses(userId);
            User user = userService.findUser(userId);
            for (Expense expense : expenses) {
                printExpense(user, expense);
            }
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("User id not present");
        }
    }

    private static void printExpense(User user, Expense expense) {
        if (expense.getName() != null && !expense.getName().isBlank()) {
            System.out.println(expense.getName());
        } else {
            System.out.println("Expense #" + expense.getExpenseId().toString());
        }
        System.out.println("\tAmount: " + expense.getAmount());
        double userOweAmount = ExpenseService.getUserOweAmountInExpense(expense, user.getUserId());
        if (userOweAmount > 0) {
            System.out.printf("\t%s is owed %.2f\n", user.getName(), userOweAmount);
        } else if (userOweAmount < 0) {
            System.out.printf("\t%s owes %.2f\n", user.getName(), -userOweAmount);
        } else {
            System.out.printf("\t%s does not participate in this expense", user.getName());
        }
    }
}