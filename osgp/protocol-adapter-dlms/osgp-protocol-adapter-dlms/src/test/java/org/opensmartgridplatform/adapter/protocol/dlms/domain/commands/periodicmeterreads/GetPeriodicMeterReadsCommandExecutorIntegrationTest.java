// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.periodicmeterreads;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.CosemDateTime;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.stub.DlmsConnectionManagerStub;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.stub.DlmsConnectionStub;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.testutil.AttributeAddressAssert;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.AmrProfileStatusCodeHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsDateTimeConverter;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.Protocol;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.services.ObjectConfigService;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PeriodTypeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PeriodicMeterReadsRequestDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PeriodicMeterReadsResponseDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PeriodicMeterReadsResponseItemDto;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;

@ExtendWith(MockitoExtension.class)
class GetPeriodicMeterReadsCommandExecutorIntegrationTest {

  private GetPeriodicMeterReadsCommandExecutor executor;

  private DlmsHelper dlmsHelper;

  private DlmsConnectionManagerStub connectionManagerStub;
  private DlmsConnectionStub connectionStub;

  private static final ObisCode OBIS_DAILY_DSMR4 = new ObisCode("1.0.99.2.0.255");
  private static final ObisCode OBIS_INTERVAL_DSMR4 = new ObisCode("1.0.99.1.0.255");
  private static final ObisCode OBIS_MONTHLY_DSMR4 = new ObisCode("0.0.98.1.0.255");

  private static final ObisCode OBIS_DAILY_SMR5 = new ObisCode("1.0.99.2.0.255");
  private static final ObisCode OBIS_INTERVAL_SMR5 = new ObisCode("1.0.99.1.0.255");
  private static final ObisCode OBIS_MONTHLY_SMR5 = new ObisCode("1.0.98.1.0.255");

  private static final ObisCode OBIS_CLOCK = new ObisCode("0.0.1.0.0.255");
  private static final ObisCode OBIS_STATUS = new ObisCode("0.0.96.10.2.255");
  private static final ObisCode OBIS_ACTIVE_ENERGY_IMPORT = new ObisCode("1.0.1.8.0.255");
  private static final ObisCode OBIS_ACTIVE_ENERGY_EXPORT = new ObisCode("1.0.2.8.0.255");
  private static final ObisCode OBIS_ACTIVE_ENERGY_IMPORT_RATE_1 = new ObisCode("1.0.1.8.1.255");
  private static final ObisCode OBIS_ACTIVE_ENERGY_IMPORT_RATE_2 = new ObisCode("1.0.1.8.2.255");
  private static final ObisCode OBIS_ACTIVE_ENERGY_EXPORT_RATE_1 = new ObisCode("1.0.2.8.1.255");
  private static final ObisCode OBIS_ACTIVE_ENERGY_EXPORT_RATE_2 = new ObisCode("1.0.2.8.2.255");
  private static final ObisCode OBIS_MBUS_CHANNEL_1 = new ObisCode("0.1.24.2.1.255");
  private static final ObisCode OBIS_MBUS_CHANNEL_2 = new ObisCode("0.2.24.2.1.255");
  private static final ObisCode OBIS_MBUS_CHANNEL_3 = new ObisCode("0.3.24.2.1.255");
  private static final ObisCode OBIS_MBUS_CHANNEL_4 = new ObisCode("0.4.24.2.1.255");

  private static final int CLASS_ID_CLOCK = 8;
  private static final int CLASS_ID_DATA = 1;
  private static final int CLASS_ID_REGISTER = 3;
  private static final int CLASS_ID_EXTENDED_REGISTER = 4;
  private static final int CLASS_ID_PROFILE = 7;

  private static final byte ATTR_ID_VALUE = 2;
  private static final byte ATTR_ID_BUFFER = 2;
  private static final byte ATTR_ID_SCALER_UNIT = 3;

  private static final DataObject CLOCK =
      DataObject.newStructureData(
          Arrays.asList(
              DataObject.newUInteger16Data(CLASS_ID_CLOCK),
                  DataObject.newOctetStringData(OBIS_CLOCK.bytes()),
              DataObject.newInteger8Data(ATTR_ID_VALUE), DataObject.newUInteger16Data(0)));

