// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.infra.networking;

import static org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol.SMR_5_0_0;
import static org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol.SMR_5_5;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.EXTERNAL_TRIGGER;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.INTERNAL_TRIGGER_ALARM;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.PUSH_SCHEDULER;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.PUSH_SETUP_ALARM;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.PUSH_SETUP_SCHEDULER;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.PUSH_SETUP_SMS;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.PUSH_SETUP_UDP;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.jdlms.ObisCode;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dlms.DlmsPushNotification;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;
import org.opensmartgridplatform.dlms.services.ObjectConfigService;
import org.springframework.stereotype.Component;

@Slf4j
@Component()
public class Smr5AlarmDecoder extends AlarmDecoder {

  private final ObjectConfigService objectConfigService;

  private static final int SMR5_NUMBER_OF_BYTES_FOR_ADDRESSING = 8;
  private static final int SMR5_NUMBER_OF_BYTES_FOR_INVOKE_ID = 4;

  /** DLMS data types used in the SMR5 push notification */
  private static final byte STRUCTURE = 0x02;

  private static final byte OCTET_STRING = 0x09;
  private static final byte DOUBLE_LONG_UNSIGNED = 0x06;

  public Smr5AlarmDecoder(final ObjectConfigService objectConfigService) {
    this.objectConfigService = objectConfigService;
  }

  public DlmsPushNotification decodeSmr5alarm(final InputStream inputStream)
      throws UnrecognizedMessageDataException {
    final DlmsPushNotification.Builder builder = new DlmsPushNotification.Builder();

    // Skip addressing, the 0x0F byte and the invoke-id-and-priority bytes
    this.skip(
        inputStream, SMR5_NUMBER_OF_BYTES_FOR_ADDRESSING + 1 + SMR5_NUMBER_OF_BYTES_FOR_INVOKE_ID);

    // Datetime is not used, so can be skipped as well
    this.skipDateTime(inputStream);

    // Next byte should indicate a Structure
    final byte dataTypeByte = this.readByte(inputStream);
    if (dataTypeByte != STRUCTURE) {
      throw new UnrecognizedMessageDataException(
          "Expected a structure (0x02), but encountered: " + dataTypeByte);
    }
    builder.appendByte(dataTypeByte);

    // Next byte should indicate the amount of elements in the structure: 2, 3 or 4
    final byte dataLength = this.readByte(inputStream);
    if (dataLength != 2 && dataLength != 3 && dataLength != 4) {
      throw new UnrecognizedMessageDataException(
          "Expected a structure with 2, 3 or 4 elements, but amount is " + dataLength);
    }
    builder.appendByte(dataLength);

    // If the alarm contains 3 elements, then the 3rd element is the alarm register
    // If the alarm contains 4 elements, then the 4th element is the alarm register 2
    final boolean alarmRegisterExpected = (dataLength == 3 || dataLength == 4);

    // Decode elements
    this.decodeEquipmentIdentifier(inputStream, builder);
    this.decodeLogicalName(inputStream, alarmRegisterExpected, builder);

    if (PUSH_ALARM_TRIGGER.equals(builder.getTriggerType())) {
      this.decodePushAlarm(dataLength, inputStream, builder);
    } else if (PUSH_UDP_TRIGGER.equals(builder.getTriggerType())) {
      this.decodePushUdp(dataLength, inputStream, builder);
    }
    return builder.build();
  }

  private void decodePushUdp(
      final byte dataLength,
      final InputStream inputStream,
      final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    if (dataLength >= 3) {
      this.decodeAlarms(inputStream, DlmsObjectType.ALARM_REGISTER_3, builder);
    }
  }

  private void decodePushAlarm(
      final byte dataLength,
      final InputStream inputStream,
      final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    if (dataLength >= 3) {
      this.decodeAlarms(inputStream, DlmsObjectType.ALARM_REGISTER_1, builder);
    }
    if (dataLength == 4) {
      // SMR 5.2
      this.decodeAlarms(inputStream, DlmsObjectType.ALARM_REGISTER_2, builder);
    }
  }

  private void skipDateTime(final InputStream inputStream) throws UnrecognizedMessageDataException {
    // The first byte of the datetime is the length byte
    final byte dateTimeLength = this.readByte(inputStream);

    // Skip the rest of the datetime bytes
    this.skip(inputStream, dateTimeLength);
  }

