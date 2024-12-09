// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.periodicmeterreads;

import static org.opensmartgridplatform.dlms.interfaceclass.attribute.ProfileGenericAttribute.BUFFER;
import static org.opensmartgridplatform.dlms.interfaceclass.attribute.ProfileGenericAttribute.CAPTURE_PERIOD;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.DAILY_VALUES_COMBINED;
import static org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType.MONTHLY_VALUES_COMBINED;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.AbstractCommandExecutor;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.model.Medium;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.model.ProfileCaptureTime;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.AmrProfileStatusCodeHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsDateTimeConverter;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.JdlmsObjectToStringUtil;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.DlmsConnectionManager;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.valueobjects.CombinedDeviceModelCode;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.BufferedDateTimeValidationException;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.interfaceclass.attribute.ExtendedRegisterAttribute;
import org.opensmartgridplatform.dlms.objectconfig.Attribute;
import org.opensmartgridplatform.dlms.objectconfig.CaptureObject;
import org.opensmartgridplatform.dlms.objectconfig.CosemObject;
import org.opensmartgridplatform.dlms.objectconfig.dlmsclasses.ProfileGeneric;
import org.opensmartgridplatform.dlms.objectconfig.dlmsclasses.Register;
import org.opensmartgridplatform.dlms.services.ObjectConfigService;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.AmrProfileStatusCodeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.AmrProfileStatusCodeFlagDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.CosemDateTimeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.MeterReadsResponseWithLogTimeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PeriodTypeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PeriodicMeterReadsRequestDataDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.PeriodicMeterReadsRequestDto;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;

