package org.example;

import javax.persistence.*;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Currency currency;
    private Double amount;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "exchange_rate_id", nullable = false)
    private ExchangeRate exchangeRate;
    
    public Transaction(Long userId, Currency currency, Double amount, ExchangeRate exchangeRate) {
        this.amount = amount;
        this.userId = userId;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
    }
    
    public Transaction(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    
    public Transaction() {
    
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                '}';
    }

}


