package com.deser.seniorbackup.factory;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.util.impl.BarUtil;
import com.deser.seniorbackup.util.impl.ChatUtil;
import com.deser.seniorbackup.util.impl.TitleUtil;
import lombok.Getter;

@Getter
public class MessageFactory {
    private final SeniorBackup main;
    private final ChatUtil chat;
    private final TitleUtil title;
    private final BarUtil bar;

    public MessageFactory(final SeniorBackup main) {
        this.main = main;
        this.chat = new ChatUtil(main);
        this.title = new TitleUtil(main);
        this.bar = new BarUtil(main);
    }
}