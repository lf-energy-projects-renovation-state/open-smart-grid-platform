/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.dto.valueobjects.smartmetering;

import java.io.Serializable;

/**
 * request actual reads for E or GAS meters
 *
 * @author dev
 */
public class ActualMeterReadsQuery implements Serializable, ChannelQuery {
    private static final long serialVersionUID = 3751586818507193990L;

    private final Channel channel;

    public ActualMeterReadsQuery() {
        this(null);
    }

    public ActualMeterReadsQuery(final Channel channel) {
        this.channel = channel;
    }

    public boolean isGas() {
        return this.channel != null;
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

}
