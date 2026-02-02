package com.nhnacademy.command;

import com.nhnacademy.domain.Message;

public interface Command {
    void execute(Message request);
}
