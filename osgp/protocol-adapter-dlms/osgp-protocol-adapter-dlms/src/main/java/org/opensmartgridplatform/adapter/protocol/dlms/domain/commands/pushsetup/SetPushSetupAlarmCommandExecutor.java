// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.pushsetup;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.application.mapping.PushSetupMapper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.ObjectConfigServiceHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.DlmsConnectionManager;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.NotSupportedByProtocolException;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ActionRequestDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ActionResponseDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.CosemObjectDefinitionDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PushSetupAlarmDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.SetPushSetupAlarmRequestDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.TransportServiceTypeDto;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalException;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.springframework.stereotype.Component;

@Slf4j
@Component()
public class SetPushSetupAlarmCommandExecutor
    extends SetPushSetupCommandExecutor<PushSetupAlarmDto, AccessResultCode> {

  private final DlmsHelper dlmsHelper;
  private final PushSetupMapper pushSetupMapper;

  public SetPushSetupAlarmCommandExecutor(
      final DlmsHelper dlmsHelper,
      final PushSetupMapper pushSetupMapper,
      final ObjectConfigServiceHelper objectConfigServiceHelper) {
    super(SetPushSetupAlarmRequestDto.class, objectConfigServiceHelper);
    this.dlmsHelper = dlmsHelper;
    this.pushSetupMapper = pushSetupMapper;
  }

  @Override
  public PushSetupAlarmDto fromBundleRequestInput(final ActionRequestDto bundleInput)
      throws ProtocolAdapterException {

    this.checkActionRequestType(bundleInput);
    final SetPushSetupAlarmRequestDto setPushSetupAlarmRequestDto =
        (SetPushSetupAlarmRequestDto) bundleInput;

    return setPushSetupAlarmRequestDto.getPushSetupAlarm();
  }

  @Override
  public ActionResponseDto asBundleResponse(final AccessResultCode executionResult)
      throws ProtocolAdapterException {

    this.checkAccessResultCode(executionResult);

    return new ActionResponseDto("Setting push setup alarm was successful");
  }

  @Override
  public AccessResultCode execute(
      final DlmsConnectionManager conn,
      final DlmsDevice device,
      final PushSetupAlarmDto pushSetupAlarm,
      final MessageMetadata messageMetadata)
      throws ProtocolAdapterException {

    this.checkPushSetupAlarm(pushSetupAlarm);

    AccessResultCode resultCode = null;

    if (pushSetupAlarm.hasSendDestinationAndMethod()) {
      resultCode = this.setSendDestinationAndMethod(conn, pushSetupAlarm, device);

      if (resultCode != AccessResultCode.SUCCESS) {
        return resultCode;
      }
    }

    if (pushSetupAlarm.hasPushObjectList()) {
      resultCode = this.setPushObjectList(conn, pushSetupAlarm, device);
    }

    return resultCode;
  }

  private AccessResultCode setSendDestinationAndMethod(
      final DlmsConnectionManager conn,
      final PushSetupAlarmDto pushSetupAlarm,
      final DlmsDevice device)
      throws ProtocolAdapterException {
    log.debug(
        "Setting Send destination and method of Push Setup Alarm: {}",
        pushSetupAlarm.getSendDestinationAndMethod());

    final SetParameter setParameterSendDestinationAndMethod =
        this.getSetParameterSendDestinationAndMethod(pushSetupAlarm, device);
    final AccessResultCode resultCode =
        this.doSetRequest(
            "PushSetupAlarm, Send destination and method",
            conn,
            setParameterSendDestinationAndMethod);

    if (resultCode != null) {
      return resultCode;
    } else {
      throw new ProtocolAdapterException(
          "Error setting Alarm push setup data (destination and method.");
    }
  }

  private SetParameter getSetParameterSendDestinationAndMethod(
      final PushSetupAlarmDto pushSetupAlarm, final DlmsDevice device)
      throws NotSupportedByProtocolException {

    final AttributeAddress sendDestinationAndMethodAddress =
        this.getSendDestinationAndMethodAddress(
            Protocol.forDevice(device), DlmsObjectType.PUSH_SETUP_ALARM);
    final DataObject value =
        this.pushSetupMapper.map(
            this.getUpdatedSendDestinationAndMethod(
                pushSetupAlarm.getSendDestinationAndMethod(), device),
            DataObject.class);

    return new SetParameter(sendDestinationAndMethodAddress, value);
  }

  private AccessResultCode setPushObjectList(
      final DlmsConnectionManager conn,
      final PushSetupAlarmDto pushSetupAlarm,
      final DlmsDevice device)
      throws ProtocolAdapterException {
    log.debug(
        "Setting Push Object List of Push Setup Alarm: {}", pushSetupAlarm.getPushObjectList());

    // Before setting the push object list, verify if the objects in the list are really present in
    // the meter
    this.verifyPushObjects(pushSetupAlarm.getPushObjectList(), conn);

    final SetParameter setParameterPushObjectList =
        this.getSetParameterPushObjectList(pushSetupAlarm, device);

    final AccessResultCode resultCode =
        this.doSetRequest("PushSetupAlarm, push object list", conn, setParameterPushObjectList);

    if (resultCode != null) {
      return resultCode;
    } else {
      throw new ProtocolAdapterException("Error setting Alarm push setup data (push object list).");
    }
  }

  private void verifyPushObjects(
      final List<CosemObjectDefinitionDto> pushObjects, final DlmsConnectionManager conn)
      throws ProtocolAdapterException {
    for (final CosemObjectDefinitionDto pushObject : pushObjects) {
      this.verifyPushObject(pushObject, conn);
    }
  }

  private void verifyPushObject(
      final CosemObjectDefinitionDto pushObject, final DlmsConnectionManager conn)
      throws ProtocolAdapterException {
    final int dataIndex = pushObject.getDataIndex();
    if (dataIndex != 0) {
      throw new ProtocolAdapterException(
          "PushObject contains non-zero data index: "
              + dataIndex
              + ". Using data index is not implemented.");
    }

    final ObisCode obisCode = new ObisCode(pushObject.getLogicalName().toByteArray());

    final AttributeAddress attributeAddress =
        new AttributeAddress(pushObject.getClassId(), obisCode, pushObject.getAttributeIndex());

    try {
      this.dlmsHelper.getAttributeValue(conn, attributeAddress);
    } catch (final FunctionalException e) {
      throw new ProtocolAdapterException(
          "Verification of push object failed. Object "
              + obisCode.asDecimalString()
              + " could not be retrieved using a get request.",
          e);
    }
  }

  private SetParameter getSetParameterPushObjectList(
      final PushSetupAlarmDto pushSetupAlarm, final DlmsDevice device)
      throws NotSupportedByProtocolException {

    final AttributeAddress pushObjectListAddress =
        this.getPushObjectListAddress(Protocol.forDevice(device), DlmsObjectType.PUSH_SETUP_ALARM);
    final DataObject value =
        DataObject.newArrayData(
            this.pushSetupMapper.mapAsList(pushSetupAlarm.getPushObjectList(), DataObject.class));

    return new SetParameter(pushObjectListAddress, value);
  }

  private void checkPushSetupAlarm(final PushSetupAlarmDto pushSetupAlarm)
      throws ProtocolAdapterException {
    if (!pushSetupAlarm.hasSendDestinationAndMethod() && !pushSetupAlarm.hasPushObjectList()) {
      throw new ProtocolAdapterException(
          "SetPushSetupAlarmCommandExecutor called without any valid option set in request.");
    }

    if (pushSetupAlarm.hasCommunicationWindow()) {
      log.warn(
          "Setting Communication Window of Push Setup Alarm not implemented: {}",
          pushSetupAlarm.getCommunicationWindow());
    }
    if (pushSetupAlarm.hasRandomisationStartInterval()) {
      log.warn(
          "Setting Randomisation Start Interval of Push Setup Alarm not implemented: {}",
          pushSetupAlarm.getRandomisationStartInterval());
    }
    if (pushSetupAlarm.hasNumberOfRetries()) {
      log.warn(
          "Setting Number of Retries of Push Setup Alarm not implemented: {}",
          pushSetupAlarm.getNumberOfRetries());
    }
    if (pushSetupAlarm.hasRepetitionDelay()) {
      log.warn(
          "Setting Repetition Delay of Push Setup Alarm not implemented: {}",
          pushSetupAlarm.getRepetitionDelay());
    }
  }

  @Override
  protected TransportServiceTypeDto getTransportServiceType() {
    return TransportServiceTypeDto.TCP;
  }
}