  private static final DataObject STATUS =
      DataObject.newStructureData(
          Arrays.asList(
              DataObject.newUInteger16Data(CLASS_ID_DATA),
                  DataObject.newOctetStringData(OBIS_STATUS.bytes()),
              DataObject.newInteger8Data(ATTR_ID_VALUE), DataObject.newUInteger16Data(0)));

  private static final DataObject ACTIVE_ENERGY_IMPORT_RATE_1 =
      DataObject.newStructureData(
          Arrays.asList(
              DataObject.newUInteger16Data(CLASS_ID_REGISTER),
              DataObject.newOctetStringData(OBIS_ACTIVE_ENERGY_IMPORT_RATE_1.bytes()),
              DataObject.newInteger8Data(ATTR_ID_VALUE),
              DataObject.newUInteger16Data(0)));

  private static final DataObject ACTIVE_ENERGY_IMPORT_RATE_2 =
      DataObject.newStructureData(
          Arrays.asList(
              DataObject.newUInteger16Data(CLASS_ID_REGISTER),
              DataObject.newOctetStringData(OBIS_ACTIVE_ENERGY_IMPORT_RATE_2.bytes()),
              DataObject.newInteger8Data(ATTR_ID_VALUE),
              DataObject.newUInteger16Data(0)));

  private static final DataObject ACTIVE_ENERGY_EXPORT_RATE_1 =
      DataObject.newStructureData(
          Arrays.asList(
              DataObject.newUInteger16Data(CLASS_ID_REGISTER),
              DataObject.newOctetStringData(OBIS_ACTIVE_ENERGY_EXPORT_RATE_1.bytes()),
              DataObject.newInteger8Data(ATTR_ID_VALUE),
              DataObject.newUInteger16Data(0)));

  private static final DataObject ACTIVE_ENERGY_EXPORT_RATE_2 =
      DataObject.newStructureData(
          Arrays.asList(
              DataObject.newUInteger16Data(CLASS_ID_REGISTER),
              DataObject.newOctetStringData(OBIS_ACTIVE_ENERGY_EXPORT_RATE_2.bytes()),
              DataObject.newInteger8Data(ATTR_ID_VALUE),
              DataObject.newUInteger16Data(0)));

  private Date timeFrom;
  private Date timeTo;
  private DataObject period1Clock;
  private DataObject period2Clock;
  private Date period1ClockValue;
  private Date period2ClockValue;
  private Date period2ClockValueNullDataPeriod15Min;
  private Date period2ClockValueNullDataPeriodDaily;
  private Date period2ClockValueNullDataPeriodMonthly;

  private static final CosemDateTime PERIOD_1_CAPTURE_TIME =
      new CosemDateTime(2018, 12, 31, 23, 50, 0, 0);
  private static final CosemDateTime PERIOD_2_CAPTURE_TIME =
      new CosemDateTime(2019, 1, 1, 0, 7, 0, 0);

  private static final int AMOUNT_OF_PERIODS = 2;

  private static final short PERIOD1_AMR_STATUS_VALUE = 0x0F; // First 4 status bits set
  private static final short PERIOD2_AMR_STATUS_VALUE = 0xF0; // Last 4 status bits set
  private static final long PERIOD_1_E_VALUE_1 = 1000L;
  private static final long PERIOD_1_E_VALUE_2 = 2000L;
  private static final long PERIOD_1_E_VALUE_3 = 3000L;
  private static final long PERIOD_1_E_VALUE_4 = 4000L;
  private static final long PERIOD_1_G_VALUE = 5000L;
  private static final long PERIOD_2_E_VALUE_1 = 1500L;
  private static final long PERIOD_2_E_VALUE_2 = 2500L;
  private static final long PERIOD_2_E_VALUE_3 = 3500L;
  private static final long PERIOD_2_E_VALUE_4 = 4500L;
  private static final long PERIOD_2_G_VALUE = 5500L;

  private final List<Protocol> protocolsNoStatusMonthlyValues =
      List.of(Protocol.DSMR_2_2, Protocol.DSMR_4_2_2, Protocol.SMR_4_3);

  private static final List<Integer> ALL_G_CHANNELS = List.of(1, 2, 3, 4);

  private static final int DLMS_ENUM_VALUE_WH = 30;

