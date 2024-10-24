// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.configuration.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.datatypes.BitString;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.ObjectConfigServiceHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.repositories.DlmsDeviceRepository;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ConfigurationFlagDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ConfigurationFlagTypeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ConfigurationFlagsDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ConfigurationObjectDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.GprsOperationModeTypeDto;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GetConfigurationObjectServiceDsmr4 extends GetConfigurationObjectService {

  private static final int NUMBER_OF_CONFIGURATION_OBJECT_ELEMENTS = 2;
  private static final int INDEX_OF_GPRS_OPERATION_MODE = 0;
  private static final int INDEX_OF_CONFIGURATION_FLAGS = 1;

  private final DlmsHelper dlmsHelper;

  private final ObjectConfigServiceHelper objectConfigServiceHelper;

  public GetConfigurationObjectServiceDsmr4(
      final DlmsHelper dlmsHelper,
      final ObjectConfigServiceHelper objectConfigServiceHelper,
      final DlmsDeviceRepository dlmsDeviceRepository) {
    super(dlmsDeviceRepository);
    this.dlmsHelper = dlmsHelper;
    this.objectConfigServiceHelper = objectConfigServiceHelper;
  }

  @Override
  public boolean handles(final Protocol protocol) {
    return protocol != null && protocol.isDsmr42();
  }

  @Override
  ConfigurationObjectDto getConfigurationObject(final GetResult result)
      throws ProtocolAdapterException {
    final DataObject resultData = result.getResultData();
    if (resultData == null || !resultData.isComplex()) {
      final String message =
          String.format(
              "Expected ConfigurationObject ResultData as Complex, but got: %s", resultData);
      log.warn(message);
      throw new ProtocolAdapterException(message);
    }
    return this.getConfigurationObject(resultData);
  }

  private ConfigurationObjectDto getConfigurationObject(final DataObject resultData)
      throws ProtocolAdapterException {
    log.debug("ConfigurationObject ResultData: {}", this.dlmsHelper.getDebugInfo(resultData));
    final List<DataObject> elements = resultData.getValue();
    if (elements == null || elements.size() != NUMBER_OF_CONFIGURATION_OBJECT_ELEMENTS) {
      final String message =
          String.format(
              "Expected ConfigurationObject ResultData with %d elements, but got %s",
              NUMBER_OF_CONFIGURATION_OBJECT_ELEMENTS, elements == null ? "null" : elements.size());
      log.warn(message);
      throw new ProtocolAdapterException(message);
    }
    return this.getConfigurationObject(elements);
  }

  private ConfigurationObjectDto getConfigurationObject(final List<DataObject> elements)
      throws ProtocolAdapterException {
    final Optional<GprsOperationModeTypeDto> gprsMode =
        this.getGprsOperationMode(elements.get(INDEX_OF_GPRS_OPERATION_MODE));
    final ConfigurationFlagsDto flags =
        this.getConfigurationFlags(elements.get(INDEX_OF_CONFIGURATION_FLAGS));
    return gprsMode
        .map(c -> new ConfigurationObjectDto(c, flags))
        .orElseGet(() -> new ConfigurationObjectDto(flags));
  }

  private Optional<GprsOperationModeTypeDto> getGprsOperationMode(final DataObject gprsMode)
      throws ProtocolAdapterException {
    if (gprsMode == null || !gprsMode.isNumber()) {
      final String message =
          String.format(
              "Expected ConfigurationObject gprsOperationMode as Number, but got: %s", gprsMode);
      log.warn(message);
      throw new ProtocolAdapterException(message);
    }
    final Number number = gprsMode.getValue();
    return GprsOperationModeTypeDto.forNumber(number.intValue());
  }

  private ConfigurationFlagsDto getConfigurationFlags(final DataObject flags)
      throws ProtocolAdapterException {

    if (flags == null || !flags.isBitString()) {
      final String message =
          String.format("Expected ConfigurationObject flags as BitString, but got: %s", flags);
      log.warn(message);
      throw new ProtocolAdapterException(message);
    }

    final BitString bitString = flags.getValue();
    final byte[] flagBytes = bitString.getBitString();
    final List<ConfigurationFlagDto> configurationFlags = this.toConfigurationFlags(flagBytes);
    this.addLowFlags(configurationFlags);
    return new ConfigurationFlagsDto(configurationFlags);
  }

  protected void addLowFlags(final List<ConfigurationFlagDto> configurationFlags) {
    final List<ConfigurationFlagTypeDto> highFlags =
        configurationFlags.stream().map(ConfigurationFlagDto::getConfigurationFlagType).toList();
    final Stream<ConfigurationFlagTypeDto> missingFlags =
        Arrays.stream(ConfigurationFlagTypeDto.values())
            .filter(
                configurationFlagTypeDto ->
                    !highFlags.contains(configurationFlagTypeDto)
                        && configurationFlagTypeDto.getBitPositionDsmr4().isPresent());
    missingFlags.forEach(
        missingFlag -> configurationFlags.add(new ConfigurationFlagDto(missingFlag, false)));
  }

  @Override
  Optional<ConfigurationFlagTypeDto> getFlagType(final int bitPosition) {
    return ConfigurationFlagTypeDto.getDsmr4FlagType(bitPosition);
  }

  @Override
  AttributeAddress getAttributeAddress(final Protocol protocol) throws ProtocolAdapterException {
    return this.objectConfigServiceHelper
        .findOptionalDefaultAttributeAddress(protocol, DlmsObjectType.CONFIGURATION_OBJECT)
        .orElseThrow();
  }
}
