package services;

import models.Expense;

import java.util.*;

public class ExpenseService {
    private final List<Expense> expenses;
    private final UserService userService;
    private final BalanceService balanceService;

    public ExpenseService(UserService userService, BalanceService balanceService) {
        this.expenses = new ArrayList<>();
        this.userService = userService;
        this.balanceService = balanceService;
    }

    public void addExpense(String spenderUserId, Double amount, List<String> shareHolders, String type, List<Double> shares, String name, String note, String imageUri) {
        validateUserId(spenderUserId);
        shareHolders.forEach(this::validateUserId);
        Expense.ExpenseType expenseType = getExpenseType(type);
        if (shareHolders.isEmpty()) {
            throw new IllegalArgumentException("Users list not provided");
        }
        if (shareHolders.size() != shares.size() && expenseType != Expense.ExpenseType.EQUAL) {
            throw new IllegalArgumentException("Shares list size should be equal to users list size");
        }
        validateShares(expenseType, shares, amount);
        Map<String, Double> amountMap = assignAmountToEachShareHolder(shareHolders, expenseType, amount, shares);
        Expense expense = new Expense(amount, name, expenseType, spenderUserId, amountMap, note, imageUri);
        expenses.add(expense);
        for (String userId : shareHolders) {
            if (!userId.equals(spenderUserId)) {
                balanceService.addBalance(spenderUserId, userId, amountMap.get(userId));
            }
        }
    }

    public Expense getExpense(UUID expenseId) {
        for (Expense e : expenses) {
            if (e.getExpenseId() == expenseId) {
                return e;
            }
        }
        return null;
    }

    private void validateUserId(String userId) {
        if (userService.findUser(userId) == null) throw new IllegalArgumentException("User id not correct");
    }

    private Expense.ExpenseType getExpenseType(String expenseType) {
        expenseType = expenseType.toUpperCase();
        switch (expenseType) {
            case "EXACT" -> {
                return Expense.ExpenseType.EXACT;
            }
            case "EQUAL" -> {
                return Expense.ExpenseType.EQUAL;
            }
            case "PERCENT" -> {
                return Expense.ExpenseType.PERCENT;
            }
            default -> {
                throw new IllegalArgumentException("Expense type is not correct");
            }
        }
    }

    private void validateShares(Expense.ExpenseType expenseType, List<Double> shares, double totalAmount) {
        if (expenseType == Expense.ExpenseType.EQUAL) {
            if (shares != null && !shares.isEmpty()) throw new IllegalArgumentException("Shares should be empty in case of equal expense type.");
            return;
        }
        if (shares == null || shares.isEmpty()) {
            throw new IllegalArgumentException("Shares are not given in case of non-equal expense");
        }
        if (expenseType == Expense.ExpenseType.EXACT) {
            Optional<Double> sum = shares.stream().reduce(Double::sum);
            if (sum.get() != totalAmount) {
                throw new IllegalArgumentException("Shares sum should be equal to totalAmount in case of exact expense type");
            }
            return;
        }
        if (expenseType == Expense.ExpenseType.PERCENT) {
            Optional<Double> sum = shares.stream().reduce(Double::sum);
            if (sum.get() != 100.00) {
                throw new IllegalArgumentException("Percentage sum should be equal to 100 in case of percent expense type");
            }
        }
    }

    private Map<String, Double> assignAmountToEachShareHolder(List<String> shareHolders, Expense.ExpenseType expenseType, double totalAmount, List<Double> shares) {
        switch (expenseType) {
            case PERCENT -> {
                return assignPercentWise(shareHolders, totalAmount, shares);
            }
            case EQUAL -> {
                return assignEqualAmount(shareHolders, totalAmount);
            }
            case EXACT -> {
                return assignExact(shareHolders, shares);
            }
        }
        throw new RuntimeException();
    }

    private Map<String, Double> assignEqualAmount(List<String> shareHolders, double totalAmount) {
        double shareAmount = totalAmount / shareHolders.size();
        Map<String, Double> balanceMap = new HashMap<>();
        for (String shareHolderId : shareHolders) {
            balanceMap.put(shareHolderId, shareAmount);
        }
        return balanceMap;
    }

    private Map<String, Double> assignPercentWise(List<String> shareHolders, double totalAmount, List<Double> percents) {
        Map<String, Double> balanceMap = new HashMap<>();
        for (int i = 0; i < shareHolders.size(); i++) {
            double amount = (totalAmount * percents.get(i)) / 100;
            balanceMap.put(shareHolders.get(i), amount);
        }
        return balanceMap;
    }

    private Map<String, Double> assignExact(List<String> shareHolders, List<Double> shares) {
        Map<String, Double> balanceMap = new HashMap<>();
        for (int i = 0; i < shareHolders.size(); i++) {
            balanceMap.put(shareHolders.get(i), shares.get(i));
        }
        return balanceMap;
    }
}
