package com.example.cafeteria.controllers;

import com.example.cafeteria.HelloApplication;
import com.example.cafeteria.hilos.ClientThread;
import com.example.cafeteria.hilos.Waiter;
import com.example.cafeteria.hilos.WaiterHouse;
import com.example.cafeteria.models.Client;
import com.example.cafeteria.publishers.ClientDriveThruOrderPublisher;
import com.example.cafeteria.models.Order;
import com.example.cafeteria.publishers.WaiterPublisher;
import com.example.cafeteria.monitor.OrderMonitor;
import com.example.cafeteria.publishers.ClientHouseOrderPublisher;
import com.example.cafeteria.types.ClientType;
import com.example.cafeteria.types.OrderState;
import com.example.cafeteria.utilities.ListUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

public class CafeteriaController implements Initializable, Flow.Subscriber<Order> {

    private static final List<Order> orders = new ArrayList<>();
    private static final AtomicReference<List<Client>> clients = new AtomicReference<>(new ArrayList<>());
    private final OrderMonitor orderMonitor = new OrderMonitor();
    private final WaiterPublisher waiterPublisher = new WaiterPublisher();
    private final ClientDriveThruOrderPublisher clientPublisher = new ClientDriveThruOrderPublisher();
    private final ClientHouseOrderPublisher clientHouseOrderPublisher = new ClientHouseOrderPublisher();
    private int clientCounter = 0;

    private Flow.Subscription subscription;

    @FXML
    private Pane principal;

    @FXML
    private Circle waiterDelivery;

    @FXML
    private Circle waiterDeliveryDrive;

    @FXML
    private Circle deliveryZone;

    @FXML
    private Circle kitchenZone;

    @FXML
    private Circle houseZone;

    @FXML
    private Circle deliverHouseZone;

    @FXML
    private Circle driveZone;

    @FXML
    private Circle driveClientZone;

    @FXML
    private Circle exitDriveClientZone;

    @FXML
    private Circle deliveryClientZone;

    @FXML
    private Circle houseClientZone;

    @FXML
    private Circle deliverHouseClientZone;

    @FXML
    private Circle unknownZone;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        waiterPublisher.subscribe(this);

