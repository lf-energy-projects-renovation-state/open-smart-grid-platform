/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.domain.smartmetering.application.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

import com.alliander.osgp.domain.core.valueobjects.smartmetering.CosemObisCode;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.CosemObjectDefinition;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.PushSetupSms;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.SendDestinationAndMethod;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.WindowElement;

public class PushSetupSmsConverter extends
        BidirectionalConverter<com.alliander.osgp.dto.valueobjects.smartmetering.PushSetupSms, PushSetupSms> {

    private final ConfigurationMapper mapper;

    public PushSetupSmsConverter() {
        this.mapper = new ConfigurationMapper();
    }

    public PushSetupSmsConverter(final ConfigurationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PushSetupSmsConverter)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        final PushSetupSmsConverter o = (PushSetupSmsConverter) other;
        if (this.mapper == null) {
            return o.mapper == null;
        }
        return this.mapper.getClass().equals(o.mapper.getClass());
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hashCode(this.mapper);
    }

    @Override
    public PushSetupSms convertTo(final com.alliander.osgp.dto.valueobjects.smartmetering.PushSetupSms source,
            final Type<PushSetupSms> destinationType) {
        if (source == null) {
            return null;
        }

        final PushSetupSms.Builder builder = new PushSetupSms.Builder();

        builder.logicalName(this.mapper.map(source.getLogicalName(), CosemObisCode.class));
        if (source.hasPushObjectList()) {
            final List<CosemObjectDefinition> pushObjectList = new ArrayList<>();
            final List<com.alliander.osgp.dto.valueobjects.smartmetering.CosemObjectDefinition> sourcePushObjectList = source
                    .getPushObjectList();
            for (final com.alliander.osgp.dto.valueobjects.smartmetering.CosemObjectDefinition cosemObjectDefinition : sourcePushObjectList) {
                pushObjectList.add(this.mapper.map(cosemObjectDefinition, CosemObjectDefinition.class));
            }
            builder.pushObjectList(pushObjectList);
        }
        builder.sendDestinationAndMethod(this.mapper.map(source.getSendDestinationAndMethod(),
                SendDestinationAndMethod.class));
        if (source.hasCommunicationWindow()) {
            final List<WindowElement> communicationWindow = new ArrayList<>();
            final List<com.alliander.osgp.dto.valueobjects.smartmetering.WindowElement> sourceCommunicationWindow = source
                    .getCommunicationWindow();
            for (final com.alliander.osgp.dto.valueobjects.smartmetering.WindowElement windowElement : sourceCommunicationWindow) {
                communicationWindow.add(this.mapper.map(windowElement, WindowElement.class));
            }
            builder.communicationWindow(communicationWindow);
        }
        builder.randomisationStartInterval(source.getRandomisationStartInterval());
        builder.numberOfRetries(source.getNumberOfRetries());
        builder.repetitionDelay(source.getRepetitionDelay());

        return builder.build();
    }

    @Override
    public com.alliander.osgp.dto.valueobjects.smartmetering.PushSetupSms convertFrom(final PushSetupSms source,
            final Type<com.alliander.osgp.dto.valueobjects.smartmetering.PushSetupSms> destinationType) {
        if (source == null) {
            return null;
        }

        final com.alliander.osgp.dto.valueobjects.smartmetering.PushSetupSms.Builder builder = new com.alliander.osgp.dto.valueobjects.smartmetering.PushSetupSms.Builder();

        builder.logicalName(this.mapper.map(source.getLogicalName(),
                com.alliander.osgp.dto.valueobjects.smartmetering.CosemObisCode.class));
        if (source.hasPushObjectList()) {
            final List<com.alliander.osgp.dto.valueobjects.smartmetering.CosemObjectDefinition> pushObjectList = new ArrayList<>();
            final List<CosemObjectDefinition> sourcePushObjectList = source.getPushObjectList();
            for (final CosemObjectDefinition cosemObjectDefinition : sourcePushObjectList) {
                pushObjectList.add(this.mapper.map(cosemObjectDefinition,
                        com.alliander.osgp.dto.valueobjects.smartmetering.CosemObjectDefinition.class));
            }
            builder.pushObjectList(pushObjectList);
        }
        builder.sendDestinationAndMethod(this.mapper.map(source.getSendDestinationAndMethod(),
                com.alliander.osgp.dto.valueobjects.smartmetering.SendDestinationAndMethod.class));
        if (source.hasCommunicationWindow()) {
            final List<com.alliander.osgp.dto.valueobjects.smartmetering.WindowElement> communicationWindow = new ArrayList<>();
            final List<WindowElement> sourceCommunicationWindow = source.getCommunicationWindow();
            for (final WindowElement windowElement : sourceCommunicationWindow) {
                communicationWindow.add(this.mapper.map(windowElement,
                        com.alliander.osgp.dto.valueobjects.smartmetering.WindowElement.class));
            }
            builder.communicationWindow(communicationWindow);
        }
        builder.randomisationStartInterval(source.getRandomisationStartInterval());
        builder.numberOfRetries(source.getNumberOfRetries());
        builder.repetitionDelay(source.getRepetitionDelay());

        return builder.build();
    }
}
