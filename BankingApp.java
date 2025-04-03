import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import javax.persistence.*;
import java.util.Date;

// Entity: Account
@Entity
@Table(name = "accounts")
class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private double balance;

    public Account() {}

    public Account(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return "Account{" + "accountNumber='" + accountNumber + "', balance=" + balance + '}';
    }
}

// Entity: Transaction
@Entity
@Table(name = "transactions")
class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String fromAccount;

    @Column(nullable = false)
    private String toAccount;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private Date transactionDate;

    public Transaction() {}

    public Transaction(String fromAccount, String toAccount, double amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.transactionDate = new Date();
    }

    @Override
    public String toString() {
        return "Transaction{from='" + fromAccount + "', to='" + toAccount + "', amount=" + amount + '}';
    }
}

// Hibernate Utility Class
class HibernateUtil {
    private static final SessionFactory factory;

    static {
        try {
            factory = new Configuration()
                    .configure()
                    .addAnnotatedClass(Account.class)
                    .addAnnotatedClass(Transaction.class)
                    .buildSessionFactory();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("SessionFactory initialization failed! " + e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return factory;
    }
}

// Service: BankingService
class BankingService {
    public void transferMoney(String fromAcc, String toAcc, double amount) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            Account sender = session.createQuery("FROM Account WHERE accountNumber = :acc", Account.class)
                                    .setParameter("acc", fromAcc)
                                    .uniqueResult();
            Account receiver = session.createQuery("FROM Account WHERE accountNumber = :acc", Account.class)
                                      .setParameter("acc", toAcc)
                                      .uniqueResult();

            if (sender == null || receiver == null) {
                throw new RuntimeException("Account not found!");
            }

            if (sender.getBalance() < amount) {
                throw new RuntimeException("Insufficient Balance!");
            }

            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount);

            session.update(sender);
            session.update(receiver);
            session.save(new Transaction(fromAcc, toAcc, amount));

            tx.commit();
            System.out.println("Transaction Successful: " + amount + " transferred from " + fromAcc + " to " + toAcc);
        } catch (Exception e) {
            tx.rollback();
            System.out.println("Transaction Failed: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}

// Main Application
public class BankingApp {
    public static void main(String[] args) {
        BankingService bankingService = new BankingService();

        // Successful Transaction
        bankingService.transferMoney("ACC123", "ACC456", 200);

        // Failed Transaction (Insufficient Balance)
        bankingService.transferMoney("ACC123", "ACC456", 10000);
    }
}
