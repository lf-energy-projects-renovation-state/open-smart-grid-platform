/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.ws.smartmetering.infra.jms.messageprocessor;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alliander.osgp.adapter.ws.schema.smartmetering.notification.NotificationType;
import com.alliander.osgp.adapter.ws.smartmetering.application.services.MeterResponseDataService;
import com.alliander.osgp.adapter.ws.smartmetering.application.services.NotificationService;
import com.alliander.osgp.adapter.ws.smartmetering.domain.entities.MeterResponseData;
import com.alliander.osgp.domain.core.valueobjects.DeviceFunction;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.AdministrativeStatusType;
import com.alliander.osgp.shared.exceptionhandling.OsgpException;
import com.alliander.osgp.shared.infra.jms.Constants;
import com.alliander.osgp.shared.infra.jms.ResponseMessageResultType;

@Component("domainSmartMeteringGetAdministrativeStatusResponseMessageProcessor")
public class GetAdministrativeStatusResponseMessageProcessor extends DomainResponseMessageProcessor {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAdministrativeStatusResponseMessageProcessor.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MeterResponseDataService meterResponseDataService;

    protected GetAdministrativeStatusResponseMessageProcessor() {
        super(DeviceFunction.GET_ADMINISTRATIVE_STATUS);
    }

    @Override
    public void processMessage(final ObjectMessage objectMessage) throws JMSException {
        LOGGER.debug("Processing smart metering get administrative status response message");

        String correlationUid = null;
        String messageType = null;
        String organisationIdentification = null;
        String deviceIdentification = null;

        final OsgpException osgpException = null;

        String result = null;
        ResponseMessageResultType resultType = null;
        String message = null;
        NotificationType notificationType = null;

        try {
            correlationUid = objectMessage.getJMSCorrelationID();
            messageType = objectMessage.getJMSType();
            organisationIdentification = objectMessage.getStringProperty(Constants.ORGANISATION_IDENTIFICATION);
            deviceIdentification = objectMessage.getStringProperty(Constants.DEVICE_IDENTIFICATION);

            result = objectMessage.getStringProperty(Constants.RESULT);
            resultType = ResponseMessageResultType.valueOf(objectMessage.getStringProperty(Constants.RESULT));
            message = objectMessage.getStringProperty(Constants.DESCRIPTION);
            notificationType = NotificationType.valueOf(messageType);
        } catch (final JMSException e) {
            LOGGER.error("UNRECOVERABLE ERROR, unable to read ObjectMessage instance, giving up.", e);
            LOGGER.debug("correlationUid: {}", correlationUid);
            LOGGER.debug("messageType: {}", messageType);
            LOGGER.debug("organisationIdentification: {}", organisationIdentification);
            LOGGER.debug("deviceIdentification: {}", deviceIdentification);
            LOGGER.debug("osgpException: {}", osgpException);
            return;
        }

        try {
            LOGGER.info("Calling application service function to handle response: {}", messageType);
            final AdministrativeStatusType data = (AdministrativeStatusType) objectMessage.getObject();
            final MeterResponseData meterResponseData = new MeterResponseData(organisationIdentification, messageType,
                    deviceIdentification, correlationUid, resultType, data);
            this.meterResponseDataService.enqueue(meterResponseData);

            // Notifying
            this.notificationService.sendNotification(organisationIdentification, deviceIdentification, result,
                    correlationUid, message, notificationType);

        } catch (final Exception e) {
            this.handleError(e, correlationUid, organisationIdentification, deviceIdentification, notificationType);
        }

    }

}
