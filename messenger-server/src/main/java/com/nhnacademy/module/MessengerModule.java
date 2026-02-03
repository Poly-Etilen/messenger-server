package com.nhnacademy.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.nhnacademy.annotation.CommandMapping;
import com.nhnacademy.command.Command;
import com.nhnacademy.domain.Header.MessageType;
import org.reflections.Reflections;

import java.util.Set;

public class MessengerModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<MessageType, Command> mapBinder = MapBinder.newMapBinder(binder(), MessageType.class, Command.class);

        Reflections reflections = new Reflections("com.nhnacademy.command.impl");
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(CommandMapping.class);

        for (Class<?> clazz : commandClasses) {
            CommandMapping commandMapping = clazz.getAnnotation(CommandMapping.class);
            mapBinder.addBinding(commandMapping.value()).to((Class<? extends Command>) clazz);
        }
    }
}