  @BeforeEach
  public void setUp() throws IOException, ObjectConfigException {

    final TimeZone defaultTimeZone = TimeZone.getDefault();
    final DateTimeZone defaultDateTimeZone = DateTimeZone.getDefault();

    // all time based tests must use UTC time.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    DateTimeZone.setDefault(DateTimeZone.UTC);

    this.initDates();

    this.dlmsHelper = new DlmsHelper();
    final AmrProfileStatusCodeHelper amrProfileStatusCodeHelper = new AmrProfileStatusCodeHelper();
    final ObjectConfigService objectConfigService = new ObjectConfigService();

    this.executor =
        new GetPeriodicMeterReadsCommandExecutor(
            this.dlmsHelper, amrProfileStatusCodeHelper, objectConfigService);

    this.connectionStub = new DlmsConnectionStub();
    this.connectionManagerStub = new DlmsConnectionManagerStub(this.connectionStub);

    this.connectionStub.setDefaultReturnValue(DataObject.newArrayData(List.of()));

    // reset to original TimeZone
    TimeZone.setDefault(defaultTimeZone);
    DateTimeZone.setDefault(defaultDateTimeZone);
  }

  private void initDates() {
    this.timeFrom = new GregorianCalendar(2019, Calendar.JANUARY, 1).getTime();
    this.timeTo = new GregorianCalendar(2019, Calendar.FEBRUARY, 2).getTime();
    this.period1Clock = this.getDateAsOctetString(2019, 1, 1);
    this.period2Clock = this.getDateAsOctetString(2019, 1, 2);
    this.period1ClockValue = new GregorianCalendar(2019, Calendar.JANUARY, 1, 0, 0).getTime();
    this.period2ClockValue = new GregorianCalendar(2019, Calendar.JANUARY, 2, 0, 0).getTime();
    this.period2ClockValueNullDataPeriod15Min =
        new GregorianCalendar(2019, Calendar.JANUARY, 1, 0, 15).getTime();
    this.period2ClockValueNullDataPeriodDaily =
        new GregorianCalendar(2019, Calendar.JANUARY, 2, 0, 0).getTime();
    this.period2ClockValueNullDataPeriodMonthly =
        new GregorianCalendar(2019, Calendar.FEBRUARY, 1, 0, 0).getTime();
  }

  private static Stream<Arguments> combinationsDsmr() {
    final List<Protocol> dsmrProtocols =
        Arrays.stream(Protocol.values())
            .filter(protocol -> protocol.isDsmr4() || protocol.isDsmr2())
            .toList();

    return generateCombinations(dsmrProtocols);
  }

  private static Stream<Arguments> combinationsSmr5() {
    final List<Protocol> smr5Protocols =
        Arrays.stream(Protocol.values()).filter(Protocol::isSmr5).toList();

    return generateCombinations(smr5Protocols);
  }

  private static Stream<Arguments> generateCombinations(final List<Protocol> protocols) {
    final List<Arguments> arguments = new ArrayList<>();

    for (final Protocol protocol : protocols) {
      for (final PeriodTypeDto periodType : PeriodTypeDto.values()) {
        arguments.add(Arguments.of(protocol, periodType));
      }
    }

    return arguments.stream();
  }

  @ParameterizedTest
  @MethodSource("combinationsDsmr")
  void testExecuteDsmrNoSelectedValues(final Protocol protocol, final PeriodTypeDto type)
      throws Exception {
    this.testExecute(protocol, type, false, false);
  }

  @ParameterizedTest
  @MethodSource("combinationsDsmr")
  void testExecuteDsmr(final Protocol protocol, final PeriodTypeDto type) throws Exception {
    this.testExecute(protocol, type, false, true);
  }

  @ParameterizedTest
  @MethodSource("combinationsSmr5")
  void testExecuteSmr5(final Protocol protocol, final PeriodTypeDto type) throws Exception {
    this.testExecute(protocol, type, false, true);
  }

  @ParameterizedTest
  @MethodSource("combinationsSmr5")
  void testExecuteSmr5_WithNullData(final Protocol protocol, final PeriodTypeDto type)
      throws Exception {
    this.testExecute(protocol, type, true, true);
  }

