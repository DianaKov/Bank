package org.example;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    private Currency currency;
    private Double balance;
    
    @OneToMany(mappedBy = "account")
    private Set<ExchangeRate> exchangeRates = new HashSet<>();
    
    public Account(User user, Currency currency, Double balance) {
        this.user = user;
        this.currency = currency;
        this.balance = balance;
    }
    
    public Account() {
    }
    

    
    public Double getBalance() {
        return balance;
    }
    
    public void setBalance(Double balance) {
        this.balance = balance;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public Long getId() {
        return id;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public User getUser() {
        return user;
    }
    
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", user=" + user +
                ", currency=" + currency +
                ", balance=" + balance +
                '}';
    }

}


