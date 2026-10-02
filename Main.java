
abstract class Account {
    int accountNo;
    String customerName;
    String customerId;
    double balance;
    String accountType;
    String phoneNumber;
    String email;
    String address;
    String panNumber;
    java.time.LocalDate openingDate;
    String branchName;
    String ifscCode;
    String micrCode;
    String status;          // ACTIVE, CLOSED, FROZEN
    double interestRate;
    java.util.List<Transaction> transactions;

    static int transactionSeq = 1000;

    Account(int accountNo, String customerName, double balance) {
        this.accountNo = accountNo;
        this.customerName = customerName;
        this.balance = balance;
        this.openingDate = java.time.LocalDate.now();
        this.status = "ACTIVE";
        this.transactions = new java.util.ArrayList<>();
    }

    void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return;
        }
        balance += amount;
        addTransaction("DEPOSIT", amount);
        System.out.printf("Deposited %.2f to Account #%d. New balance: %.2f%n", amount, accountNo, balance);
    }

    // Each account type enforces its own withdrawal rules
    abstract void withdraw(double amount);

    double checkBalance() {
        return balance;
    }

    void sendMoney(Account toAccount, double amount) {
        if (toAccount == null) {
            System.out.println("Destination account not found.");
            return;
        }
        this.withdraw(amount);
        toAccount.deposit(amount);
        System.out.printf("Transferred %.2f from Account #%d to Account #%d%n", amount, this.accountNo, toAccount.accountNo);
    }

    // Each account type calculates interest differently
    abstract double calculateInterest();

    void displayDetails() {
        System.out.println("Account No   : " + accountNo);
        System.out.println("Customer     : " + customerName);
        System.out.println("Type         : " + accountType);
        System.out.printf ("Balance      : %.2f%n", balance);
        System.out.println("Status       : " + status);
        System.out.println("Opened On    : " + openingDate);
    }

    void addTransaction(String type, double amount) {
        Transaction txn = new Transaction(++transactionSeq, accountNo, type, amount, java.time.LocalDate.now());
        txn.setBalanceAfterTransaction(balance);
        transactions.add(txn);
    }

    void getTransactionHistory() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet for Account #" + accountNo);
            return;
        }
        System.out.println("Transaction history for Account #" + accountNo + ":");
        for (Transaction t : transactions) {
            t.displayTransaction();
        }
    }

    void updateContactDetails(String phone, String email) {
        this.phoneNumber = phone;
        this.email = email;
        System.out.println("Contact details updated for Account #" + accountNo);
    }

    void closeAccount() {
        this.status = "CLOSED";
        System.out.println("Account #" + accountNo + " has been closed.");
    }

    // Getters used by BankBranch / subclasses
    int getAccountNo() { return accountNo; }
    String getCustomerName() { return customerName; }
    double getBalance() { return balance; }
    String getStatus() { return status; }
    String getAccountType() { return accountType; }
    java.util.List<Transaction> getTransactions() { return transactions; }

    void setBalance(double balance) { this.balance = balance; }
}

class SavingAccount extends Account {
    double minimumBalance;
    double withdrawalLimit;
    String debitCardNumber;
    double atmDailyLimit;
    String nomineeName;
    boolean autoSweepEnabled;

    SavingAccount(int accountNo, String customerName, double balance, double minimumBalance) {
        super(accountNo, customerName, balance);
        this.minimumBalance = (minimumBalance > 0) ? minimumBalance : 10000; // default per case study
        this.withdrawalLimit = 50000;
        this.atmDailyLimit = 25000;
        this.autoSweepEnabled = false;
        this.interestRate = 3.5;
        this.accountType = "SAVINGS";
    }

