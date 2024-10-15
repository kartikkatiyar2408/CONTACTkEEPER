package com.ContactManager.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="orders")
public class MyOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long MyId;
    private String orderId;
    private String amount;
    private String reciept;
    private String status;
    @ManyToOne
    private User user;
    private String paymentId;
    public Long getMyId() {
        return MyId;
    }
    public void setMyId(Long myId) {
        MyId = myId;
    }
    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getReciept() {
        return reciept;
    }
    public void setReciept(String reciept) {
        this.reciept = reciept;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    public MyOrder(Long myId, String orderId, String amount, String reciept, String status, User user,
            String paymentId) {
        MyId = myId;
        this.orderId = orderId;
        this.amount = amount;
        this.reciept = reciept;
        this.status = status;
        this.user = user;
        this.paymentId = paymentId;
    }
    public MyOrder() {
    }

    
}
