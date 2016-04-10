/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgp.adapter.protocol.dlms.domain.commands;

import java.util.List;

import org.openmuc.jdlms.ClientConnection;
import org.osgp.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.osgp.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alliander.osgp.dto.valueobjects.FirmwareVersionDto;
import com.alliander.osgp.dto.valueobjects.smartmetering.ActionValueObjectResponseDto;
import com.alliander.osgp.dto.valueobjects.smartmetering.FirmwareVersionResponseDataDto;
import com.alliander.osgp.dto.valueobjects.smartmetering.GetFirmwareVersionRequestDataDto;

@Component
public class GetFirmwareVersionsBundleCommandExecutor implements
        CommandExecutor<GetFirmwareVersionRequestDataDto, ActionValueObjectResponseDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetFirmwareVersionsBundleCommandExecutor.class);

    @Autowired
    private GetFirmwareVersionsCommandExecutor getFirmwareVersionsCommandExecutor;

    @Override
    public ActionValueObjectResponseDto execute(final ClientConnection conn, final DlmsDevice device,
            final GetFirmwareVersionRequestDataDto getFirmwareVersionRequestDataDto) {

        List<FirmwareVersionDto> resultList;
        try {
            resultList = this.getFirmwareVersionsCommandExecutor.execute(conn, device, null);
        } catch (final ProtocolAdapterException e) {
            LOGGER.error("Error while getting firmware versions from device: " + device.getDeviceIdentification(), e);

            return new ActionValueObjectResponseDto(e, "Error while getting firmware versions from device: "
                    + device.getDeviceIdentification());
        }

        return new FirmwareVersionResponseDataDto(resultList);
    }
}