    void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        if (amount > withdrawalLimit) {
            System.out.println("Withdrawal exceeds per-transaction limit of " + withdrawalLimit);
            return;
        }
        if (balance - amount < minimumBalance) {
            System.out.println("Withdrawal denied: balance cannot fall below minimum balance of " + minimumBalance);
            return;
        }
        setBalance(balance - amount);
        addTransaction("WITHDRAW", amount);
        System.out.printf("Withdrew %.2f from Account #%d. New balance: %.2f%n", amount, accountNo, balance);
    }

    double calculateInterest() {
        double interest = balance * (interestRate / 100) / 12; // monthly interest
        return interest;
    }

    void displayDetails() {
        super.displayDetails();
        System.out.printf("Minimum Balance: %.2f%n", minimumBalance);
        System.out.println("Auto-Sweep     : " + (autoSweepEnabled ? "Enabled" : "Disabled"));
    }

    void enableAutoSweep() {
        this.autoSweepEnabled = true;
        System.out.println("Auto-sweep enabled for Account #" + accountNo);
    }

    boolean checkMinimumBalance() {
        return balance >= minimumBalance;
    }
}

// Per the case study, a salary account behaves like a savings account,
// but freezes if no transaction happens for freezePeriodMonths.
class SalaryAccount extends Account {
    java.time.LocalDate lastTransactionDate;
    int freezePeriodMonths;
    boolean isFrozen;
    String employerName;
    String employeeId;
    double monthlySalary;
    int salaryCreditDate;
    String companyAccountNo;
    String salaryStatus;      // PENDING, CREDITED

    SalaryAccount(int accountNo, String customerName, double balance, String employerName) {
        super(accountNo, customerName, balance);
        this.employerName = employerName;
        this.freezePeriodMonths = 2;
        this.isFrozen = false;
        this.lastTransactionDate = java.time.LocalDate.now();
        this.interestRate = 3.5; // same as savings
        this.accountType = "SALARY";
        this.salaryStatus = "PENDING";
    }

    void withdraw(double amount) {
        checkAndFreezeAccount();
        if (isFrozen) {
            System.out.println("Account #" + accountNo + " is frozen. Withdrawal denied.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        if (amount > balance) {
            System.out.println("Insufficient balance for withdrawal.");
            return;
        }
        setBalance(balance - amount);
        lastTransactionDate = java.time.LocalDate.now();
        addTransaction("WITHDRAW", amount);
        System.out.printf("Withdrew %.2f from Account #%d. New balance: %.2f%n", amount, accountNo, balance);
    }

    double calculateInterest() {
        return balance * (interestRate / 100) / 12;
    }

    void checkAndFreezeAccount() {
        long monthsSince = java.time.temporal.ChronoUnit.MONTHS.between(lastTransactionDate, java.time.LocalDate.now());
        if (monthsSince >= freezePeriodMonths) {
            isFrozen = true;
            notifyAccountHolder();
        }
    }

    void notifyAccountHolder() {
        System.out.println("NOTICE: Account #" + accountNo + " (" + customerName +
                ") has been frozen due to " + freezePeriodMonths + " months of inactivity.");
    }

    void displayDetails() {
        super.displayDetails();
        System.out.println("Employer     : " + employerName);
        System.out.println("Frozen       : " + isFrozen);
    }

    void creditSalary(double amount) {
        if (isFrozen) {
            System.out.println("Cannot credit salary: account is frozen.");
            return;
        }
        deposit(amount);
        lastTransactionDate = java.time.LocalDate.now();
        salaryStatus = "CREDITED";
        System.out.println("Salary credited to Account #" + accountNo);
    }

    boolean verifySalaryCredit() {
        return "CREDITED".equals(salaryStatus);
    }
}

class CurrentAccount extends Account {
    double overdraftLimit;
    String businessName;
    String gstNumber;
    double monthlyServiceFee;
    String chequeBookNumber;
    double transactionLimit;

    CurrentAccount(int accountNo, String customerName, double balance, double overdraftLimit) {
        super(accountNo, customerName, balance);
        this.overdraftLimit = overdraftLimit;
        this.monthlyServiceFee = 500;
        this.transactionLimit = 200000;
        this.interestRate = 0; // current accounts typically don't earn interest
        this.accountType = "CURRENT";
    }

    void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        if (amount > transactionLimit) {
            System.out.println("Withdrawal exceeds per-transaction limit of " + transactionLimit);
            return;
        }
        // Allowed to go negative, up to the overdraft limit
        if (balance - amount < -overdraftLimit) {
            System.out.println("Withdrawal denied: exceeds overdraft limit of " + overdraftLimit);
            return;
        }
        setBalance(balance - amount);
        addTransaction("WITHDRAW", amount);
        System.out.printf("Withdrew %.2f from Account #%d. New balance: %.2f%n", amount, accountNo, balance);
    }

