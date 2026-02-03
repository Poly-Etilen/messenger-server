package com.nhnacademy.messenger.client;


import com.nhnacademy.messenger.client.command.CommandFactory;
import com.nhnacademy.ui.form.impl.ClientGUI;
import javafx.application.Application;

public class MessengerClient {
    public static void main(String[] args) {


        Application.launch(ClientGUI.class, args);
    }

}