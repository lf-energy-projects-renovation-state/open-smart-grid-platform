/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.ws.smartmetering.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.alliander.osgp.adapter.ws.smartmetering.domain.entities.MeterResponseData;
import com.alliander.osgp.adapter.ws.smartmetering.infra.jms.SmartMeteringRequestMessage;
import com.alliander.osgp.adapter.ws.smartmetering.infra.jms.SmartMeteringRequestMessageSender;
import com.alliander.osgp.adapter.ws.smartmetering.infra.jms.SmartMeteringRequestMessageType;
import com.alliander.osgp.domain.core.services.CorrelationIdProviderService;
import com.alliander.osgp.domain.core.validation.Identification;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.ActivityCalendar;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.AdministrativeStatusType;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.AlarmNotifications;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.SetConfigurationObjectRequest;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.SpecialDaysRequest;
import com.alliander.osgp.shared.exceptionhandling.FunctionalException;

@Service(value = "wsSmartMeteringConfigurationService")
@Validated
public class ConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    private CorrelationIdProviderService correlationIdProviderService;

    @Autowired
    private SmartMeteringRequestMessageSender smartMeteringRequestMessageSender;

    @Autowired
    private MeterResponseDataService meterResponseDataService;

    /**
     * @param organisationIdentification
     * @param requestData
     * @throws FunctionalException
     */
    public String requestSetAdministrativeStatus(final String organisationIdentification,
            final String deviceIdentification, final AdministrativeStatusType requestData) throws FunctionalException {
        return this.enqueueSetAdministrativeStatus(organisationIdentification, deviceIdentification, requestData);
    }

    public String enqueueSetAdministrativeStatus(@Identification final String organisationIdentification,
            @Identification final String deviceIdentification,
            @Identification final AdministrativeStatusType requestData) throws FunctionalException {

        LOGGER.info(
                "enqueueSetAdministrativeStatus called with organisation {} and device {}, set administrative status to {}",
                organisationIdentification, deviceIdentification, requestData);

        final String correlationUid = this.correlationIdProviderService.getCorrelationId(organisationIdentification,
                deviceIdentification);

        final SmartMeteringRequestMessage message = new SmartMeteringRequestMessage(
                SmartMeteringRequestMessageType.SET_ADMINISTRATIVE_STATUS, correlationUid, organisationIdentification,
                deviceIdentification, requestData);

        this.smartMeteringRequestMessageSender.send(message);

        return correlationUid;
    }

    public MeterResponseData dequeueSetAdministrativeStatusResponse(final String correlationUid)
            throws FunctionalException {
        return this.meterResponseDataService.dequeue(correlationUid);
    }

    public String requestGetAdministrativeStatus(final String organisationIdentification,
            final String deviceIdentification) {
        return this.enqueueGetAdministrativeStatus(organisationIdentification, deviceIdentification);
    }

    private String enqueueGetAdministrativeStatus(final String organisationIdentification,
            final String deviceIdentification) {
        LOGGER.debug("enqueueGetAdministrativeStatus called with organisation {} and device {}",
                organisationIdentification, deviceIdentification);

        final String correlationUid = this.correlationIdProviderService.getCorrelationId(organisationIdentification,
                deviceIdentification);

        final SmartMeteringRequestMessage message = new SmartMeteringRequestMessage(
                SmartMeteringRequestMessageType.GET_ADMINISTRATIVE_STATUS, correlationUid, organisationIdentification,
                deviceIdentification, AdministrativeStatusType.UNDEFINED);

        this.smartMeteringRequestMessageSender.send(message);

        return correlationUid;
    }

    public MeterResponseData dequeueGetAdministrativeStatusResponse(final String correlationUid)
            throws FunctionalException {
        return this.meterResponseDataService.dequeue(correlationUid);
    }

    public String enqueueSetSpecialDaysRequest(@Identification final String organisationIdentification,
            @Identification final String deviceIdentification, final SpecialDaysRequest requestData)
            throws FunctionalException {

        LOGGER.debug("enqueueSetSpecialDaysRequest called with organisation {} and device {}",
                organisationIdentification, deviceIdentification);

        final String correlationUid = this.correlationIdProviderService.getCorrelationId(organisationIdentification,
                deviceIdentification);

        final SmartMeteringRequestMessage message = new SmartMeteringRequestMessage(
                SmartMeteringRequestMessageType.REQUEST_SPECIAL_DAYS, correlationUid, organisationIdentification,
                deviceIdentification, requestData);

        this.smartMeteringRequestMessageSender.send(message);

        return correlationUid;
    }

    public MeterResponseData dequeueSetSpecialDaysResponse(final String correlationUid) throws FunctionalException {
        return this.meterResponseDataService.dequeue(correlationUid);
    }

    public String enqueueSetConfigurationObjectRequest(@Identification final String organisationIdentification,
            @Identification final String deviceIdentification, final SetConfigurationObjectRequest requestData)
            throws FunctionalException {

        LOGGER.debug("enqueueSetConfigurationObjectRequest called with organisation {} and device {}",
                organisationIdentification, deviceIdentification);

        final String correlationUid = this.correlationIdProviderService.getCorrelationId(organisationIdentification,
                deviceIdentification);

        final SmartMeteringRequestMessage message = new SmartMeteringRequestMessage(
                SmartMeteringRequestMessageType.SET_CONFIGURATION_OBJECT, correlationUid, organisationIdentification,
                deviceIdentification, requestData);

        this.smartMeteringRequestMessageSender.send(message);

        return correlationUid;
    }

    public MeterResponseData dequeueSetConfigurationObjectResponse(final String correlationUid)
            throws FunctionalException {
        return this.meterResponseDataService.dequeue(correlationUid);
    }

    public String enqueueSetAlarmNotificationsRequest(@Identification final String organisationIdentification,
            @Identification final String deviceIdentification, final AlarmNotifications alarmSwitches)
            throws FunctionalException {

        LOGGER.debug("enqueueSetAlarmNotificationsRequest called with organisation {} and device {}",
                organisationIdentification, deviceIdentification);

        final String correlationUid = this.correlationIdProviderService.getCorrelationId(organisationIdentification,
                deviceIdentification);

        final SmartMeteringRequestMessage message = new SmartMeteringRequestMessage(
                SmartMeteringRequestMessageType.SET_ALARM_NOTIFICATIONS, correlationUid, organisationIdentification,
                deviceIdentification, alarmSwitches);

        this.smartMeteringRequestMessageSender.send(message);

        return correlationUid;
    }

    public String enqueueSetActivityCalendarRequest(@Identification final String organisationIdentification,
            @Identification final String deviceIdentification, final ActivityCalendar activityCalendar)
                    throws FunctionalException {

        LOGGER.debug("enqueueSetActivityCalendarRequest called with organisation {} and device {}",
                organisationIdentification, deviceIdentification);

        final String correlationUid = this.correlationIdProviderService.getCorrelationId(organisationIdentification,
                deviceIdentification);

        final SmartMeteringRequestMessage message = new SmartMeteringRequestMessage(
                SmartMeteringRequestMessageType.SET_ACTIVITY_CALENDAR, correlationUid, organisationIdentification,
                deviceIdentification, activityCalendar);

        this.smartMeteringRequestMessageSender.send(message);

        return correlationUid;
    }

    /**
     * @param organisationIdentification
     * @param deviceIdentification
     * @param alarmSwitches
     * @throws FunctionalException
     */
    public String setAlarmNotifications(final String organisationIdentification, final String deviceIdentification,
            final AlarmNotifications alarmSwitches) throws FunctionalException {
        return this
                .enqueueSetAlarmNotificationsRequest(organisationIdentification, deviceIdentification, alarmSwitches);
    }

    public String setActivityCalendar(final String organisationIdentification, final String deviceIdentification,
            final ActivityCalendar activityCalendar) throws FunctionalException {
        return this.enqueueSetActivityCalendarRequest(organisationIdentification, deviceIdentification,
                activityCalendar);
    }
}