    double calculateInterest() {
        // No interest on current accounts; overdraft usage could accrue a charge instead
        return 0.0;
    }

    void displayDetails() {
        super.displayDetails();
        System.out.printf("Overdraft Limit: %.2f%n", overdraftLimit);
        System.out.println("Business Name  : " + businessName);
    }

    void useOverdraft(double amount) {
        withdraw(amount); // withdraw already enforces the overdraft limit
    }

    void chargeServiceFee() {
        setBalance(balance - monthlyServiceFee);
        addTransaction("SERVICE_FEE", monthlyServiceFee);
        System.out.println("Monthly service fee of " + monthlyServiceFee + " charged to Account #" + accountNo);
    }

    void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
}

// Per the case study, a loan account acts like a savings account but starts
// with a negative balance equal to the loan amount; repayments raise the balance toward zero.
class LoanAccount extends Account {
    double loanAmount;
    double amountRepaid;
    String loanType;
    String loanPurpose;
    int tenureMonths;
    double emiAmount;
    java.time.LocalDate startDate;
    java.time.LocalDate dueDate;
    String collateralDetails;
    String guarantorName;
    String loanStatus; // ACTIVE, CLOSED

    LoanAccount(int accountNo, String customerName, double loanAmount) {
        super(accountNo, customerName, -loanAmount); // initial balance is negative
        this.loanAmount = loanAmount;
        this.amountRepaid = 0;
        this.tenureMonths = 12;
        this.interestRate = 9.5;
        this.startDate = java.time.LocalDate.now();
        this.dueDate = startDate.plusMonths(tenureMonths);
        this.loanStatus = "ACTIVE";
        this.accountType = "LOAN";
        this.emiAmount = calculateEMI();
    }

    // Loan accounts don't support ordinary withdrawals; funds are disbursed upfront
    void withdraw(double amount) {
        System.out.println("Withdrawal not supported on a loan account.");
    }

    void repayLoan(double amount) {
        if (amount <= 0) {
            System.out.println("Repayment amount must be positive.");
            return;
        }
        setBalance(balance + amount);
        amountRepaid += amount;
        addTransaction("EMI", amount);
        System.out.printf("Repaid %.2f towards Account #%d. Outstanding: %.2f%n", amount, accountNo, getOutstandingAmount());
        if (balance >= 0) {
            closeLoan();
        }
    }

    double calculateInterest() {
        // simple interest on outstanding loan amount
        return getOutstandingAmount() * (interestRate / 100) / 12;
    }

    void displayDetails() {
        super.displayDetails();
        System.out.printf("Loan Amount    : %.2f%n", loanAmount);
        System.out.printf("Amount Repaid  : %.2f%n", amountRepaid);
        System.out.printf("Outstanding    : %.2f%n", getOutstandingAmount());
        System.out.println("Loan Status    : " + loanStatus);
    }

    double calculateEMI() {
        double monthlyRate = interestRate / 12 / 100;
        if (monthlyRate == 0) return loanAmount / tenureMonths;
        double emi = loanAmount * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths)
                / (Math.pow(1 + monthlyRate, tenureMonths) - 1);
        return emi;
    }

    double getOutstandingAmount() {
        return Math.max(0, -balance);
    }

    void closeLoan() {
        loanStatus = "CLOSED";
        status = "CLOSED";
        System.out.println("Loan Account #" + accountNo + " fully repaid and closed.");
    }
}

class Transaction {
    int transactionId;
    int accountNo;
    String transactionType;   // DEPOSIT, WITHDRAW, TRANSFER, INTEREST, EMI
    double amount;
    java.time.LocalDate transactionDate;
    java.time.LocalTime transactionTime;
    String mode;              // CASH, ONLINE, CHEQUE, ATM
    String channel;           // BRANCH, MOBILE, NETBANKING
    String referenceNo;
    double balanceAfterTransaction;
    String description;
    String status;            // SUCCESS, FAILED, REVERSED

