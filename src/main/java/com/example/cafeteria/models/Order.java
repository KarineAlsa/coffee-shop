package com.example.cafeteria.models;

import com.example.cafeteria.types.OrderState;
import com.example.cafeteria.types.OrderType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Order {

    private OrderType orderType;
    private OrderState orderState;
    private long orderNumber;

}
