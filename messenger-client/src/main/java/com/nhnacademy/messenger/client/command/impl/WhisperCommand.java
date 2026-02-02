//package com.nhnacademy.messenger.client.command.impl;
//
//import com.nhnacademy.messenger.client.command.ClientCommand;
//import com.nhnacademy.messenger.client.handler.ClientEventHandler;
//
//public class WhisperCommand implements ClientCommand {
//    @Override
//    public void execute(ClientEventHandler handler, String[] args) {
//        if (args.length < 3) {
//            System.out.println("[시스템] 사용법: /w [대상ID] [메시지]");
//            return;
//        }
//
//        String targetId = args[1];
//        StringBuilder content = new StringBuilder();
//        for (int i = 2; i < args.length; i++) {
//            content.append(args[i]).append(" ");
//        }
//
//        handler.sendWhisperMessage(targetId, content.toString().trim());
//    }
//}