  private void testExecute(
      final Protocol protocol,
      final PeriodTypeDto type,
      final boolean useNullData,
      final boolean selectiveAccessPeriodicMeterReadsSupported)
      throws Exception {

    // SETUP
    final MessageMetadata messageMetadata =
        MessageMetadata.newBuilder().withCorrelationUid("123456").build();

    // Reset stub
    this.connectionStub.clearRequestedAttributeAddresses();

    // Create device with requested protocol version
    final DlmsDevice device =
        this.createDlmsDevice(protocol, selectiveAccessPeriodicMeterReadsSupported);

    // Create request object
    final PeriodicMeterReadsRequestDto request =
        new PeriodicMeterReadsRequestDto(type, this.timeFrom, this.timeTo);

    // Get expected values
    final AttributeAddress expectedAddressProfile =
        this.createAttributeAddress(protocol, type, this.timeFrom, this.timeTo, device);
    final List<AttributeAddress> expectedScalerUnitAddresses =
        this.getScalerUnitAttributeAddresses(type, selectiveAccessPeriodicMeterReadsSupported);

    // Set response in stub
    this.setResponseForProfile(
        expectedAddressProfile,
        protocol,
        type,
        useNullData,
        selectiveAccessPeriodicMeterReadsSupported);
    this.setResponsesForScalerUnit(expectedScalerUnitAddresses);

    // CALL
    final PeriodicMeterReadsResponseDto response =
        this.executor.execute(this.connectionManagerStub, device, request, messageMetadata);

    // VERIFY

    // Get resulting requests from connection stub
    final List<AttributeAddress> requestedAttributeAddresses =
        this.connectionStub.getRequestedAttributeAddresses();
    assertThat(requestedAttributeAddresses).hasSize(1);

    // There should be 1 request to the buffer (id = 2) of a profile (class-id = 7)
    final AttributeAddress actualAttributeAddressProfile =
        requestedAttributeAddresses.stream()
            .filter(a -> a.getClassId() == this.CLASS_ID_PROFILE)
            .toList()
            .get(0);

    AttributeAddressAssert.is(actualAttributeAddressProfile, expectedAddressProfile);

    // Check response
    assertThat(response.getPeriodType()).isEqualTo(type);
    final List<PeriodicMeterReadsResponseItemDto> periodicMeterReads =
        response.getPeriodicMeterReads();
    assertThat(periodicMeterReads).hasSize(AMOUNT_OF_PERIODS);

    this.checkClockValues(periodicMeterReads, type, useNullData);
    this.checkValues(periodicMeterReads, type);
  }

  private DlmsDevice createDlmsDevice(
      final Protocol protocol, final boolean selectiveAccessPeriodicMeterReadsSupported) {
    final DlmsDevice device = new DlmsDevice();
    device.setProtocol(protocol);
    device.setSelectiveAccessSupported(true);
    device.setSelectiveAccessPeriodicMeterReadsSupported(
        selectiveAccessPeriodicMeterReadsSupported);
    return device;
  }

  private AttributeAddress createAttributeAddress(
      final Protocol protocol,
      final PeriodTypeDto type,
      final Date timeFrom,
      final Date timeTo,
      final DlmsDevice device)
      throws Exception {

    final DataObject from =
        this.dlmsHelper.asDataObject(
            DlmsDateTimeConverter.toDateTime(timeFrom, device.getTimezone()));
    final DataObject to =
        this.dlmsHelper.asDataObject(
            DlmsDateTimeConverter.toDateTime(timeTo, device.getTimezone()));

    if (protocol.isDsmr2() || protocol.isDsmr4()) {
      if (type == PeriodTypeDto.DAILY) {
        return this.createAttributeAddressDsmr4Daily(
            from, to, device.isSelectiveAccessPeriodicMeterReadsSupported());
      } else if (type == PeriodTypeDto.MONTHLY) {
        return this.createAttributeAddressDsmr4Monthly(
            from, to, device.isSelectiveAccessPeriodicMeterReadsSupported());
      } else if (type == PeriodTypeDto.INTERVAL) {
        return this.createAttributeAddressDsmr4Interval(from, to);
      }
    } else if (protocol.isSmr5()) {
      if (type == PeriodTypeDto.DAILY) {
        return this.createAttributeAddressSmr5Daily(from, to);
      } else if (type == PeriodTypeDto.MONTHLY) {
        return this.createAttributeAddressSmr5Monthly(from, to);
      } else if (type == PeriodTypeDto.INTERVAL) {
        return this.createAttributeAddressSmr5Interval(from, to);
      }
    }

    throw new Exception(
        "Invalid combination of protocol "
            + protocol.getName()
            + " and version "
            + protocol.getVersion());
  }