    Transaction(int transactionId, int accountNo, String transactionType, double amount, java.time.LocalDate date) {
        this.transactionId = transactionId;
        this.accountNo = accountNo;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = date;
        this.transactionTime = java.time.LocalTime.now();
        this.mode = "ONLINE";
        this.channel = "BRANCH";
        this.referenceNo = "REF" + transactionId;
        this.status = "SUCCESS";
    }

    void displayTransaction() {
        System.out.printf("[TXN#%d] %s | Acc:%d | Amount:%.2f | %s %s | Status:%s | BalAfter:%.2f%n",
                transactionId, transactionType, accountNo, amount, transactionDate, transactionTime, status, balanceAfterTransaction);
    }

    boolean validateTransaction() {
        return amount > 0 && accountNo > 0;
    }

    void reverseTransaction() {
        this.status = "REVERSED";
        System.out.println("Transaction #" + transactionId + " has been reversed.");
    }

    String generateReceipt() {
        return "----- RECEIPT -----\n" +
                "Txn ID: " + transactionId + "\n" +
                "Account No: " + accountNo + "\n" +
                "Type: " + transactionType + "\n" +
                "Amount: " + amount + "\n" +
                "Date/Time: " + transactionDate + " " + transactionTime + "\n" +
                "Reference: " + referenceNo + "\n" +
                "Status: " + status + "\n" +
                "--------------------";
    }

    // Getters / Setters
    int getTransactionId() { return transactionId; }
    int getAccountNo() { return accountNo; }
    String getTransactionType() { return transactionType; }
    double getAmount() { return amount; }
    java.time.LocalDate getTransactionDate() { return transactionDate; }
    String getStatus() { return status; }
    void setBalanceAfterTransaction(double balanceAfterTransaction) { this.balanceAfterTransaction = balanceAfterTransaction; }
    double getBalanceAfterTransaction() { return balanceAfterTransaction; }
    void setMode(String mode) { this.mode = mode; }
    void setDescription(String description) { this.description = description; }
}

class BankBranch {
    String bankName;
    String bankCode;
    String branchName;
    String branchCode;
    String ifscCode;
    String micrCode;
    String address;
    String phoneNumber;
    String email;
    String managerName;
    java.util.List<Account> accounts;
    int accountCount;
    java.util.List<Transaction> allTransactions; // branch-wide ledger, for end-of-day report
    boolean isOpen;

    BankBranch(String bankName, String branchName) {
        this.bankName = bankName;
        this.branchName = branchName;
        this.accounts = new java.util.ArrayList<>();
        this.allTransactions = new java.util.ArrayList<>();
        this.accountCount = 0;
        this.isOpen = false;
    }

    void addAccount(Account account) {
        accounts.add(account);
        accountCount++;
        System.out.println("Account #" + account.getAccountNo() + " added to " + branchName);
    }

    void removeAccount(int accountNo) {
        Account acc = findAccount(accountNo);
        if (acc != null) {
            accounts.remove(acc);
            accountCount--;
            System.out.println("Account #" + accountNo + " removed from " + branchName);
        } else {
            System.out.println("Account #" + accountNo + " not found.");
        }
    }

    Account findAccount(int accountNo) {
        for (Account acc : accounts) {
            if (acc.getAccountNo() == accountNo) return acc;
        }
        return null;
    }

    void displayAllAccounts() {
        System.out.println("=== Accounts at " + branchName + " ===");
        for (Account acc : accounts) {
            acc.displayDetails();
            System.out.println("-----------------------------");
        }
    }

    double getTotalBalance() {
        double total = 0;
        for (Account acc : accounts) total += acc.getBalance();
        return total;
    }

    void deposit(int accountNo, double amount) {
        Account acc = findAccount(accountNo);
        if (acc == null) { System.out.println("Account not found."); return; }
        acc.deposit(amount);
        recordLatestTransaction(acc);
    }

    void withdraw(int accountNo, double amount) {
        Account acc = findAccount(accountNo);
        if (acc == null) { System.out.println("Account not found."); return; }
        acc.withdraw(amount);
        recordLatestTransaction(acc);
    }

