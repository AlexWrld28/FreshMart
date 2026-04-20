package com.grocery.ui.util;

public class SessionManager {

    private static long userId;
    private static String fullName;
    private static String email;
    private static String role;
    private static double balance;

    public static void setUser(long id, String name, String email, String role, double balance) {
        userId = id;
        fullName = name;
        SessionManager.email = email;
        SessionManager.role = role;
        SessionManager.balance = balance;
    }

    public static void clear() {
        userId = 0;
        fullName = null;
        email = null;
        role = null;
        balance = 0;
    }

    public static long getUserId() { return userId; }
    public static String getFullName() { return fullName; }
    public static String getEmail() { return email; }
    public static String getRole() { return role; }
    public static double getBalance() { return balance; }
    public static void setBalance(double b) { balance = b; }
    public static boolean isAdmin() { return "ADMIN".equals(role); }
}
