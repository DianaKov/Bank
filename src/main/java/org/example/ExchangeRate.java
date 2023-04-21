package org.example;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "exchange_rates")
public class ExchangeRate implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Currency fromCurrency;
    private Currency toCurrency;
    private Double rate;
    
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    
    public ExchangeRate() {}
    
    public ExchangeRate(Currency fromCurrency, Currency toCurrency, Double rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
    }
    
    public ExchangeRate(Currency fromCurrency, Currency toCurrency) {
    }
    
    public static Double getExchangeRate(EntityManager em, Currency fromCurrency, Currency toCurrency) {
        TypedQuery<Double> query = em.createQuery(
                "SELECT MAX(er.rate) FROM ExchangeRate er WHERE er.fromCurrency = :fromCurrency AND er.toCurrency = :toCurrency",
                Double.class);
        query.setParameter("fromCurrency", fromCurrency);
        query.setParameter("toCurrency", toCurrency);
        return query.getSingleResult();
    }
    
    
    
    public Double getRate() {
        return rate;
    }
    public Currency getToCurrency() {
        return toCurrency;
    }
    
    public void setFromCurrency(Currency fromCurrency) {
        this.fromCurrency = fromCurrency;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setRate(Double rate) {
        this.rate = rate;
    }
    
    public void setToCurrency(Currency toCurrency) {
        this.toCurrency = toCurrency;
    }
    
    @Override
    public String toString() {
        return "ExchangeRate{" +
                "id=" + id +
                ", fromCurrency='" + fromCurrency + '\'' +
                ", toCurrency='" + toCurrency + '\'' +
                ", rate=" + rate +
                '}';
    }
    
}