  private List<AttributeAddress> getScalerUnitAttributeAddresses(
      final PeriodTypeDto type, final boolean selectedValuesSupported) throws Exception {
    final List<AttributeAddress> attributeAddresses = new ArrayList<>();

    switch (type) {
      case MONTHLY, DAILY:
        attributeAddresses.add(
            new AttributeAddress(
                CLASS_ID_REGISTER, OBIS_ACTIVE_ENERGY_IMPORT_RATE_1, ATTR_ID_SCALER_UNIT, null));
        attributeAddresses.add(
            new AttributeAddress(
                CLASS_ID_REGISTER, OBIS_ACTIVE_ENERGY_IMPORT_RATE_2, ATTR_ID_SCALER_UNIT, null));
        attributeAddresses.add(
            new AttributeAddress(
                CLASS_ID_REGISTER, OBIS_ACTIVE_ENERGY_EXPORT_RATE_1, ATTR_ID_SCALER_UNIT, null));
        attributeAddresses.add(
            new AttributeAddress(
                CLASS_ID_REGISTER, OBIS_ACTIVE_ENERGY_EXPORT_RATE_2, ATTR_ID_SCALER_UNIT, null));
        if (!selectedValuesSupported) {
          attributeAddresses.add(
              new AttributeAddress(
                  CLASS_ID_EXTENDED_REGISTER, OBIS_MBUS_CHANNEL_1, ATTR_ID_SCALER_UNIT, null));
          attributeAddresses.add(
              new AttributeAddress(
                  CLASS_ID_EXTENDED_REGISTER, OBIS_MBUS_CHANNEL_2, ATTR_ID_SCALER_UNIT, null));
          attributeAddresses.add(
              new AttributeAddress(
                  CLASS_ID_EXTENDED_REGISTER, OBIS_MBUS_CHANNEL_3, ATTR_ID_SCALER_UNIT, null));
          attributeAddresses.add(
              new AttributeAddress(
                  CLASS_ID_EXTENDED_REGISTER, OBIS_MBUS_CHANNEL_4, ATTR_ID_SCALER_UNIT, null));
        }
        break;
      case INTERVAL:
        attributeAddresses.add(
            new AttributeAddress(
                CLASS_ID_REGISTER, OBIS_ACTIVE_ENERGY_IMPORT, ATTR_ID_SCALER_UNIT, null));
        attributeAddresses.add(
            new AttributeAddress(
                CLASS_ID_REGISTER, OBIS_ACTIVE_ENERGY_EXPORT, ATTR_ID_SCALER_UNIT, null));
        break;
      default:
        throw new Exception("Unexpected period type " + type);
    }
    return attributeAddresses;
  }

  private void setResponseForProfile(
      final AttributeAddress attributeAddressForProfile,
      final Protocol protocol,
      final PeriodTypeDto type,
      final boolean useNullData,
      final boolean selectedValuesSupported) {

    // PERIOD 1

    final DataObject period1CaptureTime = DataObject.newDateTimeData(this.PERIOD_1_CAPTURE_TIME);

    final DataObject periodItem1 =
        this.createPeriodItem(
            type,
            protocol,
            selectedValuesSupported,
            this.period1Clock,
            PERIOD1_AMR_STATUS_VALUE,
            PERIOD_1_E_VALUE_1,
            PERIOD_1_E_VALUE_2,
            PERIOD_1_E_VALUE_3,
            PERIOD_1_E_VALUE_4,
            PERIOD_1_G_VALUE,
            period1CaptureTime);

    // PERIOD 2

    final DataObject period2ClockOrNull;
    final DataObject period2CaptureTime;
    if (useNullData) {
      period2ClockOrNull = DataObject.newNullData();
      period2CaptureTime = DataObject.newNullData();
    } else {
      period2ClockOrNull = this.period2Clock;
      period2CaptureTime = DataObject.newDateTimeData(this.PERIOD_2_CAPTURE_TIME);
    }

    final DataObject periodItem2 =
        this.createPeriodItem(
            type,
            protocol,
            selectedValuesSupported,
            period2ClockOrNull,
            this.PERIOD2_AMR_STATUS_VALUE,
            PERIOD_2_E_VALUE_1,
            PERIOD_2_E_VALUE_2,
            PERIOD_2_E_VALUE_3,
            PERIOD_2_E_VALUE_4,
            PERIOD_2_G_VALUE,
            period2CaptureTime);

    // Create returnvalue and set in stub
    final DataObject responseDataObject =
        DataObject.newArrayData(Arrays.asList(periodItem1, periodItem2));
    this.connectionStub.addReturnValue(attributeAddressForProfile, responseDataObject);
  }

