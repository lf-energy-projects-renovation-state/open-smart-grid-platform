// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.misc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.application.mapping.DataObjectToOutageListConverter;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.testutil.GetResultImpl;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.ObjectConfigServiceHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.DlmsConnectionManager;
import org.opensmartgridplatform.adapter.protocol.dlms.infra.messaging.DlmsMessageListener;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.services.ObjectConfigService;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.GetOutagesRequestDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.OutageDto;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;

@ExtendWith(MockitoExtension.class)
class GetOutagesCommandExecutorTest {
  private static final int CLASS_ID = 7;
  private static final int ATTRIBUTE_ID = 2;
  private static final String OBIS_CODE = "1.0.99.97.0.255";

  private GetOutagesCommandExecutor executor;

  @Mock private DlmsMessageListener listener;

  @Mock private DataObjectToOutageListConverter dataObjectToOutageListConverter;

  @Mock private DlmsConnectionManager dlmsConnectionManager;

  @Mock private DlmsConnection dlmsConnection;

  private final MessageMetadata messageMetadata =
      MessageMetadata.newBuilder().withCorrelationUid("123456").build();

  @BeforeEach
  public void setUp() throws IOException, ObjectConfigException {
    final ObjectConfigService objectConfigService = new ObjectConfigService();
    final ObjectConfigServiceHelper objectConfigServiceHelper =
        new ObjectConfigServiceHelper(objectConfigService);

    this.executor =
        new GetOutagesCommandExecutor(
            this.dataObjectToOutageListConverter, objectConfigServiceHelper);
  }

  @ParameterizedTest
  @EnumSource(
      value = Protocol.class,
      names = {"OTHER_PROTOCOL"},
      mode = Mode.EXCLUDE)
  void returnsFirmwareVersionForSupportedProtocols(final Protocol protocol) throws Exception {
    final DlmsDevice device = new DlmsDevice();
    device.setProtocol(protocol);
    final GetOutagesRequestDto queryDto = new GetOutagesRequestDto();

    final DataObject dataObject = mock(DataObject.class);
    final GetResult getResult = new GetResultImpl(dataObject);
    final OutageDto outage = mock(OutageDto.class);

    when(this.dlmsConnectionManager.getConnection()).thenReturn(this.dlmsConnection);
    when(this.dlmsConnectionManager.getDlmsMessageListener()).thenReturn(this.listener);
    when(this.dlmsConnection.get(refEq(new AttributeAddress(CLASS_ID, OBIS_CODE, ATTRIBUTE_ID))))
        .thenReturn(getResult);
    when(this.dataObjectToOutageListConverter.convert(dataObject)).thenReturn(List.of(outage));

    final List<OutageDto> outages =
        this.executor.execute(this.dlmsConnectionManager, device, queryDto, this.messageMetadata);

    assertThat(outages).hasSize(1);
    assertThat(outages.get(0)).isEqualTo(outage);
  }
}
