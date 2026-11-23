package com.fincore.fincore_api.modules.account.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fincore.fincore_api.modules.account.enums.AccountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {
    
    @Id 
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Column(length = 120)
    private String institution;

    @Column(name="initial_balance", nullable = false, precision = 15, scale =2)
    private BigDecimal initialBalance;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 7)
    private String color;

    @Column(length = 50)
    private String icon;

    @Column(name="is_active", nullable = false)
    private Boolean active;

    @Column(name="created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Account() {
    }

    @PrePersist 
    protected void onCreate(){
        OffsetDateTime now = OffsetDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (initialBalance == null){
            initialBalance = BigDecimal.ZERO;
        }

        if (currentBalance == null){
            currentBalance = initialBalance;
        }

        if (currency == null || currency.isBlank()){
            currency = "BRL";
        }

        active = true;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate 
    protected void onUpdate(){
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId(){
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId){
        this.userId = userId;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getAccountType(){
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getInstitution(){
        return institution;
    }

    public void setInstitution(String institution){
        this.institution = institution;
    }

    public BigDecimal getInitialBalance(){
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance){
        this.initialBalance = initialBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance( BigDecimal currentBalance){
        this.currentBalance = currentBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

}