  private void decodeEquipmentIdentifier(
      final InputStream inputStream, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {

    // First byte should indicate octet-string
    final byte dataTypeByte = this.readByte(inputStream);
    if (dataTypeByte != OCTET_STRING) {
      throw new UnrecognizedMessageDataException(
          "Expected an octet-string (0x09), but encountered: " + dataTypeByte);
    }
    builder.appendByte(dataTypeByte);

    // Next byte should be the length of the octet-string
    final byte dataLength = this.readByte(inputStream);
    if (dataLength != EQUIPMENT_IDENTIFIER_LENGTH) {
      throw new UnrecognizedMessageDataException(
          "Expected an identifier with length "
              + EQUIPMENT_IDENTIFIER_LENGTH
              + ", but specified length is: "
              + dataLength);
    }
    builder.appendByte(dataLength);

    // Read the identifier
    final byte[] equipmentIdentifierBytes = this.readBytes(inputStream, dataLength);

    builder.withEquipmentIdentifier(
        new String(equipmentIdentifierBytes, StandardCharsets.US_ASCII));
    builder.appendBytes(equipmentIdentifierBytes);
  }

  private void decodeLogicalName(
      final InputStream inputStream,
      final boolean alarmExpected,
      final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    // First byte should indicate octet-string
    final byte dataTypeByte = this.readByte(inputStream);
    if (dataTypeByte != OCTET_STRING) {
      throw new UnrecognizedMessageDataException(
          "Expected an octet-string (0x09), but encountered: " + dataTypeByte);
    }
    builder.appendByte(dataTypeByte);

    // Next byte should be the length of the octet-string
    final byte dataLength = this.readByte(inputStream);
    if (dataLength != NUMBER_OF_BYTES_FOR_LOGICAL_NAME) {
      throw new UnrecognizedMessageDataException(
          "Expected a logical name with length "
              + NUMBER_OF_BYTES_FOR_LOGICAL_NAME
              + ", but specified length is: "
              + dataLength);
    }
    builder.appendByte(dataLength);

    // Next bytes are the logical name
    this.decodeObisCodeData(inputStream, alarmExpected, builder);
  }

  private void decodeAlarms(
      final InputStream inputStream,
      final DlmsObjectType dlmsObjectType,
      final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    // First byte should indicate double-long-unsigned
    final byte dataTypeByte = this.readByte(inputStream);
    if (dataTypeByte != DOUBLE_LONG_UNSIGNED) {
      throw new UnrecognizedMessageDataException(
          "Expected a double-long-unsigned (0x06), but encountered: " + dataTypeByte);
    }
    builder.appendByte(dataTypeByte);

    // Next bytes are the alarm
    this.decodeAlarmRegisterData(inputStream, builder, dlmsObjectType);
  }

  private void decodeObisCodeData(
      final InputStream inputStream,
      final boolean alarmRegisterExpected,
      final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {

    final byte[] logicalNameBytes = this.readBytes(inputStream, NUMBER_OF_BYTES_FOR_LOGICAL_NAME);

    try {
      if (!alarmRegisterExpected && this.isLogicalNameSmsTrigger(logicalNameBytes)) {
        builder.withTriggerType(PUSH_SMS_TRIGGER);
      } else if (!alarmRegisterExpected && this.isLogicalNameCsdTrigger(logicalNameBytes)) {
        log.warn("CSD Push notification not supported");
        builder.withTriggerType(PUSH_CSD_TRIGGER);
      } else if (!alarmRegisterExpected && this.isLogicalNameSchedulerTrigger(logicalNameBytes)) {
        log.warn("Scheduler Push notification not supported");
        builder.withTriggerType(PUSH_SCHEDULER_TRIGGER);
      } else if (alarmRegisterExpected && this.isLogicalNameAlarmTrigger(logicalNameBytes)) {
        builder.withTriggerType(PUSH_ALARM_TRIGGER);
      } else if (alarmRegisterExpected && this.isLogicalNameUdpTrigger(logicalNameBytes)) {
        builder.withTriggerType(PUSH_UDP_TRIGGER);
      } else {
        log.warn("Unknown Push notification not supported. Unable to decode");
        builder.withTriggerType("");
      }
    } catch (final ProtocolAdapterException e) {
      throw new UnrecognizedMessageDataException("Error decoding logical name", e);
    }

    builder.withAlarms(null);
    builder.appendBytes(logicalNameBytes);
  }

  private boolean isLogicalNameSmsTrigger(final byte[] logicalNameBytes)
      throws ProtocolAdapterException {
    // SMR5 has one general object for the SMS and CSD triggers
    return Arrays.equals(this.getObisBytes(EXTERNAL_TRIGGER, SMR_5_0_0), logicalNameBytes)
        || Arrays.equals(this.getObisBytes(PUSH_SETUP_SMS, SMR_5_0_0), logicalNameBytes);
  }

  private boolean isLogicalNameCsdTrigger(final byte[] logicalNameBytes)
      throws ProtocolAdapterException {
    // SMR5 has one general object for the SMS and CSD triggers
    return Arrays.equals(this.getObisBytes(EXTERNAL_TRIGGER, SMR_5_0_0), logicalNameBytes);
  }

  private boolean isLogicalNameSchedulerTrigger(final byte[] logicalNameBytes)
      throws ProtocolAdapterException {
    return Arrays.equals(this.getObisBytes(PUSH_SCHEDULER, SMR_5_0_0), logicalNameBytes)
        || Arrays.equals(this.getObisBytes(PUSH_SETUP_SCHEDULER, SMR_5_0_0), logicalNameBytes);
  }

  private boolean isLogicalNameAlarmTrigger(final byte[] logicalNameBytes)
      throws ProtocolAdapterException {
    return Arrays.equals(this.getObisBytes(INTERNAL_TRIGGER_ALARM, SMR_5_0_0), logicalNameBytes)
        || Arrays.equals(this.getObisBytes(PUSH_SETUP_ALARM, SMR_5_0_0), logicalNameBytes);
  }

  private boolean isLogicalNameUdpTrigger(final byte[] logicalNameBytes)
      throws ProtocolAdapterException {
    return Arrays.equals(this.getObisBytes(PUSH_SETUP_UDP, SMR_5_5), logicalNameBytes);
  }

  private byte[] getObisBytes(final DlmsObjectType type, final Protocol protocol)
      throws ProtocolAdapterException {
    try {
      final String obis =
          this.objectConfigService
              .getCosemObject(protocol.getName(), protocol.getVersion(), type)
              .getObis();
      return new ObisCode(obis).bytes();
    } catch (final ObjectConfigException e) {
      throw new ProtocolAdapterException(
          "Missing " + type.name() + " in object config for " + protocol.name(), e);
    }
  }
}
