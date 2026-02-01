package com.nhnacademy.command;

import com.nhnacademy.domain.Message;
import com.nhnacademy.session.ClientSession;

public interface Command {
    void execute(Message request);
}
