package Banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;



class BankAccounts{
	
	private String username;
    private String passsword;
    private String accountNumber;
    private String accountHolder;
    private double balance;
    private double loanAmount;

    public BankAccounts(String accountNumber, String accountHolder,double balance,double loanAmount,String username,String password) {
    	this.username = username;
        this.passsword = password;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance =balance;
        this.loanAmount = loanAmount;
    }
    public BankAccounts(String accountNumber, String accountHolder,double accountBalance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
      
        this.balance = accountBalance;
        this.loanAmount = 0.0;
    }
    

    public String getUsername() {
		return username;
	}
	public String getPasssword() {
		return passsword;
	}

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public double getBalance() {
        return balance;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited " + amount);
        } else {
            System.out.println("Invalid deposit amount");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Withdrawn " + amount);
        } else {
            System.out.println("Insufficient funds or invalid withdrawal amount");
        }
    }

    public void checkAccountDetails() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Holder: " + accountHolder);
        System.out.println("Balance: " + balance);
        System.out.println("Loan Amount : " + loanAmount);
    }

    public void checkLoanDetails() {
        System.out.println("Loan Amount: " + loanAmount);
    }

    public void checkBalance() {
        System.out.println("Current Balance: " + balance);
    }
    public void applyForLoan(double loanAmount) {
        if (loanAmount > 0 ) {
            
            this.loanAmount += loanAmount;
            System.out.println("Loan of " + loanAmount + " approved.");
        } else {
            System.out.println("Loan application denied. Insufficient balance or invalid loan amount.");
        }
    }
    public void transfer(BankAccount destinationAccount, double amount) {
        if (amount > 0 && amount <= balance) {
            withdraw(amount);
            destinationAccount.deposit(amount);
            System.out.println("Transferred " + amount + " to " + destinationAccount.getAccountHolder());
        } else {
            System.out.println("Invalid transfer amount or insufficient balance.");
        }
    }
	
}

public class ApnaBank {
   
    public static void main(String[] args) {
        try {

			String s = "com.mysql.cj.jdbc.Driver";
			String url = "jdbc:mysql://localhost:3306/bank";
			String urName ="root";
			String urPass ="admin";
			
			//Class.forName(s);
			Connection connection = DriverManager.getConnection(url,urName,urPass);
			 //System.out.println("Welcome to APNA BANK\n" );

            createTableIfNotExists(connection);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String usernameInput = scanner.nextLine();
            System.out.print("Enter your password: ");
            String passwordInput = scanner.nextLine();

            
            if (verifyUser(connection, usernameInput, passwordInput)) {
                
                System.out.println("Login successful!\n");
            } else {
                
                System.out.println("Invalid username or password. Exiting...");
                scanner.close();
                connection.close();
                System.exit(1); 
            }
            System.out.println("Welcome to APNA BANK\n" );
            System.out.print("Enter your account number: ");
            String accountNumber = scanner.nextLine();

            BankAccount account = loadAccountFromDatabase(connection, accountNumber);

            if (account == null) {
                System.out.print("Enter your account holder name: ");
                String accountHolder = scanner.nextLine();
                System.out.print("Enter opening amount: ");
                double accountAmount = scanner.nextDouble();
                account = new BankAccount(accountNumber, accountHolder,accountAmount);
                insertAccountIntoDatabase(connection, account);
            }

            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Deposit money");
                System.out.println("2. Withdraw money");
                System.out.println("3. Check account details");
                System.out.println("4. Check loan details");
                System.out.println("5. Check balance");
                System.out.println("6. Apply for a loan");
                System.out.println("7. Transfer Funds");

                System.out.println("8. Exit");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        System.out.print("Enter the amount to deposit: ");
                        double depositAmount = scanner.nextDouble();
                        account.deposit(depositAmount);
                        updateAccountInDatabase(connection, account);
                        break;
                    case 2:
                        System.out.print("Enter the amount to withdraw: ");
                        double withdrawAmount = scanner.nextDouble();
                        account.withdraw(withdrawAmount);
                        updateAccountInDatabase(connection, account);
                        break;
                    case 3:
                        account.checkAccountDetails();
                        break;
                    case 4:
                        account.checkLoanDetails();
                        break;
                    case 5:
                        account.checkBalance();
                        break;
                    case 6:
                        System.out.print("Enter the loan amount you want to apply for: ");
                        double loanAmount = scanner.nextDouble();
                        account.applyForLoan(loanAmount);
                        updateAccountInDatabase(connection, account);
                        break;
                    case 8:
                        System.out.println("Thank you for using our bank app. Goodbye!");
                        scanner.close();
                        connection.close();
                        System.exit(0);
                        break;
                    case 7:
                        System.out.print("Enter the recipient's account number: ");
                        String recipientAccountNumber = scanner.next();
                        BankAccount recipientAccount = loadAccountFromDatabase(connection, recipientAccountNumber);
                        if (recipientAccount != null) {
                            System.out.print("Enter the amount to transfer: ");
                            double transferAmount = scanner.nextDouble();
                            account.transfer(recipientAccount, transferAmount);
                            updateAccountInDatabase(connection, account);
                            updateAccountInDatabase(connection, recipientAccount);
                        } else {
                            System.out.println("Recipient account not found.");
                        }
                        break;

                    default:
                        System.out.println("Invalid choice. Please choose a valid option.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



	private static void createTableIfNotExists(Connection connection) throws SQLException {
    	String createTableSQL = "CREATE TABLE IF NOT EXISTS bank_accounts (" +
    		    "account_number varchar(255) PRIMARY KEY," +
    		    "account_holder varchar(255)," +
    		    "balance double," +
    		    "loan_amount double" +
    		    ")";
    	
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
            preparedStatement.executeUpdate();
        }
    }

	private static boolean verifyUser(Connection connection, String username, String password) throws SQLException {
	    String selectSQL = "SELECT * FROM users WHERE name = ? AND passward = ?";
	    try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
	        preparedStatement.setString(1, username);
	        preparedStatement.setString(2, password);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        return resultSet.next(); // If a row is found, the credentials are valid
	    }
	}
    private static BankAccount loadAccountFromDatabase(Connection connection, String accountNumber) throws SQLException {
        String selectSQL = "SELECT * FROM bank_accounts WHERE account_number = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
            preparedStatement.setString(1, accountNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
            	 String accountNumber1 = resultSet.getString("account_number");
                String accountHolder = resultSet.getString("account_holder");
                double balance = resultSet.getDouble("balance");
                double loanAmount = resultSet.getDouble("loan_amount");
                return new BankAccount(accountNumber1, accountHolder, balance, loanAmount);
            }
        }
        return null;
    }

    private static void insertAccountIntoDatabase(Connection connection, BankAccount account) throws SQLException {
        String insertSQL = "INSERT INTO bank_accounts (account_number, account_holder, balance, loan_amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setString(2, account.getAccountHolder());
            preparedStatement.setDouble(3, account.getBalance());
            preparedStatement.setDouble(4, account.getLoanAmount());
            preparedStatement.executeUpdate();
        }
    }

    private static void updateAccountInDatabase(Connection connection, BankAccount account) throws SQLException {
        String updateSQL = "UPDATE bank_accounts SET balance = ?, loan_amount = ? WHERE account_number = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setDouble(1, account.getBalance());
            preparedStatement.setDouble(2, account.getLoanAmount());
            preparedStatement.setString(3, account.getAccountNumber());
            preparedStatement.executeUpdate();
        }
    }
}
