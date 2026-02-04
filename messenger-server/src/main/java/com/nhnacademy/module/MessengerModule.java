package com.nhnacademy.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.nhnacademy.annotation.CommandMapping;
import com.nhnacademy.command.Command;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.MessageQueueManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.repository.UserRepository;
import org.reflections.Reflections;

import java.util.Set;

public class MessengerModule extends AbstractModule {
    // MessengerModule은 Google Guice의 설정을 담당함
    @Override
    protected void configure() {
        // 서버 실행 시 의존성 주입
        bind(ChatRoomManager.class).toInstance(ChatRoomManager.getInstance());
        bind(SessionManager.class).toInstance(SessionManager.getInstance());
        bind(UserRepository.class).asEagerSingleton();
        bind(MessageQueueManager.class).toInstance(MessageQueueManager.getInstance());

        MapBinder<MessageType, Command> mapBinder = MapBinder.newMapBinder(binder(), MessageType.class, Command.class);

        // Reflections를 통해 command 들을 스캔함
        Reflections reflections = new Reflections("com.nhnacademy.command.impl");
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(CommandMapping.class);

        for (Class<?> clazz : commandClasses) {
            CommandMapping commandMapping = clazz.getAnnotation(CommandMapping.class);
            // 어노테이션이 붙은 command 클래스를 찾아 MessageType을 카로 하는 Map 형태로 자동 바인딩 함
            mapBinder.addBinding(commandMapping.value()).to((Class<? extends Command>) clazz);
        }
    }
}
