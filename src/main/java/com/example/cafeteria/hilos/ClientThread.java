package com.example.cafeteria.hilos;

import com.example.cafeteria.models.Client;
import com.example.cafeteria.publishers.ClientDriveThruOrderPublisher;
import com.example.cafeteria.monitor.OrderMonitor;
import com.example.cafeteria.publishers.ClientHouseOrderPublisher;
import com.example.cafeteria.types.ClientType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientThread extends Thread {

    private Client client;
    private final OrderMonitor orderMonitor;
    private final ClientDriveThruOrderPublisher clientPublisher;
    private final ClientHouseOrderPublisher clientHouseOrderPublisher;

    public ClientThread(Client client,
                        OrderMonitor orderMonitor ,
                        ClientDriveThruOrderPublisher clientPublisher,
                        ClientHouseOrderPublisher clientHouseOrderPublisher) {
        this.client = client;
        this.clientPublisher = clientPublisher;
        this.orderMonitor = orderMonitor;
        this.clientHouseOrderPublisher = clientHouseOrderPublisher;
    }

    @Override
    public void run() {
        if (client.getClientType() == ClientType.DRIVE_THRU) {
            orderMonitor.isClientDriveOrdering(client);
            clientPublisher.publishOrder(client.getOrder());

        } else {
            orderMonitor.setClientOrdering(client);
            clientHouseOrderPublisher.publishOrder(client.getOrder());
        }

    }

}
