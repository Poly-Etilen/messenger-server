package com.nhnacademy.command;

import com.nhnacademy.annotation.CommandMapping;
import com.nhnacademy.command.impl.*;
import com.nhnacademy.domain.Header.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CommandFactory {
    public Map<MessageType, Command> createCommandMap() {
        Map<MessageType, Command> commandMap = new HashMap<>();
        Reflections reflections = new Reflections("com.nhnacademy.command.impl");
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(CommandMapping.class);

        for (Class<?> clazz : commandClasses) {
            try {
                if (Command.class.isAssignableFrom(clazz)) {
                    CommandMapping mapping = clazz.getAnnotation(CommandMapping.class);
                    MessageType messageType = mapping.value();

                    Command commandInstance = (Command) clazz.getDeclaredConstructor().newInstance();
                    commandMap.put(messageType, commandInstance);

                    log.info("Command Auto-Registered: [{}] -> {}", messageType, clazz.getSimpleName());
                }
            } catch (Exception e) {
                log.error("Command 등록 중 오류 발생: {}", clazz.getName(), e);
            }
        }

        return commandMap;
    }
}
