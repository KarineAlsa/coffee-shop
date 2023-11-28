package com.example.cafeteria.hilos;

import com.example.cafeteria.models.Order;
import com.example.cafeteria.publishers.WaiterPublisher;

import java.util.concurrent.Flow;

public class WaiterHouse extends Thread implements Flow.Subscriber<Order> {
    private final WaiterPublisher publisher;
    private Order order = null;
    private Flow.Subscription subscription;

    public WaiterHouse(WaiterPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void run() {
        while (true) {
            if (order != null) {
                publisher.publishOrder(order);
                order = null;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Order item) {
        order = item;
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
