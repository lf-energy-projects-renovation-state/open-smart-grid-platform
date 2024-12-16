// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.infra.networking;

import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.EXTERNAL_TRIGGER_CSD;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.EXTERNAL_TRIGGER_SMS;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.PUSH_SCHEDULER;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.PUSH_SETUP_SCHEDULER;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.jdlms.ObisCode;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dlms.DlmsPushNotification;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;
import org.opensmartgridplatform.dlms.services.ObjectConfigService;
import org.springframework.stereotype.Component;

@Slf4j
@Component()
public class Dsmr4AlarmDecoder extends AlarmDecoder {

  private final ObjectConfigService objectConfigService;

  /**
   * The elements inside the DSMR4 DLMS Push notification (Alarm or Wakeup SMS are expressed in
   * bytes separated b0y a comma (byte 0x2C).
   */
  private static final byte COMMA = 0x2C;

  public Dsmr4AlarmDecoder(final ObjectConfigService objectConfigService) {
    this.objectConfigService = objectConfigService;
  }

  public DlmsPushNotification decodeDsmr4alarm(final InputStream inputStream)
      throws UnrecognizedMessageDataException {
    final DlmsPushNotification.Builder builder = new DlmsPushNotification.Builder();

    this.decodeEquipmentIdentifier(inputStream, builder);
    this.decodeReceivedData(inputStream, builder);

    return builder.build();
  }

  private void decodeEquipmentIdentifier(
      final InputStream inputStream, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {

    final byte[] equipmentIdentifierPlusSeparatorBytes =
        this.readBytes(inputStream, EQUIPMENT_IDENTIFIER_LENGTH + 1);

    if (equipmentIdentifierPlusSeparatorBytes[EQUIPMENT_IDENTIFIER_LENGTH] != COMMA) {
      throw new UnrecognizedMessageDataException(
          "message must start with "
              + EQUIPMENT_IDENTIFIER_LENGTH
              + " bytes for the equipment identifier, followed by byte 0x2C (a comma).");
    }

    final byte[] equipmentIdentifierBytes =
        Arrays.copyOfRange(equipmentIdentifierPlusSeparatorBytes, 0, EQUIPMENT_IDENTIFIER_LENGTH);
    final String equipmentIdentifier =
        new String(equipmentIdentifierBytes, StandardCharsets.US_ASCII);
    builder.withEquipmentIdentifier(equipmentIdentifier);
    builder.appendBytes(equipmentIdentifierPlusSeparatorBytes);
  }

  private void decodeReceivedData(
      final InputStream inputStream, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    // SLIM-1711 Is a very weird bug, where readableBytes turns out to be
    // almost MAXINT
    // Seems like BigEndianHeapChannelBuffer has some kind of
    // overflow/underflow.
    final int readableBytes;
    try {
      readableBytes = inputStream.available();
      if (readableBytes > Math.max(NUMBER_OF_BYTES_FOR_ALARM, NUMBER_OF_BYTES_FOR_LOGICAL_NAME)) {
        throw new UnrecognizedMessageDataException(
            "length of data bytes is not "
                + NUMBER_OF_BYTES_FOR_ALARM
                + " (alarm) or "
                + NUMBER_OF_BYTES_FOR_LOGICAL_NAME
                + " (obiscode)");
      }
    } catch (final IOException e) {
      throw new UnrecognizedMessageDataException(e.getMessage(), e);
    }

    if (readableBytes == NUMBER_OF_BYTES_FOR_ALARM) {
      this.decodeAlarmRegisterData(inputStream, builder, DlmsObjectType.ALARM_REGISTER_1);
    } else if (readableBytes == NUMBER_OF_BYTES_FOR_LOGICAL_NAME) {
      this.decodeObisCodeData(inputStream, builder);
    } else {
      throw new UnrecognizedMessageDataException(
          "Incorrect amount of bytes: "
              + readableBytes
              + ". Expected "
              + NUMBER_OF_BYTES_FOR_ALARM
              + " or "
              + NUMBER_OF_BYTES_FOR_LOGICAL_NAME);
    }
  }

  private void decodeObisCodeData(
      final InputStream inputStream, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {

    final byte[] logicalNameBytes = this.readBytes(inputStream, NUMBER_OF_BYTES_FOR_LOGICAL_NAME);

    try {
      if (this.isLogicalNameSmsTrigger(logicalNameBytes)) {
        builder.withTriggerType(PUSH_SMS_TRIGGER);
      } else if (this.isLogicalNameCsdTrigger(logicalNameBytes)) {
        log.warn("CSD Push notification not supported");
        builder.withTriggerType(PUSH_CSD_TRIGGER);
      } else if (this.isLogicalNameSchedulerTrigger(logicalNameBytes)) {
        log.warn("Scheduler Push notification not supported");
        builder.withTriggerType(PUSH_SCHEDULER_TRIGGER);
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
    // DSMR4 has specific objects for the different external trigger types for SMS and CSD
    return Arrays.equals(this.getObisBytes(EXTERNAL_TRIGGER_SMS), logicalNameBytes);
  }

  private boolean isLogicalNameCsdTrigger(final byte[] logicalNameBytes)
      throws ProtocolAdapterException {
    // DSMR4 has specific objects for the different external trigger types for SMS and CSD
    return Arrays.equals(this.getObisBytes(EXTERNAL_TRIGGER_CSD), logicalNameBytes);
  }

  private boolean isLogicalNameSchedulerTrigger(final byte[] logicalNameBytes)
      throws ProtocolAdapterException {
    return Arrays.equals(this.getObisBytes(PUSH_SCHEDULER), logicalNameBytes)
        || Arrays.equals(this.getObisBytes(PUSH_SETUP_SCHEDULER), logicalNameBytes);
  }

  private byte[] getObisBytes(final DlmsObjectType type) throws ProtocolAdapterException {
    try {
      final String obis = this.objectConfigService.getCosemObject("DSMR", "4.2.2", type).getObis();
      return new ObisCode(obis).bytes();
    } catch (final ObjectConfigException e) {
      throw new ProtocolAdapterException(
          "Missing " + type.name() + " in object config for DSMR 4.2.2", e);
    }
  }
}