    void sendMoney(int fromAcc, int toAcc, double amount) {
        Account from = findAccount(fromAcc);
        Account to = findAccount(toAcc);
        if (from == null || to == null) {
            System.out.println("One or both accounts not found.");
            return;
        }
        from.sendMoney(to, amount);
        recordLatestTransaction(from);
        recordLatestTransaction(to);
    }

    void addTransaction(Transaction transaction) {
        allTransactions.add(transaction);
    }

    void recordLatestTransaction(Account acc) {
        java.util.List<Transaction> txns = acc.getTransactions();
        if (!txns.isEmpty()) {
            addTransaction(txns.get(txns.size() - 1));
        }
    }

    java.util.List<Transaction> getTransactionsByAccount(int accountNo) {
        java.util.List<Transaction> result = new java.util.ArrayList<>();
        for (Transaction t : allTransactions) {
            if (t.getAccountNo() == accountNo) result.add(t);
        }
        return result;
    }

    void generateEndOfDayReport() {
        System.out.println("========== END OF DAY REPORT: " + branchName + " (" + java.time.LocalDate.now() + ") ==========");
        double totalDeposits = 0, totalWithdrawals = 0;
        int count = 0;
        for (Transaction t : allTransactions) {
            if (!t.getTransactionDate().equals(java.time.LocalDate.now())) continue;
            count++;
            if ("DEPOSIT".equals(t.getTransactionType())) totalDeposits += t.getAmount();
            if ("WITHDRAW".equals(t.getTransactionType())) totalWithdrawals += t.getAmount();
        }
        System.out.println("Transactions today : " + count);
        System.out.printf ("Total deposits      : %.2f%n", totalDeposits);
        System.out.printf ("Total withdrawals   : %.2f%n", totalWithdrawals);
        System.out.printf ("Total branch balance: %.2f%n", getTotalBalance());
        System.out.println("=================================================================");
    }

    void openBranch() {
        isOpen = true;
        System.out.println(branchName + " is now OPEN.");
    }

    void closeBranch() {
        isOpen = false;
        System.out.println(branchName + " is now CLOSED.");
    }
}

public class Main {
    public static void main(String[] args) {
        BankBranch branch = new BankBranch("XYZ Bank", "Pune Main Branch");
        branch.openBranch();

        // 1. Savings Account
        SavingAccount savings = new SavingAccount(1001, "Rahul Sharma", 15000, 10000);
        branch.addAccount(savings);

        // 2. Salary Account
        SalaryAccount salary = new SalaryAccount(1002, "Priya Verma", 5000, "Infosys Ltd");
        branch.addAccount(salary);

        // 3. Current Account
        CurrentAccount current = new CurrentAccount(1003, "Sharma Traders", 20000, 50000);
        branch.addAccount(current);

        // 4. Loan Account
        LoanAccount loan = new LoanAccount(1004, "Amit Patel", 200000);
        branch.addAccount(loan);

        System.out.println("\n--- Over-the-counter activities ---");
        branch.deposit(1001, 5000);
        branch.withdraw(1001, 3000);
        branch.withdraw(1001, 20000); // should be denied: below min balance

        salary.creditSalary(45000);
        branch.withdraw(1002, 10000);

        current.useOverdraft(60000); // within overdraft limit of 50000+balance
        current.chargeServiceFee();

        loan.repayLoan(20000);
        loan.displayDetails();

        System.out.println("\n--- Fund transfer ---");
        branch.sendMoney(1002, 1001, 5000);

        System.out.println("\n--- All account details ---");
        branch.displayAllAccounts();

        System.out.println("\n--- Interest calculation ---");
        System.out.printf("Savings interest this month: %.2f%n", savings.calculateInterest());
        System.out.printf("Salary interest this month : %.2f%n", salary.calculateInterest());
        System.out.printf("Current interest this month: %.2f%n", current.calculateInterest());
        System.out.printf("Loan interest this month    : %.2f%n", loan.calculateInterest());

        System.out.println("\n--- Transaction history (Savings) ---");
        savings.getTransactionHistory();

        branch.generateEndOfDayReport();
        branch.closeBranch();
    }
}