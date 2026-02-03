package com.nhnacademy.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.nhnacademy.annotation.CommandMapping;
import com.nhnacademy.command.Command;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import org.reflections.Reflections;

import java.util.Set;

public class MessengerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ChatRoomManager.class).toInstance(ChatRoomManager.getInstance());
        bind(SessionManager.class).toInstance(SessionManager.getInstance());

        MapBinder<MessageType, Command> mapBinder = MapBinder.newMapBinder(binder(), MessageType.class, Command.class);

        Reflections reflections = new Reflections("com.nhnacademy.command.impl");
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(CommandMapping.class);

        for (Class<?> clazz : commandClasses) {
            CommandMapping commandMapping = clazz.getAnnotation(CommandMapping.class);
            mapBinder.addBinding(commandMapping.value()).to((Class<? extends Command>) clazz);
        }
    }
}
