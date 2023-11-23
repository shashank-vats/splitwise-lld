package models;

import java.util.Map;
import java.util.UUID;

public class Expense {
    private final UUID expenseId;

    private final String name;

    private final String note;

    private final String imageUri;
    private final double amount;
    private final ExpenseType expenseType;
    private final String spenderUserId;
    private final Map<String, Double> amountMap;

    public Expense(double amount, String name, ExpenseType expenseType, String spenderUserId, Map<String, Double> amountMap, String note, String imageUri) {
        this.expenseId = UUID.randomUUID();
        this.amount = amount;
        this.expenseType = expenseType;
        this.spenderUserId = spenderUserId;
        this.amountMap = amountMap;
        this.name = name;
        this.note = note;
        this.imageUri = imageUri;
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

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public String getImageUri() {
        return imageUri;
    }

    public enum ExpenseType {
        EQUAL,
        EXACT,
        PERCENT,
        SHARE
    }

}
