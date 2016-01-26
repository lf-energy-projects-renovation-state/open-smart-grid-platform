/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgp.adapter.protocol.dlms.infra.networking;

import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;

import com.alliander.osgp.dto.valueobjects.smartmetering.AlarmType;

public abstract class DlmsChannelHandler extends SimpleChannelHandler {

    private final Logger logger;

    protected DlmsChannelHandler(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel opened", e.getChannel().getId());
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel disconnected", e.getChannel().getId());
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel closed", e.getChannel().getId());
        super.channelClosed(ctx, e);
    }

    @Override
    public void channelUnbound(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel unbound", e.getChannel().getId());
        super.channelUnbound(ctx, e);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        final int channelId = e.getChannel().getId();
        this.logger.warn("{} Unexpected exception from downstream. {}", channelId, e.getCause());
        e.getChannel().close();
    }

    protected void logMessage(final String deviceIdentification, final Set<AlarmType> alarmTypes) {
        this.logger.info("logMessage(" + deviceIdentification + ", " + alarmTypes + ")");
    }
}
