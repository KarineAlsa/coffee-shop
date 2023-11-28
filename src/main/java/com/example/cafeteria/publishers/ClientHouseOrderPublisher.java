package com.example.cafeteria.publishers;

import com.example.cafeteria.models.Order;

import java.util.concurrent.SubmissionPublisher;

public class ClientHouseOrderPublisher extends SubmissionPublisher<Order> {
    public void publishOrder(Order order) {
        submit(order);
    }
}
