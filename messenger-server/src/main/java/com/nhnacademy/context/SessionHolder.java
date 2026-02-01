package com.nhnacademy.context;

import com.nhnacademy.session.ClientSession;

public class SessionHolder {
    private static final ThreadLocal<ClientSession> sessionHolder = new ThreadLocal<>();

    public static void set(ClientSession session) {
        sessionHolder.set(session);
    }

    public static ClientSession get() {
        return sessionHolder.get();
    }

    public static void clear() {
        sessionHolder.remove();
    }
}