@Slf4j
public abstract class AbstractPeriodicMeterReadsCommandExecutor<T, R>
    extends AbstractCommandExecutor<T, R> {

  private final PeriodicMeterReadsConfig config;

  private final AmrProfileStatusCodeHelper amrProfileStatusCodeHelper;
  private final DlmsHelper dlmsHelper;
  private final ObjectConfigService objectConfigService;

  AbstractPeriodicMeterReadsCommandExecutor(
      final Class<? extends PeriodicMeterReadsRequestDataDto> clazz,
      final AmrProfileStatusCodeHelper amrProfileStatusCodeHelper,
      final DlmsHelper dlmsHelper,
      final ObjectConfigService objectConfigService,
      final PeriodicMeterReadsConfig config) {
    super(clazz);
    this.amrProfileStatusCodeHelper = amrProfileStatusCodeHelper;
    this.dlmsHelper = dlmsHelper;
    this.objectConfigService = objectConfigService;
    this.config = config;
  }

  abstract MeterReadsResponseWithLogTimeDto convertToResponseItem(
      final PeriodTypeDto periodType,
      final List<CaptureObject> selectedObjects,
      final ProfileCaptureTime intervalTime,
      final List<DataObject> bufferedObjects,
      final int channel,
      final List<MeterReadsResponseWithLogTimeDto> periodicMeterReads)
      throws ProtocolAdapterException, BufferedDateTimeValidationException;

  protected List<MeterReadsResponseWithLogTimeDto> getPeriodicMeterReads(
      final DlmsConnectionManager conn,
      final DlmsDevice device,
      final PeriodicMeterReadsRequestDto periodicMeterReadsQuery,
      final MessageMetadata messageMetadata)
      throws ProtocolAdapterException {

    final CombinedDeviceModelCode combinedDeviceModelCode =
        CombinedDeviceModelCode.parse(messageMetadata.getDeviceModelCode());

    if (periodicMeterReadsQuery == null) {
      throw new IllegalArgumentException(
          "PeriodicMeterReadsQuery should contain PeriodType, BeginDate and EndDate.");
    }

    final DateTime from =
        DlmsDateTimeConverter.toDateTime(
            periodicMeterReadsQuery.getBeginDate(), device.getTimezone());
    final DateTime to =
        DlmsDateTimeConverter.toDateTime(
            periodicMeterReadsQuery.getEndDate(), device.getTimezone());

    // The query can be for 3 different types of values: Interval, daily or monthly.
    final PeriodTypeDto queryPeriodType = periodicMeterReadsQuery.getPeriodType();

    // The periodic values are stored in the meter in the buffer of a Profile Generic.
    // Based on the type and the protocol version, get the information for the right Profile from
    // the object configuration.
    final ProfileGeneric profileObject = this.getProfileConfigObject(device, queryPeriodType);

    // A Profile Generic periodically stores values of multiple objects. Usually, this is a
    // timestamp from the Clock, a status and one or more meter values. For M-Bus devices, the
    // timestamp when the meter value was read (the capture time) is stored as well.
    // For some meters, the gas values and the electricity values are combined in one profile.
    // Values in the profile will be selected using selective access. Selecting values based on a
    // start and end datetime should be supported for all devices. Selecting a subset of values to
    // be retrieved (e.g. only gas values in a combined profile) is not supported by all devices.
    final boolean selectedValuesSupported = device.isSelectiveAccessPeriodicMeterReadsSupported();

    // A request can be for a channel (1-4), e.g. for M-Bus devices.
    final int channel =
        periodicMeterReadsQuery.getChannel() != null
            ? periodicMeterReadsQuery.getChannel().getChannelNumber()
            : 0;

    // The profile object from the object config contains information about the objects for which
    // a value (one of the attributes) is stored in the profile: the capture objects.
    // Note that the order is important: the meter will return the values in the order of the
    // capture object definition in the profile.
    // All capture objects are retrieved from the config to get information about the scaler and
    // unit of the values. The scaler and unit might depend on the device model.
    final List<CaptureObject> allCaptureObjectsInProfile =
        this.getCaptureObjectsInProfile(
            profileObject, device, channel, this.getDeviceModel(combinedDeviceModelCode, channel));

    // If selectedValues is supported, then determine a subset of capture objects that are to be
    // retrieved. E.g. when it is a combined profile, we can only get the gas values without the
    // electricity values. This is more efficient and improves privacy (we only get what we need).
    final List<CaptureObject> selectedCaptureObjects =
        this.getSelectedCaptureObjects(
            allCaptureObjectsInProfile, this.config.getMedium(), channel, selectedValuesSupported);

    // Check if it's needed to select values. If all values are requested, then we can skip the
    // select values option in the request to the meter.
    final boolean selectValues =
        selectedValuesSupported
            && (allCaptureObjectsInProfile.size() != selectedCaptureObjects.size());

    // To request the values from the meter, we need the address. This contains the obis code of
    // the profile object, the attribute id of the buffer (2) and the selective access parameters:
    // the from and to dates and (if applicable) the selected values.
    final AttributeAddress profileBufferAddress =
        this.getAttributeAddressForProfile(
            profileObject, from, to, channel, selectedCaptureObjects, selectValues);

    log.info(
        "Retrieving current billing period and profiles for {} for period type: {}, from: "
            + "{}, to: {}",
        this.config.getMedium().name(),
        queryPeriodType,
        from,
        to);

    conn.getDlmsMessageListener()
        .setDescription(
            String.format(
                this.config.getFormatDescription(),
                queryPeriodType,
                from,
                to,
                JdlmsObjectToStringUtil.describeAttributes(profileBufferAddress),
                periodicMeterReadsQuery.getChannel()));

    // This is the actual request to the meter. The DlmsHelper will automatically check if the
    // result is SUCCESS. Otherwise, it will throw an exception.
    final List<GetResult> getResultList =
        this.dlmsHelper.getAndCheck(
            conn,
            device,
            "retrieve periodic meter reads for " + queryPeriodType + ", channel " + channel,
            profileBufferAddress);

    log.debug("Received getResult: {} ", getResultList);

    // Unpack the data from the meter response
    final DataObject resultData =
        this.dlmsHelper.readDataObject(
            getResultList.get(0), this.config.getReadDataObjectDescription());
    final List<DataObject> bufferedObjectsList = resultData.getValue();

    // The values in the bufferedObjectList now need to be converted to a ResponseItem including
    // information about the time, the type of value and the unit.

    // A capture object might not have a fixed scaler unit in the config, or the scaler unit needs
    // to be chosen based on the device type. So check if that is the case and update the capture
    // objects if necessary. Note: this might result in an additional request to the meter.
    final List<CaptureObject> captureObjectsWithScalerUnit =
        this.checkAndGetScalerUnits(
            selectedCaptureObjects, conn, device, this.config.getMedium(), channel);

    // The interval time of the profile is important. For efficiency, most meters only send a
    // timestamp for the first value in the response. The timestamp of the other values should be
    // calculated using the interval time.
    final ProfileCaptureTime intervalTime = this.getProfileCaptureTime(profileObject);

    // Now convert the retrieved values. Each buffered object contains the values for a single
    // interval (e.g. a timestamp, a status and one or more meter values). The values in the
    // buffered object are in the order of the capture objects in the profile.
    final List<MeterReadsResponseWithLogTimeDto> periodicMeterReads = new ArrayList<>();
    for (final DataObject bufferedObject : bufferedObjectsList) {
      final List<DataObject> bufferedObjectValue = bufferedObject.getValue();

      try {
        periodicMeterReads.add(
            this.convertToResponseItem(
                queryPeriodType,
                captureObjectsWithScalerUnit,
                intervalTime,
                bufferedObjectValue,
                channel,
                periodicMeterReads));
      } catch (final BufferedDateTimeValidationException | ProtocolAdapterException e) {
        log.warn(e.getMessage(), e);
      }
    }

    // To be sure no values are returned outside the requested period, filter on from and to date.
    final List<MeterReadsResponseWithLogTimeDto> periodicMeterReadsWithinRequestedPeriod =
        periodicMeterReads.stream()
            .filter(
                meterRead ->
                    this.validateDateTime(meterRead.getLogTime(), from.toDate(), to.toDate()))
            .toList();

    log.debug("Resulting periodicMeterReads: {} ", periodicMeterReads);

    return periodicMeterReadsWithinRequestedPeriod;
  }

  protected Date readClock(
      final PeriodTypeDto periodType,
      final Optional<Date> previousLogTime,
      final ProfileCaptureTime intervalTime,
      final DataObject bufferedObject)
      throws ProtocolAdapterException, BufferedDateTimeValidationException {

    final Date logTime;

    final CosemDateTimeDto cosemDateTime =
        this.dlmsHelper.readDateTime(bufferedObject, "Clock from " + periodType + " buffer");

    final DateTime bufferedDateTime = cosemDateTime == null ? null : cosemDateTime.asDateTime();

    if (bufferedDateTime != null) {
      logTime = bufferedDateTime.toDate();
    } else {
      logTime =
          this.calculateIntervalTimeBasedOnPreviousValue(
              periodType, previousLogTime, Optional.of(intervalTime));
    }

    if (logTime == null) {
      throw new BufferedDateTimeValidationException("Unable to calculate logTime");
    }

    return logTime;
  }

  private AttributeAddress getAttributeAddressForProfile(
      final CosemObject profile,
      final DateTime from,
      final DateTime to,
      final int channel,
      final List<CaptureObject> selectedCaptureObjects,
      final boolean selectValues) {

    final SelectiveAccessDescription access =
        this.getAccessDescription(selectedCaptureObjects, from, to, selectValues);

    final ObisCode obisCode = new ObisCode(profile.getObis().replace("x", String.valueOf(channel)));

    return new AttributeAddress(profile.getClassId(), obisCode, BUFFER.attributeId(), access);
  }

  private List<CaptureObject> getCaptureObjectsInProfile(
      final ProfileGeneric profile,
      final DlmsDevice device,
      final Integer channel,
      final String deviceModel)
      throws ProtocolAdapterException {
    try {
      return profile.getCaptureObjects(
          this.objectConfigService,
          device.getProtocolName(),
          device.getProtocolVersion(),
          channel,
          deviceModel);
    } catch (final ObjectConfigException e) {
      throw new ProtocolAdapterException(
          "Could not get capture objects for profile " + profile.getTag(), e);
    }
  }

  private List<CaptureObject> getSelectedCaptureObjects(
      final List<CaptureObject> allCaptureObjectsInProfile,
      final Medium medium,
      final int channel,
      final boolean selectedValuesSupported)
      throws ProtocolAdapterException {

    final List<CaptureObject> selectedObjects = new ArrayList<>();

    try {
      for (final CaptureObject captureObject : allCaptureObjectsInProfile) {
        // If selectedValues is supported, then select all capture objects with the same medium and
        // the same channel. All abstract objects (clock and amr status) should be selected as well.
        if (!selectedValuesSupported
            || (captureObject.getCosemObject().getGroup().equals(medium.name())
                && captureObject.getCosemObject().getChannel() == channel)
            || (captureObject.getCosemObject().getGroup().equals(Medium.ABSTRACT.name()))) {
          selectedObjects.add(captureObject);
        }
      }
    } catch (final ObjectConfigException e) {
      throw new ProtocolAdapterException("Can't get selected capture objects", e);
    }

    return selectedObjects;
  }

  private SelectiveAccessDescription getAccessDescription(
      final List<CaptureObject> selectedCaptureObjects,
      final DateTime from,
      final DateTime to,
      final boolean selectValues) {

    if (from == null || to == null) {
      return null;
    } else {
      final int accessSelector = 1;

      final DataObject selectedValues = this.getSelectedValuesObject(selectedCaptureObjects);

      final DataObject accessParameter =
          this.dlmsHelper.getAccessSelectionTimeRangeParameter(
              from,
              to,
              selectValues ? selectedValues : DataObject.newArrayData(Collections.emptyList()));

      return new SelectiveAccessDescription(accessSelector, accessParameter);
    }
  }

  private DataObject getSelectedValuesObject(final List<CaptureObject> selectedObjects) {
    final List<DataObject> objectDefinitions = this.getObjectDefinitions(selectedObjects);
    return DataObject.newArrayData(objectDefinitions);
  }

  private List<DataObject> getObjectDefinitions(final List<CaptureObject> selectedObjects) {
    final List<DataObject> objectDefinitions = new ArrayList<>();

    for (final CaptureObject captureObject : selectedObjects) {
      final CosemObject relatedObject = captureObject.getCosemObject();
      objectDefinitions.add(
          DataObject.newStructureData(
              Arrays.asList(
                  DataObject.newUInteger16Data(relatedObject.getClassId()),
                  DataObject.newOctetStringData(new ObisCode(relatedObject.getObis()).bytes()),
                  DataObject.newInteger8Data((byte) captureObject.getAttributeId()),
                  DataObject.newUInteger16Data(0))));
    }

    return objectDefinitions;
  }

  private ProfileCaptureTime getProfileCaptureTime(final CosemObject profile)
      throws ProtocolAdapterException {

    final Attribute capturePeriodAttribute = profile.getAttribute(CAPTURE_PERIOD.attributeId());
    final String capturePeriodValue = capturePeriodAttribute.getValue();

    return switch (capturePeriodValue) {
      case "900" -> ProfileCaptureTime.QUARTER_HOUR;
      case "3600" -> ProfileCaptureTime.HOUR;
      case "86400" -> ProfileCaptureTime.DAY;
      case "0" -> ProfileCaptureTime.MONTH;
      default ->
          throw new ProtocolAdapterException("Unexpected capture period " + capturePeriodValue);
    };
  }

  private List<CaptureObject> checkAndGetScalerUnits(
      final List<CaptureObject> captureObjects,
      final DlmsConnectionManager conn,
      final DlmsDevice device,
      final Medium medium,
      final int channel)
      throws ProtocolAdapterException {
    final List<CaptureObject> captureObjectsWithScalerUnit = new ArrayList<>();

    // Each relevant meter value retrieved from the meter should have a scaler and unit. If values
    // were retrieved from a combined (E+G) profile and selectedValues is not supported, then
    // more values are retrieved than needed. For example: if the request was for gas and channel 1,
    // then all electricity values and values for other channels are not relevant and no scaler and
    // unit is needed for those objects. Note: order of the capture objects is important and
    // should not change.
    final List<CaptureObject> relevantCaptureObjects =
        this.getRelevantCaptureObjects(captureObjects, medium.name(), channel);

    final List<CaptureObject> captureObjectsThatNeedScalerUnitFromMeter = new ArrayList<>();

    for (final CaptureObject captureObject : relevantCaptureObjects) {
      final Register register = (Register) captureObject.getCosemObject();

      // There are 2 possibilities for the scalerUnit in the capture object:
      // - A fixed scalerUnit is defined. In that case, we don't need to do anything.
      // - No scalerUnit is defined or the scalerUnit is defined as Dynamic. In that case, the
      //   scaler unit needs to be read from the meter
      if (register.needsScalerUnitFromMeter()) {
        captureObjectsThatNeedScalerUnitFromMeter.add(captureObject);
      }
    }

    // Get scaler units from meter. They are read from the meter in one call for efficiency.
    final Map<CaptureObject, String> scalerUnits =
        new HashMap<>(
            this.getScalerUnitsFromMeter(captureObjectsThatNeedScalerUnitFromMeter, conn, device));

    // Create a new list with capture objects and fill in the missing scaler units.
    // Note: the order should be the same as the order of the capture objects in the input param.
    for (final CaptureObject captureObject : captureObjects) {
      if (scalerUnits.containsKey(captureObject)) {
        final CosemObject cosemObject = captureObject.getCosemObject();
        final Attribute scalerUnitAttribute =
            cosemObject.getAttribute(ExtendedRegisterAttribute.SCALER_UNIT.attributeId());

        final Attribute newScalerUnitAttribute =
            scalerUnitAttribute.copyWithNewValue(scalerUnits.get(captureObject));

        captureObjectsWithScalerUnit.add(
            captureObject.copyWithNewAttribute(newScalerUnitAttribute));
      } else {
        captureObjectsWithScalerUnit.add(captureObject);
      }
    }

    return captureObjectsWithScalerUnit;
  }

  private List<CaptureObject> getRelevantCaptureObjects(
      final List<CaptureObject> captureObjects, final String medium, final int channel) {
    return captureObjects.stream()
        .filter(captureObject -> captureObject.getCosemObject() instanceof Register)
        .filter(
            captureObject ->
                captureObject.getAttributeId() == ExtendedRegisterAttribute.VALUE.attributeId())
        .filter(captureObject -> captureObject.getCosemObject().getGroup().equals(medium))
        .filter(
            captureObject ->
                this.getChannelWithoutException(captureObject.getCosemObject()) == channel)
        .toList();
  }

  private int getChannelWithoutException(final CosemObject object) {
    try {
      return object.getChannel();
    } catch (final ObjectConfigException e) {
      return -1;
    }
  }

  private Map<CaptureObject, String> getScalerUnitsFromMeter(
      final List<CaptureObject> captureObjects,
      final DlmsConnectionManager conn,
      final DlmsDevice device)
      throws ProtocolAdapterException {
    if (captureObjects.isEmpty()) {
      return Map.of();
    }

    final Map<CaptureObject, String> captureObjectsWithScalerUnit = new HashMap<>();

    final AttributeAddress[] scalerUnitAddresses = this.getScalerUnitAddresses(captureObjects);

    conn.getDlmsMessageListener()
        .setDescription(
            String.format(
                "GetPeriodicMeterReadsGas scaler units, retrieve attribute: %s",
                JdlmsObjectToStringUtil.describeAttributes(scalerUnitAddresses)));

    final List<GetResult> getResults =
        this.dlmsHelper.getWithList(conn, device, scalerUnitAddresses);

    if (getResults.stream().anyMatch(result -> result.getResultCode() != AccessResultCode.SUCCESS)
        || getResults.size() != captureObjects.size()) {
      throw new ProtocolAdapterException(
          "Could not get all scaler units from meter: " + getResults);
    }

    final List<String> scalerUnits = this.readScalerUnits(getResults);

    for (int i = 0; i < scalerUnits.size(); i++) {
      captureObjectsWithScalerUnit.put(captureObjects.get(i), scalerUnits.get(i));
    }

    return captureObjectsWithScalerUnit;
  }

  private AttributeAddress[] getScalerUnitAddresses(final List<CaptureObject> captureObjects) {
    return captureObjects.stream().map(this::getScalerUnitAddress).toArray(AttributeAddress[]::new);
  }

  private AttributeAddress getScalerUnitAddress(final CaptureObject captureObject) {
    final CosemObject cosemObject = captureObject.getCosemObject();
    return new AttributeAddress(
        cosemObject.getClassId(),
        new ObisCode(cosemObject.getObis()),
        ExtendedRegisterAttribute.SCALER_UNIT.attributeId());
  }

  private List<String> readScalerUnits(final List<GetResult> getResultList)
      throws ProtocolAdapterException {

    final List<String> scalerUnits = new ArrayList<>();

    for (final GetResult getResult : getResultList) {
      final DataObject scalerUnitObject = getResult.getResultData();

      scalerUnits.add(
          this.dlmsHelper.getScalerUnit(
              scalerUnitObject, "get scaler unit for periodic meter reads"));
    }

    return scalerUnits;
  }

  protected boolean validateDateTime(
      final Date meterReadTime, final Date beginDateTime, final Date endDateTime) {

    if (meterReadTime.before(beginDateTime) || meterReadTime.after(endDateTime)) {
      log.info(
          "Not using an object from capture buffer (clock= {}), because the date does not match the given period: [ {} .. {} ].",
          meterReadTime,
          beginDateTime,
          endDateTime);
      return false;
    } else {
      return true;
    }
  }

  /**
   * Calculates/derives the next interval time in case it was not present in the current meter read
   * record.
   *
   * @param periodTypeDto the time interval period.
   * @param previousLogTime the logTime of the previous meter read record
   * @param intervalTime the interval time for this device to be taken into account when the
   *     periodTypeDto is INTERVAL
   * @return the derived date based on the previous meter read record, or null if it cannot be
   *     determined
   */
  protected Date calculateIntervalTimeBasedOnPreviousValue(
      final PeriodTypeDto periodTypeDto,
      final Optional<Date> previousLogTime,
      final Optional<ProfileCaptureTime> intervalTime)
      throws BufferedDateTimeValidationException {

    if (!previousLogTime.isPresent()) {
      throw new BufferedDateTimeValidationException(
          "Unable to calculate next interval date, previous logTime " + "is not available");
    }

    final Date prevLogTime = previousLogTime.get();

    return switch (periodTypeDto) {
      case DAILY -> Date.from(prevLogTime.toInstant().plus(Duration.ofDays(1)));
      case MONTHLY -> {
        final LocalDateTime localDateTime =
            LocalDateTime.ofInstant(prevLogTime.toInstant(), ZoneId.systemDefault()).plusMonths(1);
        yield Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
      }
      case INTERVAL ->
          Date.from(
              prevLogTime
                  .toInstant()
                  .plus(Duration.ofMinutes(this.getIntervalTimeMinutes(intervalTime))));
      default ->
          throw new BufferedDateTimeValidationException(
              "Invalid PeriodTypeDto given: " + periodTypeDto);
    };
  }

  private int getIntervalTimeMinutes(final Optional<ProfileCaptureTime> intervalTime) {

    final ProfileCaptureTime profileCaptureTime =
        intervalTime.isPresent() ? intervalTime.get() : null;
    int intervalTimeMinutes = 0;
    if (profileCaptureTime == ProfileCaptureTime.QUARTER_HOUR) {
      intervalTimeMinutes = 15;
    } else if (profileCaptureTime == ProfileCaptureTime.HOUR) {
      intervalTimeMinutes = 60;
    }
    return intervalTimeMinutes;
  }

  /**
   * Reads AmrProfileStatusCode from DataObject holding a bitvalue in a numeric datatype.
   *
   * @param amrProfileStatusData AMR profile register value.
   * @return AmrProfileStatusCode object holding status enum values.
   * @throws ProtocolAdapterException on invalid register data.
   */
  AmrProfileStatusCodeDto readAmrProfileStatusCode(final DataObject amrProfileStatusData)
      throws ProtocolAdapterException {

    if (!amrProfileStatusData.isNumber()) {
      throw new ProtocolAdapterException(
          "Could not read AMR profile register data. Invalid data type.");
    }

    log.debug(
        "Received amrProfileStatusData {} - {}",
        amrProfileStatusData.toString(),
        amrProfileStatusData.getValue());

    final Set<AmrProfileStatusCodeFlagDto> flags =
        this.amrProfileStatusCodeHelper.toAmrProfileStatusCodeFlags(
            amrProfileStatusData.getValue());
    return new AmrProfileStatusCodeDto(flags);
  }

  private String getDeviceModel(final CombinedDeviceModelCode code, final int channel) {
    if (channel > 0) {
      return code.getCodeFromChannel(channel);
    } else {
      return null;
    }
  }

  protected Optional<Date> getPreviousLogTime(
      final List<MeterReadsResponseWithLogTimeDto> periodicMeterReads) {

    if (periodicMeterReads.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(periodicMeterReads.get(periodicMeterReads.size() - 1).getLogTime());
  }

  private ProfileGeneric getProfileConfigObject(
      final DlmsDevice device, final PeriodTypeDto periodType) throws ProtocolAdapterException {
    final CosemObject profile;

    final String protocol = device.getProtocolName();
    final String version = device.getProtocolVersion();

    try {
      profile =
          switch (periodType) {
            case DAILY -> {
              final Optional<CosemObject> optionalDaily =
                  this.objectConfigService.getOptionalCosemObject(
                      protocol, version, this.config.getDailyObjectType());
              if (optionalDaily.isPresent()) {
                yield optionalDaily.get();
              } else {
                yield this.objectConfigService.getCosemObject(
                    protocol, version, DAILY_VALUES_COMBINED);
              }
            }
            case MONTHLY -> {
              final Optional<CosemObject> optionalMonthly =
                  this.objectConfigService.getOptionalCosemObject(
                      protocol, version, this.config.getMonthlyObjectType());
              if (optionalMonthly.isPresent()) {
                yield optionalMonthly.get();
              } else {
                yield this.objectConfigService.getCosemObject(
                    protocol, version, MONTHLY_VALUES_COMBINED);
              }
            }
            case INTERVAL ->
                this.objectConfigService.getCosemObject(
                    protocol, version, this.config.getIntervalObjectType());
          };
    } catch (final ObjectConfigException e) {
      throw new ProtocolAdapterException(
          "Can't find profile object in "
              + protocol
              + " "
              + version
              + " config for "
              + periodType.name()
              + " values",
          e);
    }

    return (ProfileGeneric) profile;
  }
}
