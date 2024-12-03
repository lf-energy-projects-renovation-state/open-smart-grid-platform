// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.misc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.application.mapping.ConfigurationMapper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.ObjectConfigServiceHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.DlmsConnectionManager;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.NotSupportedByProtocolException;
import org.opensmartgridplatform.adapter.protocol.dlms.infra.messaging.DlmsMessageListener;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.services.ObjectConfigService;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.CaptureObjectDefinitionDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.DefinableLoadProfileConfigurationDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ObisCodeValuesDto;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;

@ExtendWith(MockitoExtension.class)
class ConfigureDefinableLoadProfileCommandExecutorTest {
  private static final int CLASS_ID = 7;
  private static final int ATTRIBUTE_ID_CAPTURE_OBJECTS = 3;
  private static final int ATTRIBUTE_ID_CAPTURE_PERIOD = 4;
  private static final String OBIS_CODE_PROFILE = "0.1.94.31.6.255";
  private static final String OBIS_CODE_CLOCK = "0.0.1.0.0.255";
  private static final String OBIS_CODE_CAPTURE_OBJ1 = "1.2.3.4.5.6";
  private static final String OBIS_CODE_CAPTURE_OBJ2 = "2.2.3.4.5.6";
  private static final long CAPTURE_PERIOD = 100;

  private ConfigureDefinableLoadProfileCommandExecutor executor;

  @Mock private DlmsMessageListener listener;

  @Mock private DlmsConnectionManager dlmsConnectionManager;

  @Mock private DlmsConnection dlmsConnection;

  @Captor ArgumentCaptor<SetParameter> setParamCaptor;

  final MapperFacade configurationMapper = new ConfigurationMapper();

  private final MessageMetadata messageMetadata =
      MessageMetadata.newBuilder().withCorrelationUid("123456").build();

  @BeforeEach
  public void setUp() throws IOException, ObjectConfigException {
    final ObjectConfigService objectConfigService = new ObjectConfigService();
    final ObjectConfigServiceHelper objectConfigServiceHelper =
        new ObjectConfigServiceHelper(objectConfigService);

    this.executor =
        new ConfigureDefinableLoadProfileCommandExecutor(
            this.configurationMapper, objectConfigServiceHelper);
  }

  @ParameterizedTest
  @EnumSource(
      value = Protocol.class,
      names = {"OTHER_PROTOCOL", "DSMR_2_2"},
      mode = Mode.EXCLUDE)
  void configureLoadProfileForSupportedProtocols(final Protocol protocol) throws Exception {
    final DlmsDevice device = new DlmsDevice();
    device.setProtocol(protocol);
    final DefinableLoadProfileConfigurationDto config = this.createConfig();

    when(this.dlmsConnectionManager.getConnection()).thenReturn(this.dlmsConnection);
    when(this.dlmsConnectionManager.getDlmsMessageListener()).thenReturn(this.listener);
    when(this.dlmsConnection.set(any(SetParameter.class))).thenReturn(AccessResultCode.SUCCESS);

    this.executor.execute(this.dlmsConnectionManager, device, config, this.messageMetadata);

    verify(this.dlmsConnection, times(2)).set(this.setParamCaptor.capture());

    // First call is to set capture objects
    assertThat(this.setParamCaptor.getAllValues().get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            new SetParameter(
                new AttributeAddress(
                    CLASS_ID, new ObisCode(OBIS_CODE_PROFILE), ATTRIBUTE_ID_CAPTURE_OBJECTS),
                this.createExpectedDataObjectsForCaptureObjects()));

    // Second call is to set capture period
    assertThat(this.setParamCaptor.getAllValues().get(1))
        .usingRecursiveComparison()
        .isEqualTo(
            new SetParameter(
                new AttributeAddress(
                    CLASS_ID, new ObisCode(OBIS_CODE_PROFILE), ATTRIBUTE_ID_CAPTURE_PERIOD),
                DataObject.newUInteger32Data(CAPTURE_PERIOD)));
  }

  @Test
  void configureLoadProfileForUnsupportedProtocols() {
    final DlmsDevice device = new DlmsDevice();
    device.setProtocol(Protocol.DSMR_2_2);
    final DefinableLoadProfileConfigurationDto config = this.createConfig();

    assertThrows(
        NotSupportedByProtocolException.class,
        () ->
            this.executor.execute(
                this.dlmsConnectionManager, device, config, this.messageMetadata));
  }

  private DefinableLoadProfileConfigurationDto createConfig() {
    final CaptureObjectDefinitionDto object1 =
        new CaptureObjectDefinitionDto(
            1, new ObisCodeValuesDto(OBIS_CODE_CAPTURE_OBJ1), (byte) 1, 0);
    final CaptureObjectDefinitionDto object2 =
        new CaptureObjectDefinitionDto(
            2, new ObisCodeValuesDto(OBIS_CODE_CAPTURE_OBJ2), (byte) 2, 0);

    return new DefinableLoadProfileConfigurationDto(List.of(object1, object2), CAPTURE_PERIOD);
  }

  private DataObject createExpectedDataObjectsForCaptureObjects() {
    final DataObject clock =
        DataObject.newStructureData(
            DataObject.newUInteger16Data(8),
            DataObject.newOctetStringData(new ObisCode(OBIS_CODE_CLOCK).bytes()),
            DataObject.newInteger8Data((byte) 2),
            DataObject.newUInteger16Data(0));
    final DataObject object1 =
        DataObject.newStructureData(
            DataObject.newUInteger16Data(1),
            DataObject.newOctetStringData(new ObisCode(OBIS_CODE_CAPTURE_OBJ1).bytes()),
            DataObject.newInteger8Data((byte) 1),
            DataObject.newUInteger16Data(0));
    final DataObject object2 =
        DataObject.newStructureData(
            DataObject.newUInteger16Data(2),
            DataObject.newOctetStringData(new ObisCode(OBIS_CODE_CAPTURE_OBJ2).bytes()),
            DataObject.newInteger8Data((byte) 2),
            DataObject.newUInteger16Data(0));

    return DataObject.newArrayData(List.of(clock, object1, object2));
  }
}
