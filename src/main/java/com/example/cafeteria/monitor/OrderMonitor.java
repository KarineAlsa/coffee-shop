package com.example.cafeteria.monitor;

import com.example.cafeteria.models.Client;
import com.example.cafeteria.models.Order;
import com.example.cafeteria.types.ClientType;
import com.example.cafeteria.types.OrderState;
import com.example.cafeteria.types.OrderType;

public class OrderMonitor {

    private boolean isClientOrdering;

    private boolean isClientDriveOrdering;

    public synchronized void setClientOrdering(Client client) {
        while (isClientOrdering) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isClientOrdering = true;

        Order order = new Order();
        switch (client.getClientType()) {
            case DELIVERY -> order.setOrderType(OrderType.DELIVERY);
            case HOUSE -> order.setOrderType(OrderType.IN_HOUSE);
        }
        order.setOrderState(OrderState.REQUEST);
        order.setOrderNumber(client.getClientId());

        client.setOrder(order);
    }

    public synchronized void isClientDriveOrdering(Client client) {
        while (isClientDriveOrdering) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isClientDriveOrdering = true;

        Order order = new Order();
        order.setOrderType(OrderType.DRIVE_THRU);
        order.setOrderState(OrderState.REQUEST);
        order.setOrderNumber(client.getClientId());

        client.setOrder(order);
    }

    public synchronized void isClientDriveOrdered() {
        isClientDriveOrdering = false;
        notifyAll();
    }

    public synchronized void ordered() {
        isClientOrdering = false;
        notifyAll();
    }

}
