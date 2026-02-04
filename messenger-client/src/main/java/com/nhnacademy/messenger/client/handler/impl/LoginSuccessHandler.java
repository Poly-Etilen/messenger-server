package com.nhnacademy.messenger.client.handler.impl;

import com.nhnacademy.domain.Message;
import com.nhnacademy.messenger.client.handler.Handler;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.ui.form.impl.ClientGUI;
import org.checkerframework.checker.units.qual.C;

public class LoginSuccessHandler implements Handler {
    private final ClientGUI view;
    private final ClientSession session;

    public LoginSuccessHandler(ClientGUI view, ClientSession session) {
        this.view = view;
        this.session = session;
    }


    @Override
    public void handler(Message message) {
        view.setCurrentUser(session.getUserId());

    }
}
