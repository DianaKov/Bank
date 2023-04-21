package org.example;

import javax.persistence.*;
import java.util.List;
import java.util.Scanner;

public class App {
  
    private static final String CURRENCY_CHOICES = "USD/EUR/UAH";
    
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static TypedQuery<User> userQuery;
    
    
    
    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in);
        try {
            // create connection
            emf = Persistence.createEntityManagerFactory("bank2");
            em = emf.createEntityManager();
            userQuery = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            Account moneyTransfer = new Account();
            saveExchangeRates();
    
            try {
                while (true) {
                    System.out.println("Выберите действие:");
                    System.out.println("1: add user");
                    System.out.println("2: add account to user");
                    System.out.println("3: account replenishment");
                    System.out.println("4: funds transfer");
                    System.out.println("5: view All Users\"");
                    System.out.println("6: total amount in UAH");
                    System.out.print("-> ");
                    
                    String choice = sc.nextLine();
                    switch (choice) {
                        case "1": {
                            addUser(sc);
                            break;
                        }
                        case "2": {
                            addAccountToUser(sc);
                            break;
                        }
                        case "3": {
                            depositMoney(sc);
                            break;
                        }
                        case "4": {
                            transferMoney(sc);
                            break;
                        }
                        case "5": {
                            viewAllUsers();
                            break;
                        }
                        case "6": {
                            viewUserAccounts(sc);
                            break;
                        }
                        default:
                            return;
                    }
                }
            } finally {
                sc.close();
                em.close();
                emf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
  
    private static void saveExchangeRates() {
        ExchangeRate usdToUah = new ExchangeRate();
        usdToUah.setFromCurrency(Currency.USD);
        usdToUah.setToCurrency(Currency.UAH);
        usdToUah.setRate(37.5);
        
        ExchangeRate eurToUah = new ExchangeRate();
        eurToUah.setFromCurrency(Currency.EUR);
        eurToUah.setToCurrency(Currency.UAH);
        eurToUah.setRate(40.8);
        
        em.getTransaction().begin(); // Начинаем транзакцию
        
        em.persist(usdToUah); // Сохраняем запись в базе данных
        em.persist(eurToUah);
        
        em.getTransaction().commit(); // Завершаем транзакцию
    }
    
    public static void viewUserAccounts(Scanner sc) {
        try {
            System.out.print("Enter user id: ");
            Long userId = sc.nextLong();
            sc.nextLine();
            
            em.getTransaction().begin();
            
            User user = em.find(User.class, userId);
            
            if (user == null) {
                System.out.println("User not found.");
            } else {
                System.out.println(user.getName() + " accounts:");
                List<Account> accounts = user.getAccounts();
                double totalBalance = 0;
                for (Account account : accounts) {
                    double exchangeRate = 1;
                    if (account.getCurrency() == Currency.USD) {
                        exchangeRate = 37.5;
                    } else if (account.getCurrency() == Currency.EUR) {
                        exchangeRate = 40.8;
                    }
                    double balanceInUAH = account.getBalance() * exchangeRate;
                    System.out.println("\t" + account.getCurrency() + " " + account.getBalance() + " = UAH " + balanceInUAH);
                    totalBalance += balanceInUAH;
                }
                System.out.println("Total balance in UAH: " + totalBalance);
            }
            
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw new RuntimeException(ex);
        }
    }
    
    
    private static void addUser(Scanner sc) {
        try {
            em.getTransaction().begin();
            
            System.out.print("Введите имя нового пользователя: ");
            String name = sc.nextLine();
            
            System.out.print("Введите email нового пользователя: ");
            String email = sc.nextLine();
            
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            
            System.out.printf("Введите валюту для нового счета (%s): ", CURRENCY_CHOICES);
            String currency = sc.nextLine().toUpperCase();
            
            Account account = new Account();
            account.setUser(user);
            account.setCurrency(Currency.valueOf(currency));
            account.setBalance(0.0);
            
            user.getAccounts().add(account);
            
            em.persist(user);
            em.getTransaction().commit();
            
            System.out.println("User added successfully.");
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw new RuntimeException(ex);
        }
    }
    
    private static void viewAllUsers() {
        try {
            em.getTransaction().begin();
            
            List<User> users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
            
            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                for (User user : users) {
                    System.out.println(user.getId() + ": " + user.getName() + " (" + user.getEmail() + ")");
                    List<Account> accounts = user.getAccounts();
                    for (Account account : accounts) {
                        System.out.println("\t" + account.getCurrency() + " " + account.getBalance());
                    }
                }
            }
            
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw new RuntimeException(ex);
        }
    }
    
    
    private static void depositMoney(Scanner sc) {
        System.out.print("Введите email пользователя: ");
        String email = sc.nextLine();
    
        TypedQuery<User> userQuery = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
        userQuery.setParameter("email", email);
    
        try {
            User user = userQuery.getSingleResult();
            List<Account> accounts = user.getAccounts();
        
            System.out.println("Доступные счета для пользователя " + user.getName() + ":");
            for (Account account : accounts) {
                System.out.println(account.getId() + " " + account.getCurrency() + " " + account.getBalance());
            }
        
            System.out.print("Введите номер счета для пополнения: ");
            Long accountId = Long.parseLong(sc.nextLine());
        
            Account accountToReplenish = accounts.stream()
                    .filter(account -> account.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid account id"));
        
            System.out.print("Введите сумму для пополнения: ");
            Double amount = Double.parseDouble(sc.nextLine());
        
            em.getTransaction().begin();
        
            accountToReplenish.setBalance(accountToReplenish.getBalance() + amount);
            em.merge(accountToReplenish);
        
            em.getTransaction().commit();
        
            System.out.println("Счет успешно пополнен.");
        } catch (NoResultException ex) {
            System.out.println("Пользователь с email " + email + " не найден.");
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            em.getTransaction().rollback();
            System.out.println("Error: " + ex.getMessage());
        }
    
    }
    
    private static void addAccountToUser(Scanner sc) {
        try {
            em.getTransaction().begin();
            
            System.out.print("Введите email пользователя: ");
            String email = sc.nextLine();
            
            // Получаем пользователя из базы данных по его email
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            User user = query.getSingleResult();
    
            System.out.printf("Введите валюту для нового счета (%s): ", CURRENCY_CHOICES);
            String currency = sc.nextLine();
            
            // Создаем новый счет и добавляем его к списку счетов пользователя
            Account account = new Account();
            account.setUser(user);
            account.setCurrency(Currency.valueOf(currency.toUpperCase()));
            account.setBalance(0.0);
            user.getAccounts().add(account);
            
            em.persist(account);
            em.getTransaction().commit();
            
            System.out.println("Account added successfully.");
        } catch (Exception ex) {
            em.getTransaction().rollback();
            System.out.println("Error: " + ex.getMessage());
        }
    }
    
    private static void transferMoney(Scanner sc) {
        System.out.println("Введите идентификатор пользователя, с которого будут списаны средства:");
        Long fromUserId = sc.nextLong();
        System.out.println("Введите идентификатор пользователя, на чей счет будут переведены средства:");
        Long toUserId = sc.nextLong();
        
        User fromUser = em.find(User.class, fromUserId);
        User toUser = em.find(User.class, toUserId);
        
        System.out.println("Выберите номер счета, с которого будут списаны средства:");
        List<Account> fromUserAccounts = fromUser.getAccounts();
        
        for (int i = 0; i < fromUserAccounts.size(); i++) {
            System.out.println((i + 1) + ". " + fromUserAccounts.get(i).getCurrency() + " " + fromUserAccounts.get(i).getBalance());
        }
        int fromAccountIndex = sc.nextInt() - 1;
        Account fromAccount = fromUserAccounts.get(fromAccountIndex);
        
        System.out.println("Выберите номер счета, на который будут переведены средства:");
        List<Account> toUserAccounts = toUser.getAccounts();
        for (int i = 0; i < toUserAccounts.size(); i++) {
            System.out.println((i + 1) + ". " + toUserAccounts.get(i).getCurrency() + " " + toUserAccounts.get(i).getBalance());
        }
        int toAccountIndex = sc.nextInt() - 1;
        Account toAccount = toUserAccounts.get(toAccountIndex);
        
        if (fromAccount.getCurrency() != toAccount.getCurrency()) {
            System.out.println("Вы не можете перевести средства между счетами в разных валютах");
            return;
        }
        
        System.out.println("Введите сумму для перевода:");
        Double amount = sc.nextDouble();
        
        if (fromAccount.getBalance() < amount) {
            System.out.println("На счету недостаточно средств для перевода");
            return;
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        em.persist(fromAccount);
        em.persist(toAccount);
        em.merge(fromUser);
        em.merge(toUser);
        
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
    
        try {
            transaction.commit();
            System.out.println("Перевод выполнен успешно");
        } catch (Exception e) {
            transaction.rollback();
            System.out.println("Ошибка при выполнении перевода: " + e.getMessage());
        }
        
    }
    
}