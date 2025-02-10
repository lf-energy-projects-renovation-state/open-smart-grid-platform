// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.misc;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.AbstractCommandExecutor;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.DlmsConnectionManager;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.objectconfig.Attribute;
import org.opensmartgridplatform.dlms.objectconfig.CosemObject;
import org.opensmartgridplatform.dlms.objectconfig.DlmsDataType;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;
import org.opensmartgridplatform.dlms.services.ObjectConfigService;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ActionResponseDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.SetSpecificAttributeValueRequestDto;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalException;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SetSpecificAttributeValueCommandExecutor
    extends AbstractCommandExecutor<SetSpecificAttributeValueRequestDto, Void> {

  private final DlmsHelper dlmsHelper;
  private final ObjectConfigService objectConfigService;

  public SetSpecificAttributeValueCommandExecutor(
      final DlmsHelper dlmsHelper, final ObjectConfigService objectConfigService) {
    super(SetSpecificAttributeValueRequestDto.class);
    this.objectConfigService = objectConfigService;
    this.dlmsHelper = dlmsHelper;
  }

  @Override
  public ActionResponseDto asBundleResponse(final Void executionResult)
      throws ProtocolAdapterException {
    return new ActionResponseDto("Set specific attribute was successful");
  }

  @Override
  public Void execute(
      final DlmsConnectionManager conn,
      final DlmsDevice device,
      final SetSpecificAttributeValueRequestDto requestData,
      final MessageMetadata messageMetadata)
      throws FunctionalException, ProtocolAdapterException {

    final DlmsObjectType objectType = DlmsObjectType.valueOf(requestData.getDlmsObjectTag());
    final CosemObject cosemObject;
    try {
      cosemObject =
          this.objectConfigService.getCosemObject(
              device.getProtocolName(), device.getProtocolVersion(), objectType);
    } catch (final ObjectConfigException e) {
      throw new ProtocolAdapterException(AbstractCommandExecutor.ERROR_IN_OBJECT_CONFIG, e);
    }

    final Attribute attribute = cosemObject.getAttribute(requestData.getAttributeId());

    final DlmsDataType dataType = attribute.getDatatype();

    final DataObject data =
        switch (dataType) {
          case UNSIGNED -> DataObject.newUInteger8Data(requestData.getIntValue().shortValue());
          case LONG_UNSIGNED -> DataObject.newUInteger16Data(requestData.getIntValue());
          case DOUBLE_LONG_UNSIGNED -> DataObject.newUInteger32Data(requestData.getIntValue());
          case LONG64_UNSIGNED -> DataObject.newUInteger64Data(requestData.getIntValue());
          case INTEGER -> DataObject.newInteger8Data(requestData.getIntValue().byteValue());
          case LONG -> DataObject.newInteger16Data(requestData.getIntValue().shortValue());
          case DOUBLE_LONG -> DataObject.newInteger32Data(requestData.getIntValue());
          case LONG64 -> DataObject.newInteger64Data(requestData.getIntValue());
          default ->
              throw new ProtocolAdapterException(
                  "Datatype " + dataType.name() + " not supported for integer value");
        };

    final AttributeAddress attributeAddress =
        new AttributeAddress(
            cosemObject.getClassId(),
            new ObisCode(cosemObject.getObis()),
            requestData.getAttributeId());
    final SetParameter setParameter = new SetParameter(attributeAddress, data);

    log.debug(
        "Set specific attribute value, class id: {}, obis code: {}, attribute id: {}, value: {}",
        cosemObject.getClassId(),
        cosemObject.getObis(),
        requestData.getAttributeId(),
        requestData.getIntValue());

    final List<AccessResultCode> resultCodes =
        this.dlmsHelper.setWithList(conn, device, List.of(setParameter));

    if (!resultCodes.stream().allMatch(code -> code.equals(AccessResultCode.SUCCESS))) {
      log.debug("Result of THD configuration is {}", resultCodes);
      throw new ProtocolAdapterException("THD configuration resulted in: " + resultCodes);
    }

    return null;
  }
}
