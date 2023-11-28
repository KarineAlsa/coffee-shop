package com.example.cafeteria.models;

import com.example.cafeteria.types.ClientType;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Client {

    private int clientId;
    private Order order;
    private ClientType clientType;
    private Circle circle;

}
