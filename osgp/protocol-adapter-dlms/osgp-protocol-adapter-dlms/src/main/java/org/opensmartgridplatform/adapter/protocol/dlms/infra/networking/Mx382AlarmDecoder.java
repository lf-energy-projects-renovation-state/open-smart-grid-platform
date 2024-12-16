// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.infra.networking;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.util.encoders.Hex;
import org.opensmartgridplatform.dlms.DlmsPushNotification;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.AlarmTypeDto;
import org.springframework.stereotype.Component;

@Component()
public class Mx382AlarmDecoder extends AlarmDecoder {

  private static final byte EVENT_NOTIFICATION_REQUEST = (byte) 0xC2;
  private static final byte[] WPDU_HEADER = new byte[] {0x00, 0x01, 0x00, 0x67, 0x00, 0x66};
  private static final byte[] AMM_FORWARDED_ALARM_VERSION_0 =
      new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x00};
  static final byte[] CLASS_ID_1 = new byte[] {0x00, 0x01};
  static final byte ATTRIBUTE_ID_2 = 0x02;

  public static final int DATA_TYPE_OCTET_STRING = 0x09;

  public static final String EXPECTED_MESSAGE_TEMPLATE = "Expected a %s (%s), within message: (%s)";

  public DlmsPushNotification decodeMx382alarm(
      final InputStream inputStream, final ConnectionProtocol connectionProtocol)
      throws UnrecognizedMessageDataException {

    if (connectionProtocol == ConnectionProtocol.UDP) {
      return this.decodeMx382alarmUdp(inputStream);
    }
    return this.decodeMx382alarmTcp(inputStream);
  }

  private DlmsPushNotification decodeMx382alarmTcp(final InputStream inputStream)
      throws UnrecognizedMessageDataException {
    final DlmsPushNotification.Builder builder = new DlmsPushNotification.Builder();

    this.handleWpduHeaderBytes(inputStream, "WPDU-header", builder);
    this.checkByte(inputStream, EVENT_NOTIFICATION_REQUEST, "event-notification-request", builder);

    // Datetime is read and skipped
    this.handleDateTime(inputStream);

    this.handleCosemAttributeDescriptor(inputStream, builder);
    // Get equipment identifier and add it to the new push notification
    this.handleEquipmentIdentifier(inputStream, builder);
    // Add alarmbits to the new push notification
    this.addTriggerType(PUSH_ALARM_TRIGGER, builder);
    this.addAlarm(AlarmTypeDto.LAST_GASP, builder);

    return builder.build();
  }

  private DlmsPushNotification decodeMx382alarmUdp(final InputStream inputStream)
      throws UnrecognizedMessageDataException {
    final DlmsPushNotification.Builder builder = new DlmsPushNotification.Builder();

    this.handleWpduHeaderBytes(inputStream, "WPDU-header", builder);
    this.checkByte(inputStream, EVENT_NOTIFICATION_REQUEST, "event-notification-request", builder);

    // Datetime is read and skipped
    this.handleDateTime(inputStream);

    // Get equipment identifier and add it to the new push notification
    this.readEquipmentIdentifierAtEndOfMessage(inputStream, builder);
    // Add alarmbits to the new push notification
    this.addTriggerType(PUSH_SMS_TRIGGER, builder);

    return builder.build();
  }

  private void handleDateTime(final InputStream inputStream)
      throws UnrecognizedMessageDataException {
    // The first byte tells if datetime is provided 0x00 is not provided and 0x09 is provided
    final byte dataType = this.readByte(inputStream);

    // read the rest of the datetime bytes date is provided
    if (dataType == DATA_TYPE_OCTET_STRING) {
      // The next byte of the datetime is the length byte
      final byte lengthOfData = this.readByte(inputStream);
      this.readBytes(inputStream, lengthOfData);
    }
  }

  private void handleEquipmentIdentifier(
      final InputStream inputStream, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {

    final byte dataType = this.readByte(inputStream);
    builder.appendByte(dataType);
    final byte length = this.readByte(inputStream);
    builder.appendByte(length);

    // Read the equipment identifier from incoming mx382 message
    final byte[] equipmentIdentifierBytes = this.readBytes(inputStream, length);
    builder.withEquipmentIdentifier(
        new String(equipmentIdentifierBytes, StandardCharsets.US_ASCII));
  }

  private void readEquipmentIdentifierAtEndOfMessage(
      final InputStream inputStream, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    try {
      this.readBytes(inputStream, inputStream.available() - 17);
      // Read the equipment identifier from incoming mx382 message
      final byte[] equipmentIdentifierBytes = this.readBytes(inputStream, 17);
      final String equipmentIdentifier =
          new String(equipmentIdentifierBytes, StandardCharsets.US_ASCII).trim();
      if (equipmentIdentifier.matches("[a-zA-Z0-9]+")) {
        builder.withEquipmentIdentifier(equipmentIdentifier);
      }
    } catch (final IOException io) {
      throw new UnrecognizedMessageDataException(io.getMessage(), io);
    }
  }

  private void handleCosemAttributeDescriptor(
      final InputStream inputStream, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    // Check bytes of incoming mx382 message
    this.checkBytes(inputStream, CLASS_ID_1, "class-id", builder);
    this.checkBytes(
        inputStream, AMM_FORWARDED_ALARM_VERSION_0, "logical name forwarded alarm", builder);
    this.checkByte(inputStream, ATTRIBUTE_ID_2, "attribute-id", builder);
  }

  private void addTriggerType(
      final String triggerType, final DlmsPushNotification.Builder builder) {
    builder.withTriggerType(triggerType);
  }

  private void addAlarm(final AlarmTypeDto alarmType, final DlmsPushNotification.Builder builder) {
    final Set<AlarmTypeDto> alarmSet = new HashSet<>();
    alarmSet.add(alarmType);
    builder.addAlarms(alarmSet);
  }

  private void checkByte(
      final InputStream inputStream,
      final byte expectedByte,
      final String name,
      final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    final byte readByte = this.readByte(inputStream);
    builder.appendByte(readByte);
    if (readByte != expectedByte) {
      throw new UnrecognizedMessageDataException(
          String.format(
              EXPECTED_MESSAGE_TEMPLATE,
              expectedByte,
              name,
              toHexString(this.resetAndReadAllBytes(inputStream))));
    }
  }

  private void handleWpduHeaderBytes(
      final InputStream inputStream, final String name, final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    final byte[] readBytes = this.readBytes(inputStream, WPDU_HEADER.length);
    builder.appendBytes(readBytes);
    if (!Arrays.equals(readBytes, WPDU_HEADER)) {
      throw new UnrecognizedMessageDataException(
          String.format(
              EXPECTED_MESSAGE_TEMPLATE,
              toHexString(WPDU_HEADER),
              name,
              toHexString(this.resetAndReadAllBytes(inputStream))));
    }

    final byte[] apduLengthBytes = this.readBytes(inputStream, 2);
    builder.appendBytes(apduLengthBytes);
    final int adpuLength = new BigInteger(apduLengthBytes).intValue();
    if (adpuLength != 29 && adpuLength != 30 && adpuLength != 42 && adpuLength != 43) {
      throw new UnrecognizedMessageDataException(
          String.format(
              "Expected length value of 29,30,42 or 43, but found length of %d", adpuLength));
    }
  }

  private void checkBytes(
      final InputStream inputStream,
      final byte[] expectedBytes,
      final String name,
      final DlmsPushNotification.Builder builder)
      throws UnrecognizedMessageDataException {
    final byte[] readBytes = this.readBytes(inputStream, expectedBytes.length);
    builder.appendBytes(readBytes);
    if (!Arrays.equals(readBytes, expectedBytes)) {
      throw new UnrecognizedMessageDataException(
          String.format(
              EXPECTED_MESSAGE_TEMPLATE,
              toHexString(expectedBytes),
              name,
              toHexString(this.resetAndReadAllBytes(inputStream))));
    }
  }

  public static String toHexString(final byte[] ba) {
    final StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    for (final byte b : ba) {
      if (!first) {
        sb.append(", ");
      } else {
        first = false;
      }
      sb.append("0x");
      sb.append(Hex.toHexString(new byte[] {b}));
    }
    sb.append("]");
    return sb.toString();
  }
}