  private DataObject createPeriodItem(
      final PeriodTypeDto type,
      final Protocol protocol,
      final boolean selectedValuesSupported,
      final DataObject clock,
      final short statusValue,
      final long longValueE1,
      final long longValueE2,
      final long longValueE3,
      final long longValueE4,
      final long longValueG,
      final DataObject captureTime) {
    final DataObject periodStatus = DataObject.newUInteger8Data(statusValue);
    final DataObject periodValueE1 = DataObject.newUInteger32Data(longValueE1);
    final DataObject periodValueE2 = DataObject.newUInteger32Data(longValueE2);
    final DataObject periodValueE3 = DataObject.newUInteger32Data(longValueE3);
    final DataObject periodValueE4 = DataObject.newUInteger32Data(longValueE4);

    final List<DataObject> items = new ArrayList<>();

    // Overview protocols - periodtypes (Interval/Daily/Monthly) - selected values supported
    //
    //               DSMR2.2  DSMR4.2.2 / SMR4.3  SMR5.0-5.5
    //                I D M          I D M          I D M
    //
    // Clock          1 1 1          1 1 1          1 1 1
    // Status         1 1 0          1 1 0          1 1 1
    // E values       2 4 4          2 4 4          2 4 4
    // G values       0 0 0          0 0 0          0 0 0
    // Capture time   0 0 0          0 0 0          0 0 0

    // Overview protocols - periodtypes - selected values NOT supported
    //
    //               DSMR2.2  DSMR4.2.2 / SMR4.3
    //                I D M          I D M
    //
    // Clock          1 1 1          1 1 1
    // Status         1 1 0          1 1 0
    // E values       2 4 4          2 4 4
    // G values       0 1 1          0 1 1
    // Capture time   0 0 0          0 1 1

    // Always add clock first
    items.add(clock);

    // Add status
    if (type != PeriodTypeDto.MONTHLY || !this.protocolsNoStatusMonthlyValues.contains(protocol)) {
      items.add(periodStatus);
    }

    if (type == PeriodTypeDto.INTERVAL) {
      // Add E values (import total, export total)
      items.addAll(List.of(periodValueE1, periodValueE2));
    } else {
      // Add E values (import rate 1 and 2, export rate 1 and 2)
      items.addAll(List.of(periodValueE1, periodValueE2, periodValueE3, periodValueE4));
    }

    // Add G values and capture times
    if (!selectedValuesSupported && type != PeriodTypeDto.INTERVAL) {
      for (final int c : this.ALL_G_CHANNELS) {
        // Make each value different by adding channel number
        items.add(DataObject.newUInteger32Data(longValueG + c));
        if (protocol != Protocol.DSMR_2_2) {
          items.add(captureTime);
        }
      }
    }

    return DataObject.newStructureData(items);
  }

  private void setResponsesForScalerUnit(
      final List<AttributeAddress> attributeAddressesForScalerUnit) {
    final DataObject responseDataObject =
        DataObject.newStructureData(
            DataObject.newInteger8Data((byte) 0), DataObject.newEnumerateData(DLMS_ENUM_VALUE_WH));

    for (final AttributeAddress attributeAddress : attributeAddressesForScalerUnit) {
      this.connectionStub.addReturnValue(attributeAddress, responseDataObject);
    }
  }

  private DataObject getDateAsOctetString(final int year, final int month, final int day) {
    final CosemDateTime dateTime = new CosemDateTime(year, month, day, 0, 0, 0, 0);

    return DataObject.newOctetStringData(dateTime.encode());
  }

