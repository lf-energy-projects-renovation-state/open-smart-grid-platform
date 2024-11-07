// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.alarm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResult;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.application.services.AdhocService;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.ObjectConfigServiceHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.DlmsConnectionManager;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ConnectionException;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.adapter.protocol.dlms.infra.messaging.DlmsMessageListener;
import org.opensmartgridplatform.adapter.protocol.dlms.infra.messaging.LoggingDlmsMessageListener;
import org.opensmartgridplatform.dlms.interfaceclass.InterfaceClass;
import org.opensmartgridplatform.dlms.interfaceclass.attribute.DataAttribute;
import org.opensmartgridplatform.dlms.interfaceclass.attribute.ExtendedRegisterAttribute;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ClearMBusStatusOnAllChannelsRequestDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.MbusChannelShortEquipmentIdentifierDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.MbusShortEquipmentIdentifierDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ScanMbusChannelsResponseDto;
import org.opensmartgridplatform.shared.exceptionhandling.OsgpException;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;

@ExtendWith(MockitoExtension.class)
class ClearMBusStatusOnAllChannelsCommandExecutorTest {

  private static final String OBIS_CODE_TEMPLATE_READ_STATUS = "0.x.24.2.6.255";
  private static final int CLASS_ID_READ_STATUS = 4;

  private static final String OBIS_CODE_TEMPLATE_CLEAR_STATUS = "0.x.94.31.10.255";
  private static final int CLASS_ID_CLEAR_STATUS = 1;

  private static final String OBIS_CODE_TEMPLATE_MBUS_CLIENT_SETUP = "0.x.24.1.0.255";
  private static final int CLASS_ID_MBUS_CLIENT = 72;

  private static final Long STATUS_MASK = 123456L;

  @Mock private ObjectConfigServiceHelper objectConfigServiceHelper;

  @Mock private DlmsConnectionManager connectionManager;

  @Mock private DlmsConnection dlmsConnection;
  @Mock private AdhocService adhocService;

  private DlmsMessageListener dlmsMessageListener;

  @Mock private ClearMBusStatusOnAllChannelsRequestDto dto;

  @Mock private MessageMetadata messageMetadata;

  @Mock private GetResult getResult;

  @Mock private MethodResult methodResult;

  @Captor private ArgumentCaptor<AttributeAddress> attributeAddressArgumentCaptor;

  @Captor private ArgumentCaptor<SetParameter> setParameterArgumentCaptor;

  @Captor private ArgumentCaptor<MethodParameter> methodParameterArgumentCaptor;

  private ClearMBusStatusOnAllChannelsCommandExecutor executor;

  @BeforeEach
  void setup() {
    this.executor =
        new ClearMBusStatusOnAllChannelsCommandExecutor(
            this.objectConfigServiceHelper, this.adhocService);
    this.dlmsMessageListener = new LoggingDlmsMessageListener(null, null);
  }

  @Test
  void testExecuteObjectNotFound() throws OsgpException {
    final DlmsDevice dlmsDevice = new DlmsDevice();
    dlmsDevice.setProtocol("SMR", "5.0.0");
    final MbusShortEquipmentIdentifierDto mbusShortEquipmentIdentifierDto =
        new MbusShortEquipmentIdentifierDto(
            "identificationNumber", "manufacturerIdentification", (short) 1, (short) 1);
    final List<MbusChannelShortEquipmentIdentifierDto> channelShortIds = new ArrayList<>();

    final MbusChannelShortEquipmentIdentifierDto mbusChannelShortEquipmentIdentifierDto =
        new MbusChannelShortEquipmentIdentifierDto((short) 1, mbusShortEquipmentIdentifierDto);
    channelShortIds.add(mbusChannelShortEquipmentIdentifierDto);

    when(this.adhocService.scanMbusChannels(
            this.connectionManager, dlmsDevice, this.messageMetadata))
        .thenReturn(new ScanMbusChannelsResponseDto(channelShortIds));

    when(this.objectConfigServiceHelper.findDefaultAttributeAddress(any(), any(), any(), any()))
        .thenThrow(new ProtocolAdapterException("Object not found"));

    assertThatExceptionOfType(ConnectionException.class)
        .isThrownBy(
            () ->
                this.executor.execute(
                    this.connectionManager, dlmsDevice, this.dto, this.messageMetadata))
        .withMessageContaining("Object not found");
  }