        Image imageWaiter = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("assets/waiter.png")));
        ImagePattern waiterPattern = new ImagePattern(imageWaiter);
        waiterDelivery.setFill(waiterPattern);

        imageWaiter = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("assets/waiter2.png")));
        waiterPattern = new ImagePattern(imageWaiter);
        waiterDeliveryDrive.setFill(waiterPattern);

        WaiterHouse waiter = new WaiterHouse(waiterPublisher);
        waiter.start();

        Waiter waiterDrive = new Waiter(waiterPublisher);
        waiterDrive.start();

        clientHouseOrderPublisher.subscribe(waiter);
        clientPublisher.subscribe(waiterDrive);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::start);
    }

    public void start() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ClientType[] values = ClientType.values();
            Random random = new Random();
            ClientType clientType = values[random.nextInt(values.length)];
            Circle clientCircle = new Circle(50);

            switch (clientType) {
                case DELIVERY -> {
                    Image image = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("assets/deliver.png")));
                    ImagePattern pattern = new ImagePattern(image);
                    clientCircle.setFill(pattern);
                    clientCircle.setLayoutX(276);
                    clientCircle.setLayoutY(574);
                    clientCircle.setRadius(40);
                }
                case HOUSE -> {
                    Image img = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("assets/client.png")));
                    ImagePattern pattern = new ImagePattern(img);
                    clientCircle.setFill(pattern);
                    clientCircle.setLayoutX(276);
                    clientCircle.setLayoutY(574);
                }
                case DRIVE_THRU -> {
                    Image image = new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream("assets/car.png")));
                    ImagePattern pattern = new ImagePattern(image);
                    clientCircle.setFill(pattern);
                    clientCircle.setLayoutX(786);
                    clientCircle.setLayoutY(176);
                }
            }

            Client client = new Client();
            client.setCircle(clientCircle);
            client.setClientType(clientType);
            client.setClientId(clientCounter);
            clientCounter++;
            clients.get().add(client);

            Platform.runLater(() -> principal.getChildren().add(clientCircle));

            ClientThread clientThread = new ClientThread(client, orderMonitor, clientPublisher, clientHouseOrderPublisher);
            clientThread.start();
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Order item) {
        handleOrder(item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        handleException(throwable);
    }

    @Override
    public void onComplete() {
        handleCompletion();
    }

    private void handleOrder(Order item) {
        System.out.println("Order: " + item + " " + item.getOrderType() + " " + item.getOrderState());
        Platform.runLater(() -> {
            switch (item.getOrderType()) {
                case DELIVERY -> handleDeliveryOrder(item);
                case IN_HOUSE -> handleHouseOrder(item);
                case DRIVE_THRU -> handleDriveThruOrder(item);
            }
        });
    }

    private void handleException(Throwable throwable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void handleCompletion() {
        System.out.println("Completed");
    }

    private void handleDeliveryOrder(Order order) {
        Order upstarted = orders.stream()
                .filter(o -> o.getOrderNumber() == order.getOrderNumber())
                .findFirst()
                .orElse(order);

        switch (upstarted.getOrderState()) {
            case REQUEST -> {
                upstarted.setOrderState(OrderState.IN_PROGRESS);

                Client client = getClient(order);

                System.out.println("Client: " + client);

                Platform.runLater(() -> {
                    assert client != null;
                    client.getCircle().layoutXProperty().bind(deliveryClientZone.layoutXProperty());
                    client.getCircle().layoutYProperty().bind(deliveryClientZone.layoutYProperty());
                });

                Platform.runLater(() -> {
                    waiterDelivery.layoutXProperty().bind(deliveryZone.layoutXProperty());
                    waiterDelivery.layoutYProperty().bind(deliveryZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientHouseOrderPublisher.publishOrder(upstarted);
            }
            case IN_PROGRESS -> {
                upstarted.setOrderState(OrderState.DELIVERING);

                Platform.runLater(() -> {
                    waiterDelivery.layoutXProperty().bind(kitchenZone.layoutXProperty());
                    waiterDelivery.layoutYProperty().bind(kitchenZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientHouseOrderPublisher.publishOrder(upstarted);
            }
            case DELIVERING -> {
                Platform.runLater(() -> {
                    waiterDelivery.layoutXProperty().bind(deliveryZone.layoutXProperty());
                    waiterDelivery.layoutYProperty().bind(deliveryZone.layoutYProperty());
                });

                Client client = getClient(order);

                assert client != null;
                Platform.runLater(() -> principal.getChildren().remove(client.getCircle()));
                clients.get().remove(client);
                orderMonitor.ordered();
            }
        }
    }

    private void handleHouseOrder(Order order) {
        Order upstarted = orders.stream()
                .filter(o -> o.getOrderNumber() == order.getOrderNumber())
                .findFirst()
                .orElse(order);

        switch (upstarted.getOrderState()) {
            case REQUEST -> {
                upstarted.setOrderState(OrderState.IN_PROGRESS);

                Client client = getClient(order);

                System.out.println("Client: " + client);

                Platform.runLater(() -> {
                    assert client != null;
                    client.getCircle().layoutXProperty().bind(houseClientZone.layoutXProperty());
                    client.getCircle().layoutYProperty().bind(houseClientZone.layoutYProperty());
                });

                Platform.runLater(() -> {
                    waiterDelivery.layoutXProperty().bind(houseZone.layoutXProperty());
                    waiterDelivery.layoutYProperty().bind(houseZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientHouseOrderPublisher.publishOrder(upstarted);
            }
            case IN_PROGRESS -> {
                upstarted.setOrderState(OrderState.DELIVERING);

                Platform.runLater(() -> {
                    waiterDelivery.layoutXProperty().bind(kitchenZone.layoutXProperty());
                    waiterDelivery.layoutYProperty().bind(kitchenZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientHouseOrderPublisher.publishOrder(upstarted);
            }
            case DELIVERING -> {
                upstarted.setOrderState(OrderState.DELIVERED);
                Platform.runLater(() -> {
                    waiterDelivery.layoutXProperty().bind(deliverHouseZone.layoutXProperty());
                    waiterDelivery.layoutYProperty().bind(deliverHouseZone.layoutYProperty());
                });

                Client client = getClient(order);

                Platform.runLater(() -> {
                    assert client != null;
                    client.getCircle().layoutXProperty().bind(deliverHouseClientZone.layoutXProperty());
                    client.getCircle().layoutYProperty().bind(deliverHouseClientZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientHouseOrderPublisher.publishOrder(upstarted);
            }
            case DELIVERED -> {
                Client client = getClient(order);

                assert client != null;
                Platform.runLater(() -> principal.getChildren().remove(client.getCircle()));
                clients.get().remove(client);
                orderMonitor.ordered();
            }
        }
    }

    private void handleDriveThruOrder(Order order) {
        Order upstarted = orders.stream()
                .filter(o -> o.getOrderNumber() == order.getOrderNumber())
                .findFirst()
                .orElse(order);

        switch (upstarted.getOrderState()) {
            case REQUEST -> {
                upstarted.setOrderState(OrderState.IN_PROGRESS);

                Client client = getClient(order);

                System.out.println("Client: " + client);

                Platform.runLater(() -> {
                    assert client != null;
                    client.getCircle().layoutXProperty().bind(driveClientZone.layoutXProperty());
                    client.getCircle().layoutYProperty().bind(driveClientZone.layoutYProperty());
                });

                Platform.runLater(() -> {
                    waiterDeliveryDrive.layoutXProperty().bind(driveZone.layoutXProperty());
                    waiterDeliveryDrive.layoutYProperty().bind(driveZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientPublisher.publishOrder(upstarted);
            }
            case IN_PROGRESS -> {
                upstarted.setOrderState(OrderState.DELIVERING);

                Platform.runLater(() -> {
                    waiterDeliveryDrive.layoutXProperty().bind(kitchenZone.layoutXProperty());
                    waiterDeliveryDrive.layoutYProperty().bind(kitchenZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientPublisher.publishOrder(upstarted);
            }
            case DELIVERING -> {
                upstarted.setOrderState(OrderState.DELIVERED);
                Platform.runLater(() -> {
                    waiterDeliveryDrive.layoutXProperty().bind(driveZone.layoutXProperty());
                    waiterDeliveryDrive.layoutYProperty().bind(driveZone.layoutYProperty());
                });

                Client client = getClient(order);

                Platform.runLater(() -> {
                    assert client != null;
                    client.getCircle().layoutXProperty().bind(exitDriveClientZone.layoutXProperty());
                    client.getCircle().layoutYProperty().bind(exitDriveClientZone.layoutYProperty());
                });

                ListUtil.upsert(orders, upstarted);
                clientPublisher.publishOrder(upstarted);
            }
            case DELIVERED -> {
                Client client = getClient(order);

                Platform.runLater(() -> {
                    assert client != null;
                    client.getCircle().layoutXProperty().bind(unknownZone.layoutXProperty());
                    client.getCircle().layoutYProperty().bind(unknownZone.layoutYProperty());
                });

                Platform.runLater(() -> {
                    assert client != null;
                    principal.getChildren().remove(client.getCircle());
                });
                orderMonitor.isClientDriveOrdered();
                clients.get().remove(client);
            }
        }
    }

    private Client getClient(Order order) {
        return clients.get().stream()
                .filter(c -> c.getClientId() == order.getOrderNumber())
                .findFirst().orElseThrow(RuntimeException::new);
    }

}