  private void checkClockValues(
      final List<PeriodicMeterReadsResponseItemDto> periodicMeterReads,
      final PeriodTypeDto type,
      final boolean useNullData) {

    final PeriodicMeterReadsResponseItemDto periodicMeterRead1 = periodicMeterReads.get(0);

    assertThat(periodicMeterRead1.getLogTime()).isEqualTo(this.period1ClockValue);

    final PeriodicMeterReadsResponseItemDto periodicMeterRead2 = periodicMeterReads.get(1);

    if (!useNullData) { // The timestamps should be the same as the times
      // set in the test
      assertThat(periodicMeterRead2.getLogTime()).isEqualTo(this.period2ClockValue);
    } else { // The timestamps should be calculated using the periodType,
      // starting from the time of period 1
      if (type == PeriodTypeDto.INTERVAL) {
        assertThat(periodicMeterRead2.getLogTime())
            .isEqualTo(this.period2ClockValueNullDataPeriod15Min);
      } else if (type == PeriodTypeDto.DAILY) {
        assertThat(periodicMeterRead2.getLogTime())
            .isEqualTo(this.period2ClockValueNullDataPeriodDaily);
      } else if (type == PeriodTypeDto.MONTHLY) {
        assertThat(periodicMeterRead2.getLogTime())
            .isEqualTo(this.period2ClockValueNullDataPeriodMonthly);
      }
    }
  }

  private void checkValues(
      final List<PeriodicMeterReadsResponseItemDto> periodicMeterReads, final PeriodTypeDto type) {

    final PeriodicMeterReadsResponseItemDto period1 = periodicMeterReads.get(0);
    final PeriodicMeterReadsResponseItemDto period2 = periodicMeterReads.get(1);

    if (type == PeriodTypeDto.MONTHLY || type == PeriodTypeDto.DAILY) {
      assertThat(period1.getActiveEnergyImportTariffOne().getValue().longValue())
          .isEqualTo(this.PERIOD_1_E_VALUE_1);
      assertThat(period1.getActiveEnergyImportTariffTwo().getValue().longValue())
          .isEqualTo(this.PERIOD_1_E_VALUE_2);
      assertThat(period1.getActiveEnergyExportTariffOne().getValue().longValue())
          .isEqualTo(this.PERIOD_1_E_VALUE_3);
      assertThat(period1.getActiveEnergyExportTariffTwo().getValue().longValue())
          .isEqualTo(this.PERIOD_1_E_VALUE_4);
      assertThat(period2.getActiveEnergyImportTariffOne().getValue().longValue())
          .isEqualTo(this.PERIOD_2_E_VALUE_1);
      assertThat(period2.getActiveEnergyImportTariffTwo().getValue().longValue())
          .isEqualTo(this.PERIOD_2_E_VALUE_2);
      assertThat(period2.getActiveEnergyExportTariffOne().getValue().longValue())
          .isEqualTo(this.PERIOD_2_E_VALUE_3);
      assertThat(period2.getActiveEnergyExportTariffTwo().getValue().longValue())
          .isEqualTo(this.PERIOD_2_E_VALUE_4);
    } else { // INTERVAL, only total values
      assertThat(period1.getActiveEnergyImport().getValue().longValue())
          .isEqualTo(this.PERIOD_1_E_VALUE_1);
      assertThat(period1.getActiveEnergyExport().getValue().longValue())
          .isEqualTo(this.PERIOD_1_E_VALUE_2);
      assertThat(period2.getActiveEnergyImport().getValue().longValue())
          .isEqualTo(this.PERIOD_2_E_VALUE_1);
      assertThat(period2.getActiveEnergyExport().getValue().longValue())
          .isEqualTo(this.PERIOD_2_E_VALUE_2);
    }
  }

  private SelectiveAccessDescription createSelectiveAccessDescription(
      final DataObject from, final DataObject to) {

    final DataObject selectedValues = DataObject.newArrayData(List.of());

    final DataObject expectedAccessParam =
        DataObject.newStructureData(Arrays.asList(this.CLOCK, from, to, selectedValues));

    return new SelectiveAccessDescription(1, expectedAccessParam);
  }

  // DSMR4

  private AttributeAddress createAttributeAddressDsmr4Daily(
      final DataObject from,
      final DataObject to,
      final boolean selectiveAccessPeriodicMeterReadsSupported) {
    final SelectiveAccessDescription expectedSelectiveAccess =
        this.createSelectiveAccessDescriptionDsmr4Daily(
            from, to, selectiveAccessPeriodicMeterReadsSupported);
    return new AttributeAddress(
        this.CLASS_ID_PROFILE, this.OBIS_DAILY_DSMR4, this.ATTR_ID_BUFFER, expectedSelectiveAccess);
  }

