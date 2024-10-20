// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.exceptions;

import org.opensmartgridplatform.shared.exceptionhandling.ComponentType;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalException;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalExceptionType;
import org.opensmartgridplatform.shared.exceptionhandling.OsgpException;
import org.opensmartgridplatform.shared.exceptionhandling.TechnicalException;
import org.springframework.stereotype.Component;

/**
 * OsgpExceptionConverter
 *
 * <p>Converts given exception to a OsgpException type, and removes cause exceptions from any other
 * type. This is because other layers need to deserialize the exception (and the cause within it)
 * and the Exception class must be known to these layers.
 */
@Component
public class OsgpExceptionConverter {

  /**
   * If the Exception is a OsgpException, this exception is returned.
   *
   * <p>If the Exception is not an OsgpException, only the exception message will be wrapped in an
   * TechnicalException (OsgpException subclass) and returned. This also applies to the cause when
   * it is an OsgpException.
   *
   * @param e The exception.
   * @return OsgpException the given exception or a new TechnicalException instance.
   */
  public OsgpException ensureOsgpOrTechnicalException(final Exception e) {

    final boolean osgpExceptionSupportedByShared =
        !(e instanceof ImageTransferException
            || e instanceof ProtocolAdapterException
            || (e instanceof FunctionalException
                && e.getCause() instanceof ProtocolAdapterException));

    if (e instanceof final OsgpException osgpException && osgpExceptionSupportedByShared) {
      return osgpException;
    }

    if (e instanceof final ConnectionException connectionException) {
      return new FunctionalException(
          connectionException.getType(),
          ComponentType.PROTOCOL_DLMS,
          new OsgpException(ComponentType.PROTOCOL_DLMS, e.getMessage()));
    }

    if (e instanceof NotSupportedByProtocolException) {
      return new FunctionalException(
          FunctionalExceptionType.OPERATION_NOT_SUPPORTED_BY_PLATFORM_FOR_PROTOCOL,
          ComponentType.PROTOCOL_DLMS,
          new OsgpException(ComponentType.PROTOCOL_DLMS, e.getMessage()));
    }

    if (e instanceof final FunctionalException functionalException
        && e.getCause() instanceof final ProtocolAdapterException protocolAdapterException) {
      return new FunctionalException(
          functionalException.getExceptionType(),
          functionalException.getComponentType(),
          new OsgpException(
              protocolAdapterException.getComponentType(), protocolAdapterException.getMessage()));
    }

    return new TechnicalException(
        ComponentType.PROTOCOL_DLMS,
        "Unexpected exception while handling protocol request/response message",
        new OsgpException(ComponentType.PROTOCOL_DLMS, e.getMessage()));
  }
}