  @Test
  void shouldSkipIfStatusIsZero() throws OsgpException, IOException {
    final DlmsDevice dlmsDevice51 = new DlmsDevice("SMR 5.1 device");
    dlmsDevice51.setProtocol("SMR", "5.1");
    final Protocol protocol = Protocol.forDevice(dlmsDevice51);

    this.setupMocksForChannels(dlmsDevice51, protocol, 4, 0L);

    this.executor.execute(this.connectionManager, dlmsDevice51, this.dto, this.messageMetadata);

    this.assertCurrentStatusAttributeAddresses(
        this.attributeAddressArgumentCaptor.getAllValues(), 4);
    verify(this.dlmsConnection, never()).set(any(SetParameter.class));
    verify(this.dlmsConnection, never()).action(any(MethodParameter.class));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4})
  void shouldExecuteForProtocol_SMR_5_1ForNumberOfChannelsOccupied(final int nrOfOccupiedChannels)
      throws OsgpException, IOException {
    final DlmsDevice dlmsDevice51 = new DlmsDevice("SMR 5.1 device");
    dlmsDevice51.setProtocol("SMR", "5.1");
    final Protocol protocol = Protocol.forDevice(dlmsDevice51);

    this.setupMocksForChannels(dlmsDevice51, protocol, nrOfOccupiedChannels, STATUS_MASK);

    when(this.dlmsConnection.set(this.setParameterArgumentCaptor.capture()))
        .thenReturn(AccessResultCode.SUCCESS);
    when(this.methodResult.getResultCode()).thenReturn(MethodResultCode.SUCCESS);
    when(this.dlmsConnection.action(this.methodParameterArgumentCaptor.capture()))
        .thenReturn(this.methodResult);

    this.executor.execute(this.connectionManager, dlmsDevice51, this.dto, this.messageMetadata);

    this.assertCurrentStatusAttributeAddresses(
        this.attributeAddressArgumentCaptor.getAllValues(), nrOfOccupiedChannels);
    this.assertClearStatus(this.setParameterArgumentCaptor.getAllValues(), nrOfOccupiedChannels);
    this.assertMethodResetAlarm(
        this.methodParameterArgumentCaptor.getAllValues(), nrOfOccupiedChannels);

    verify(this.objectConfigServiceHelper, times(nrOfOccupiedChannels * 3))
        .findDefaultAttributeAddress(any(), any(), any(), any());
  }

  private void setupMocksForChannels(
      final DlmsDevice dlmsDevice,
      final Protocol protocol,
      final int nrOfChannels,
      final long statusMask)
      throws IOException, OsgpException {
    final MbusShortEquipmentIdentifierDto mbusShortEquipmentIdentifierDto =
        new MbusShortEquipmentIdentifierDto(
            "identificationNumber", "manufacturerIdentification", (short) 1, (short) 1);
    final List<MbusChannelShortEquipmentIdentifierDto> channelShortIds = new ArrayList<>();

    for (int channel = 1; channel <= nrOfChannels; channel++) {
      when(this.objectConfigServiceHelper.findDefaultAttributeAddress(
              dlmsDevice, protocol, DlmsObjectType.READ_MBUS_STATUS, channel))
          .thenReturn(
              new AttributeAddress(
                  InterfaceClass.EXTENDED_REGISTER.id(),
                  OBIS_CODE_TEMPLATE_READ_STATUS.replaceAll("x", Integer.toString(channel)),
                  ExtendedRegisterAttribute.VALUE.attributeId()));

      when(this.objectConfigServiceHelper.findDefaultAttributeAddress(
              dlmsDevice, protocol, DlmsObjectType.CLEAR_MBUS_STATUS, channel))
          .thenReturn(
              new AttributeAddress(
                  InterfaceClass.DATA.id(),
                  OBIS_CODE_TEMPLATE_CLEAR_STATUS.replaceAll("x", Integer.toString(channel)),
                  DataAttribute.VALUE.attributeId()));

      when(this.objectConfigServiceHelper.findDefaultAttributeAddress(
              dlmsDevice, protocol, DlmsObjectType.MBUS_CLIENT_SETUP, channel))
          .thenReturn(
              new AttributeAddress(
                  InterfaceClass.MBUS_CLIENT.id(),
                  OBIS_CODE_TEMPLATE_MBUS_CLIENT_SETUP.replaceAll("x", Integer.toString(channel)),
                  DataAttribute.VALUE.attributeId()));

      final MbusChannelShortEquipmentIdentifierDto mbusChannelShortEquipmentIdentifierDto =
          new MbusChannelShortEquipmentIdentifierDto(
              (short) channel, mbusShortEquipmentIdentifierDto);
      channelShortIds.add(mbusChannelShortEquipmentIdentifierDto);
    }

    when(this.adhocService.scanMbusChannels(
            this.connectionManager, dlmsDevice, this.messageMetadata))
        .thenReturn(new ScanMbusChannelsResponseDto(channelShortIds));
    when(this.connectionManager.getDlmsMessageListener()).thenReturn(this.dlmsMessageListener);
    when(this.connectionManager.getConnection()).thenReturn(this.dlmsConnection);
    when(this.dlmsConnection.get(this.attributeAddressArgumentCaptor.capture()))
        .thenReturn(this.getResult);
    when(this.getResult.getResultData()).thenReturn(DataObject.newUInteger32Data(statusMask));
  }

  private void assertMethodResetAlarm(
      final List<MethodParameter> allMethods, final int nrOfOccupiedChannels) {
    for (int i = 1; i <= nrOfOccupiedChannels; i++) {
      final MethodParameter methodParameter = allMethods.get(i - 1);
      assertThat(((Number) methodParameter.getParameter().getValue()).longValue()).isZero();

      assertThat(methodParameter.getInstanceId().asDecimalString())
          .isEqualTo(OBIS_CODE_TEMPLATE_MBUS_CLIENT_SETUP.replaceAll("x", String.valueOf(i)));
      assertThat(methodParameter.getClassId()).isEqualTo(CLASS_ID_MBUS_CLIENT);
    }
  }

  private void assertClearStatus(
      final List<SetParameter> setParameters, final int nrOfOccupiedChannels) {
    for (int i = 1; i <= nrOfOccupiedChannels; i++) {
      final SetParameter setParameter = setParameters.get(i - 1);
      assertThat(((Number) setParameter.getData().getValue()).longValue()).isEqualTo(STATUS_MASK);

      final AttributeAddress attributeAddress = setParameter.getAttributeAddress();
      assertThat(attributeAddress.getInstanceId().asDecimalString())
          .isEqualTo(OBIS_CODE_TEMPLATE_CLEAR_STATUS.replaceAll("x", String.valueOf(i)));
      assertThat(attributeAddress.getClassId()).isEqualTo(CLASS_ID_CLEAR_STATUS);
    }
  }

  private void assertCurrentStatusAttributeAddresses(
      final List<AttributeAddress> attributes, final int nrOfOccupiedChannels) {
    for (int i = 1; i <= nrOfOccupiedChannels; i++) {
      final AttributeAddress attributeAddress = attributes.get(i - 1);
      assertThat(attributeAddress.getInstanceId().asDecimalString())
          .isEqualTo(OBIS_CODE_TEMPLATE_READ_STATUS.replaceAll("x", String.valueOf(i)));
      assertThat(attributeAddress.getClassId()).isEqualTo(CLASS_ID_READ_STATUS);
    }
  }
}