  private SelectiveAccessDescription createSelectiveAccessDescriptionDsmr4Daily(
      final DataObject from,
      final DataObject to,
      final boolean selectiveAccessPeriodicMeterReadsSupported) {

    final List<DataObject> dataObjects =
        selectiveAccessPeriodicMeterReadsSupported
            ? Arrays.asList(
                this.CLOCK,
                this.STATUS,
                this.ACTIVE_ENERGY_IMPORT_RATE_1,
                this.ACTIVE_ENERGY_IMPORT_RATE_2,
                this.ACTIVE_ENERGY_EXPORT_RATE_1,
                this.ACTIVE_ENERGY_EXPORT_RATE_2)
            : new ArrayList<>();

    final DataObject selectedValues = DataObject.newArrayData(dataObjects);

    final DataObject expectedAccessParam =
        DataObject.newStructureData(Arrays.asList(this.CLOCK, from, to, selectedValues));

    return new SelectiveAccessDescription(1, expectedAccessParam);
  }

  private AttributeAddress createAttributeAddressDsmr4Monthly(
      final DataObject from,
      final DataObject to,
      final boolean selectiveAccessPeriodicMeterReadsSupported) {
    final SelectiveAccessDescription expectedSelectiveAccess =
        this.createSelectiveAccessDescriptionDsmr4Monthly(
            from, to, selectiveAccessPeriodicMeterReadsSupported);
    return new AttributeAddress(
        this.CLASS_ID_PROFILE,
        this.OBIS_MONTHLY_DSMR4,
        this.ATTR_ID_BUFFER,
        expectedSelectiveAccess);
  }

  private SelectiveAccessDescription createSelectiveAccessDescriptionDsmr4Monthly(
      final DataObject from,
      final DataObject to,
      final boolean selectiveAccessPeriodicMeterReadsSupported) {

    final List<DataObject> dataObjects =
        selectiveAccessPeriodicMeterReadsSupported
            ? Arrays.asList(
                this.CLOCK,
                this.ACTIVE_ENERGY_IMPORT_RATE_1,
                this.ACTIVE_ENERGY_IMPORT_RATE_2,
                this.ACTIVE_ENERGY_EXPORT_RATE_1,
                this.ACTIVE_ENERGY_EXPORT_RATE_2)
            : new ArrayList<>();

    final DataObject selectedValues = DataObject.newArrayData(dataObjects);

    final DataObject expectedAccessParam =
        DataObject.newStructureData(Arrays.asList(this.CLOCK, from, to, selectedValues));

    return new SelectiveAccessDescription(1, expectedAccessParam);
  }

  private AttributeAddress createAttributeAddressDsmr4Interval(
      final DataObject from, final DataObject to) {
    final SelectiveAccessDescription expectedSelectiveAccess =
        this.createSelectiveAccessDescription(from, to);
    return new AttributeAddress(
        this.CLASS_ID_PROFILE,
        this.OBIS_INTERVAL_DSMR4,
        this.ATTR_ID_BUFFER,
        expectedSelectiveAccess);
  }

  // SMR5

  private AttributeAddress createAttributeAddressSmr5Daily(
      final DataObject from, final DataObject to) {
    final SelectiveAccessDescription expectedSelectiveAccess =
        this.createSelectiveAccessDescription(from, to);
    return new AttributeAddress(
        this.CLASS_ID_PROFILE, this.OBIS_DAILY_SMR5, this.ATTR_ID_BUFFER, expectedSelectiveAccess);
  }

  private AttributeAddress createAttributeAddressSmr5Monthly(
      final DataObject from, final DataObject to) {
    final SelectiveAccessDescription expectedSelectiveAccess =
        this.createSelectiveAccessDescription(from, to);
    return new AttributeAddress(
        this.CLASS_ID_PROFILE,
        this.OBIS_MONTHLY_SMR5,
        this.ATTR_ID_BUFFER,
        expectedSelectiveAccess);
  }

  private AttributeAddress createAttributeAddressSmr5Interval(
      final DataObject from, final DataObject to) {
    final SelectiveAccessDescription expectedSelectiveAccess =
        this.createSelectiveAccessDescription(from, to);
    return new AttributeAddress(
        this.CLASS_ID_PROFILE,
        this.OBIS_INTERVAL_SMR5,
        this.ATTR_ID_BUFFER,
        expectedSelectiveAccess);
  }
}
