package com.ContactManager.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ContactManager.Entities.MyOrder;

public interface MyOrderRepository extends JpaRepository<MyOrder,Long>{

    public MyOrder findByOrderId(String orderId);
}
