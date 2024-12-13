// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.infra.networking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.opensmartgridplatform.dlms.DlmsPushNotification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DlmsPushNotificationReplayingDecoder
    extends ReplayingDecoder<DlmsPushNotificationReplayingDecoder.DecodingState> {

  private final DlmsPushNotificationDecoder decoder;

  public enum DecodingState {
    EQUIPMENT_IDENTIFIER
  }

  public DlmsPushNotificationReplayingDecoder(final DlmsPushNotificationDecoder decoder) {
    log.debug("Created new DLMS Push Notification replaying decoder");
    this.decoder = decoder;
  }

  /**
   * Decoded the alarm bytes in the buffer. Could be either a MX382, DSMR4 or SMR5 alarm. If there
   * are not enough bytes while decoding, the ReplayingDecoder rewinds and tries the decoding again
   * when there are more bytes received.
   *
   * @param ctx the context from the ReplayingDecoder. Not used in decoding the alarm.
   * @param byteBuf the bytes of the alarm.
   * @param out decoded list of objects
   * @throws UnrecognizedMessageDataException
   */
  @Override
  protected void decode(
      final ChannelHandlerContext ctx, final ByteBuf byteBuf, final List<Object> out)
      throws UnrecognizedMessageDataException {
    final byte[] byteArray = getBytes(byteBuf);
    final DlmsPushNotification dlmsPushNotification =
        this.decoder.decode(byteArray, ConnectionProtocol.TCP);

    log.info("Decoded push notification: {}", dlmsPushNotification);
    out.add(dlmsPushNotification);
  }

  private static byte[] getBytes(final ByteBuf byteBuf) throws UnrecognizedMessageDataException {
    try (final ByteBufInputStream inputStream = new ByteBufInputStream(byteBuf)) {
      return inputStream.readAllBytes();
    } catch (final IOException e) {
      throw new UnrecognizedMessageDataException(e.getMessage(), e);
    }
  }
}
