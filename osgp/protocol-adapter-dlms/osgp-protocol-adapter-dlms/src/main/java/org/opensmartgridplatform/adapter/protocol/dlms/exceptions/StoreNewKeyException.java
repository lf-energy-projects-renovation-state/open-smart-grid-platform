// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.exceptions;

import java.io.Serial;

public class StoreNewKeyException extends RuntimeException implements NonRetryableException {

  @Serial private static final long serialVersionUID = -539165888074888682L;

  public StoreNewKeyException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public StoreNewKeyException(final String message) {
    super(message);
  }
}
