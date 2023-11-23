package services;

import models.User;

import java.util.HashMap;
import java.util.Map;

public class BalanceService {
    private final Map<String, Map<String, Double>> owedMap;

    private final UserService userService;

    public BalanceService(UserService userService) {
        owedMap = new HashMap<>();
        this.userService = userService;
    }

    public void addBalance(String loanerId, String loaneeId, double amount) {
        if (loanerId.equals(loaneeId)) return;
        validateUserId(loanerId);
        validateUserId(loaneeId);
        double newBalance = amount;

        if (owedMap.containsKey(loanerId)) {
            if (owedMap.get(loanerId).containsKey(loaneeId)) {
                newBalance += owedMap.get(loanerId).get(loaneeId);
            }
            owedMap.get(loanerId).put(loaneeId, newBalance);
        } else {
            owedMap.put(loanerId, new HashMap<>());
            owedMap.get(loanerId).put(loaneeId, newBalance);
        }

        if (owedMap.containsKey(loaneeId)) {
            owedMap.get(loaneeId).put(loanerId, -newBalance);
        } else {
            owedMap.put(loaneeId, new HashMap<>());
            owedMap.get(loaneeId).put(loanerId, -newBalance);
        }
    }

    public Map<User, Double> getUserBalances(String userId) {
        if (owedMap.containsKey(userId)) {
            Map<User, Double> userBalances = new HashMap<>();
            for (Map.Entry<String, Double> entry : owedMap.get(userId).entrySet()) {
                if (entry.getValue() != 0) {
                    userBalances.put(userService.findUser(entry.getKey()), entry.getValue());
                }
            }
            return userBalances;
        }
        else return new HashMap<>();
    }

    public Map<User, Map<User, Double>> getAllBalances() {
        Map<User, Map<User, Double>> allBalances = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : owedMap.entrySet()) {
            for (Map.Entry<String, Double> entry2 : entry.getValue().entrySet()) {
                if (entry2.getValue() > 0) {
                    User loaner = userService.findUser(entry.getKey());
                    User loanee = userService.findUser(entry2.getKey());
                    if (!allBalances.containsKey(loaner)) {
                        allBalances.put(loaner, new HashMap<>());
                    }
                    if (!allBalances.get(loaner).containsKey(loanee)) {
                        allBalances.get(loaner).put(loanee, entry2.getValue());
                    }
                }
            }
        }
        return allBalances;
    }

    private void validateUserId(String userId) {
        if (userService.findUser(userId) == null) throw new IllegalArgumentException("User id not correct");
    }
}
