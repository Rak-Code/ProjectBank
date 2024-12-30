package src;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

class BankAccount {
    private int accountNumber;
    private String accountHolderName;
    private double balance;

    public BankAccount(int accountNumber, String accountHolderName, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

public class BankApplication {
    private static final String url = "jdbc:mysql://localhost:3306/BankDB";
    private static final String user = "root";
    private static final String password = "Rak@1411";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Database connected.");

            
            System.out.println("Open Account....");
            
           

            System.out.print("Enter account holder name: ");
            String accountHolderName = scanner.next();

            System.out.print("Enter initial balance: ");
            double initialBalance = scanner.nextDouble();
            
            
            
            Random random = new Random();
            int accountNumber = 100000 + random.nextInt(9999999);
            System.out.println("your generated account number is: "+ accountNumber);
            
            
            
            
            

            // Insert new account into the database
            String insertAccount = "INSERT INTO BankAccount (accountNumber, accountHolderName, balance) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertAccount)) {
            	ps.setInt(1, accountNumber);
            	ps.setString(2, accountHolderName);
            	ps.setDouble(3, initialBalance);
            	int rows = ps.executeUpdate();
            	if(rows > 0) {
            		System.out.println(rows + "rows(s) affected.");
            	}
            	else {
            		System.out.println("No rows affected.");
            	}
            }

            
            
            int choice;
            do {
                System.out.println("\n1. Deposit");
                System.out.println("2. Withdraw");
                System.out.println("3. Check Balance");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();

                switch (choice) {
                    case 1: // Deposit
                        System.out.print("Enter amount to deposit: ");
                        double depositAmount = scanner.nextDouble();

                        String depositSQL = "UPDATE BankAccount SET balance = balance + ? WHERE accountNumber = ?";
                        try (PreparedStatement ps = connection.prepareStatement(depositSQL)) {
                        	ps.setDouble(1, depositAmount);
                        	ps.setInt(2, accountNumber);
                        	int rows = ps.executeUpdate();
                        	if(rows > 0) {
                        		System.out.println(rows + "rows(s) affected.");
                        	}
                        	else {
                        		System.out.println("No rows affected.");
                        	}
                        	

                            // transaction recorf
                            String recordTransactionSQL = "INSERT INTO Transaction (accountNumber, transactionType, amount) VALUES (?, ?, ?)";
                            try (PreparedStatement tps = connection.prepareStatement(recordTransactionSQL)) {
                            	tps.setInt(1, accountNumber);
                            	tps.setString(2, "Deposit");
                            	tps.setDouble(3, depositAmount);
                            	int t_rows = tps.executeUpdate();
                            	if(t_rows > 0) {
                            		System.out.println(t_rows + "rows(s) affected.");
                            	}
                            	else {
                            		System.out.println("No rows affected.");
                            	}
                            }
                        }
                        System.out.println("Amount " + depositAmount + " deposited successfully.");
                        break;

                    case 2: // Withdraw
                        System.out.print("Enter amount to withdraw: ");
                        double withdrawAmount = scanner.nextDouble();
                        String balanceCheckSQL = "SELECT balance FROM BankAccount WHERE accountNumber = ?";
//                       
                        try (PreparedStatement ps = connection.prepareStatement(balanceCheckSQL)) {
                        	ps.setInt(1, accountNumber);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                double currentBalance = rs.getDouble("balance");
                                if (withdrawAmount > currentBalance) {
                                    throw new InsufficientFundsException("Insufficient balance.");
                                }
                                String withdrawSQL = "UPDATE BankAccount SET balance = balance - ? WHERE accountNumber = ?";
                                try (PreparedStatement wps = connection.prepareStatement(withdrawSQL)) {
                                	wps.setDouble(1, withdrawAmount);
                                	wps.setInt(2, accountNumber);
                                	wps.executeUpdate();

                                    // Record transaction
                                    String recordTransactionSQLquery = "INSERT INTO Transaction (accountNumber, transactionType, amount) VALUES (?, ?, ?)";
                                    try (PreparedStatement tps = connection.prepareStatement(recordTransactionSQLquery)) {
                                    	tps.setInt(1, accountNumber);
                                    	tps.setString(2, "Withdraw");
                                        tps.setDouble(3, withdrawAmount);
                                        int rows = tps.executeUpdate();
                                        if(rows > 0) {
                                    		System.out.println(rows + "rows(s) affected.");
                                    	}
                                    	else {
                                    		System.out.println("No rows affected.");
                                    	}
                                        
                                    }
                                }
                                System.out.println("Amount " + withdrawAmount + " withdrawn successfully.");
                            } 
                        }
                        break;

                        
                        
                    case 3: // Check Balance
                        String checkBalanceSQL = "SELECT balance FROM BankAccount WHERE accountNumber = ?";
                        try (PreparedStatement ps = connection.prepareStatement(checkBalanceSQL)) {
                        	ps.setInt(1, accountNumber);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                System.out.println("Account Balance: " + rs.getDouble("balance"));
                            } else {
                                System.out.println("Account not found.");
                            }
                        }
                        break;

                    case 4: // Exit
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }
            } while (choice != 4);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        } 
        scanner.close();
    }
}
