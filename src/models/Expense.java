package models;

import java.util.Map;
import java.util.UUID;

public class Expense {
    private final UUID expenseId;
    private final double amount;
    private final ExpenseType expenseType;
    private final String spenderUserId;
    private final Map<String, Double> amountMap;

    public Expense(double amount, ExpenseType expenseType, String spenderUserId, Map<String, Double> amountMap) {
        this.expenseId = UUID.randomUUID();
        this.amount = amount;
        this.expenseType = expenseType;
        this.spenderUserId = spenderUserId;
        this.amountMap = amountMap;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public double getAmount() {
        return amount;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public String getSpenderUserId() {
        return spenderUserId;
    }

    public Map<String, Double> getAmountMap() {
        return amountMap;
    }

    public enum ExpenseType {
        EQUAL,
        EXACT,
        PERCENT
    }

}
