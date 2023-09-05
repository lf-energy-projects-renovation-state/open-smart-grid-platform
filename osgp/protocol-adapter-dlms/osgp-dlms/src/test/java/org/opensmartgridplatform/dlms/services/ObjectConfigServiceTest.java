// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dlms.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;
import org.opensmartgridplatform.dlms.objectconfig.CosemObject;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;
import org.opensmartgridplatform.dlms.objectconfig.ObjectProperty;
import org.opensmartgridplatform.dlms.objectconfig.PowerQualityRequest;

@Slf4j
class ObjectConfigServiceTest {

  private ObjectConfigService objectConfigService;

  @BeforeEach
  void setUp() throws IOException, ObjectConfigException {
    this.objectConfigService = new ObjectConfigService();
  }

  @ParameterizedTest
  @EnumSource(Protocol.class)
  void testProtocolSpecifics(final Protocol protocol) throws ObjectConfigException {

    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects(protocol.getName(), protocol.getVersion());
    Assertions.assertThat(cosemObjects).hasSize(protocol.getNrOfCosemObjects());

    final List<CosemObject> cosemObjectsOnDemandPrivate =
        this.getCosemObjectsWithProperties(
            protocol, PowerQualityRequest.ACTUAL_SP, Profile.PRIVATE);
    Assertions.assertThat(cosemObjectsOnDemandPrivate)
        .hasSize(protocol.getNrOfCosemObjects(PowerQualityRequest.ACTUAL_SP, Profile.PRIVATE));

    final List<CosemObject> cosemObjectsOnDemandPublic =
        this.getCosemObjectsWithProperties(protocol, PowerQualityRequest.ACTUAL_SP, Profile.PUBLIC);
    Assertions.assertThat(cosemObjectsOnDemandPublic)
        .hasSize(protocol.getNrOfCosemObjects(PowerQualityRequest.ACTUAL_SP, Profile.PUBLIC));

    final List<CosemObject> cosemObjectsOnPeriodicSpPrivate =
        this.getCosemObjectsWithProperties(
            protocol, PowerQualityRequest.PERIODIC_SP, Profile.PRIVATE);
    Assertions.assertThat(cosemObjectsOnPeriodicSpPrivate)
        .hasSize(protocol.getNrOfCosemObjects(PowerQualityRequest.PERIODIC_SP, Profile.PRIVATE));

    final List<CosemObject> cosemObjectsOnPeriodicPpPrivate =
        this.getCosemObjectsWithProperties(
            protocol, PowerQualityRequest.PERIODIC_PP, Profile.PRIVATE);
    Assertions.assertThat(cosemObjectsOnPeriodicPpPrivate)
        .hasSize(protocol.getNrOfCosemObjects(PowerQualityRequest.PERIODIC_PP, Profile.PRIVATE));

    final List<CosemObject> cosemObjectsPeriodicSpPublic =
        this.getCosemObjectsWithProperties(
            protocol, PowerQualityRequest.PERIODIC_SP, Profile.PUBLIC);
    Assertions.assertThat(cosemObjectsPeriodicSpPublic)
        .hasSize(protocol.getNrOfCosemObjects(PowerQualityRequest.PERIODIC_SP, Profile.PUBLIC));

    final List<CosemObject> cosemObjectsPeriodicPpPublic =
        this.getCosemObjectsWithProperties(
            protocol, PowerQualityRequest.PERIODIC_PP, Profile.PUBLIC);
    Assertions.assertThat(cosemObjectsPeriodicPpPublic)
        .hasSize(protocol.getNrOfCosemObjects(PowerQualityRequest.PERIODIC_PP, Profile.PUBLIC));
  }

  @ParameterizedTest
  @EnumSource(Protocol.class)
  void testProfilesObjectsContainPqObjects(final Protocol protocol) throws ObjectConfigException {
    this.assertAllInConfig(
        protocol, DlmsObjectType.DEFINABLE_LOAD_PROFILE, protocol.hasDefinableLoadProfile());
    this.assertAllInConfig(
        protocol, DlmsObjectType.POWER_QUALITY_PROFILE_1, protocol.hasPqProfiles());
    this.assertAllInConfig(
        protocol, DlmsObjectType.POWER_QUALITY_PROFILE_2, protocol.hasPqProfiles());
  }

  @ParameterizedTest
  @EnumSource(Protocol.class)
  void testAllPqObjectMapToProfile(final Protocol protocol) throws ObjectConfigException {
    final List<DlmsObjectType> dlmsProfiles = new ArrayList<>();
    if (protocol.hasDefinableLoadProfile()) {
      Assertions.assertThat(
              this.objectConfigService.getCosemObject(
                  protocol.getName(), protocol.getVersion(), DlmsObjectType.DEFINABLE_LOAD_PROFILE))
          .isNotNull();
      dlmsProfiles.add(DlmsObjectType.DEFINABLE_LOAD_PROFILE);
    }
    if (protocol.hasPqProfiles()) {
      Assertions.assertThat(
              this.objectConfigService.getCosemObject(
                  protocol.getName(),
                  protocol.getVersion(),
                  DlmsObjectType.POWER_QUALITY_PROFILE_1))
          .isNotNull();
      Assertions.assertThat(
              this.objectConfigService.getCosemObject(
                  protocol.getName(),
                  protocol.getVersion(),
                  DlmsObjectType.POWER_QUALITY_PROFILE_2))
          .isNotNull();
      dlmsProfiles.addAll(
          List.of(DlmsObjectType.POWER_QUALITY_PROFILE_1, DlmsObjectType.POWER_QUALITY_PROFILE_2));
    }
    if (!dlmsProfiles.isEmpty()) {
      this.assertAllPqPeriodicObjectsInProfiles(protocol, dlmsProfiles);
    }
  }

  private List<CosemObject> getCosemObjectsWithProperties(
      final Protocol protocol, final PowerQualityRequest powerQualityRequest, final Profile profile)
      throws ObjectConfigException {
    final EnumMap<ObjectProperty, List<String>> pqProperties = new EnumMap<>(ObjectProperty.class);
    pqProperties.put(ObjectProperty.PQ_PROFILE, Collections.singletonList(profile.name()));
    pqProperties.put(ObjectProperty.PQ_REQUEST, List.of(powerQualityRequest.name()));

    return this.objectConfigService.getCosemObjectsWithProperties(
        protocol.getName(), protocol.getVersion(), pqProperties);
  }

  @Test
  void testGetCosemObjectsDsmr22() throws ObjectConfigException {
    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects("DSMR", "2.2");

    assertNotNull(cosemObjects);
    Assertions.assertThat(cosemObjects).hasSize(Protocol.DSMR_2_2.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
    assertNotNull(cosemObjects.get(DlmsObjectType.NUMBER_OF_VOLTAGE_SWELLS_FOR_L1));
  }

  @Test
  void testGetCosemObjectsDsmr422() throws ObjectConfigException {
    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects("DSMR", "4.2.2");

    assertNotNull(cosemObjects);
    Assertions.assertThat(cosemObjects).hasSize(Protocol.DSMR_4_2_2.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
    assertNotNull(cosemObjects.get(DlmsObjectType.NUMBER_OF_VOLTAGE_SWELLS_FOR_L1));
    assertNull(cosemObjects.get(DlmsObjectType.CDMA_DIAGNOSTIC));
  }

  @Test
  void getCosemObjectIsNull() throws ObjectConfigException {
    final String protocolName = "SMR";
    final String protocolVersion43 = "4.3";

    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects(protocolName, protocolVersion43);

    assertNotNull(cosemObjects);
    org.assertj.core.api.Assertions.assertThat(cosemObjects)
        .hasSize(Protocol.SMR_4_3.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
  }

  @Test
  void testGetCosemObjectsSmr43_shouldComplyWithAssertionChecks() throws ObjectConfigException {
    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects("SMR", "4.3");

    assertNotNull(cosemObjects);
    Assertions.assertThat(cosemObjects).hasSize(Protocol.SMR_4_3.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
    assertNotNull(cosemObjects.get(DlmsObjectType.NUMBER_OF_VOLTAGE_SWELLS_FOR_L1));
    assertNotNull(cosemObjects.get(DlmsObjectType.CDMA_DIAGNOSTIC));
    assertNull(cosemObjects.get(DlmsObjectType.MBUS_DIAGNOSTIC));
  }

  @Test
  void testGetCosemObjectsSmr500() throws ObjectConfigException {
    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects("SMR", "5.0.0");

    assertNotNull(cosemObjects);
    Assertions.assertThat(cosemObjects).hasSize(Protocol.SMR_5_0_0.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
    assertNotNull(cosemObjects.get(DlmsObjectType.NUMBER_OF_POWER_FAILURES));
    assertNull(cosemObjects.get(DlmsObjectType.LTE_DIAGNOSTIC));
    assertNull(cosemObjects.get(DlmsObjectType.PUSH_SETUP_UDP));
  }

  @Test
  void testGetCosemObjectsSmr51() throws ObjectConfigException {
    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects("SMR", "5.1");

    assertNotNull(cosemObjects);
    Assertions.assertThat(cosemObjects).hasSize(Protocol.SMR_5_1.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
    assertNotNull(cosemObjects.get(DlmsObjectType.NUMBER_OF_POWER_FAILURES));
    assertNull(cosemObjects.get(DlmsObjectType.LTE_DIAGNOSTIC));
    assertNull(cosemObjects.get(DlmsObjectType.PUSH_SETUP_UDP));
  }

  @Test
  void testGetCosemObjectsSmr52() throws ObjectConfigException {
    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects("SMR", "5.2");

    assertNotNull(cosemObjects);
    Assertions.assertThat(cosemObjects).hasSize(Protocol.SMR_5_2.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_2));
    assertNotNull(cosemObjects.get(DlmsObjectType.NUMBER_OF_POWER_FAILURES));
    assertNotNull(cosemObjects.get(DlmsObjectType.LTE_DIAGNOSTIC));
    assertNull(cosemObjects.get(DlmsObjectType.PUSH_SETUP_UDP));
  }

  @Test
  void testGetCosemObjectsSmr55() throws ObjectConfigException {
    final Map<DlmsObjectType, CosemObject> cosemObjects =
        this.objectConfigService.getCosemObjects("SMR", "5.5");

    assertNotNull(cosemObjects);
    Assertions.assertThat(cosemObjects).hasSize(Protocol.SMR_5_5.getNrOfCosemObjects());
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_1));
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_2));
    assertNotNull(cosemObjects.get(DlmsObjectType.ALARM_REGISTER_3));
    assertNotNull(cosemObjects.get(DlmsObjectType.NUMBER_OF_POWER_FAILURES));
    assertNotNull(cosemObjects.get(DlmsObjectType.LTE_DIAGNOSTIC));
    assertNotNull(cosemObjects.get(DlmsObjectType.PUSH_SETUP_UDP));
  }

  @Test
  void testDlmsProfileNotFound() {
    assertThrows(
        ObjectConfigException.class, () -> this.objectConfigService.getCosemObjects("ABC", "12"));
  }

  @Test
  void getCosemObjectNotFoundShouldThrowIllegalArgumentException() {
    final String protocolVersion43 = "4.3";
    final String protocolName = "SMR";

    assertThrows(
        IllegalArgumentException.class,
        () ->
            this.objectConfigService.getCosemObject(
                protocolName, protocolVersion43, DlmsObjectType.MBUS_DIAGNOSTIC));
  }

  @Test
  void getCosemObjectShouldFindObject() throws ObjectConfigException {
    final String protocolVersion43 = "4.3";
    final String protocolName = "SMR";

    final CosemObject cosemObject =
        this.objectConfigService.getCosemObject(
            protocolName, protocolVersion43, DlmsObjectType.AVERAGE_VOLTAGE_L1);

    assertNotNull(cosemObject);
    Assertions.assertThat(cosemObject.getTag())
        .isEqualTo(DlmsObjectType.AVERAGE_VOLTAGE_L1.value());
  }

  @Test
  void getOptionalCosemObject() throws ObjectConfigException {
    Assertions.assertThat(
            this.objectConfigService.getOptionalCosemObject(
                "DSMR", "4.2.2", DlmsObjectType.POWER_QUALITY_PROFILE_1))
        .isEmpty();

    Assertions.assertThat(
            this.objectConfigService.getOptionalCosemObject(
                "SMR", "5.0.0", DlmsObjectType.POWER_QUALITY_PROFILE_1))
        .isPresent();
  }

  @Test
  void testGetCosemObjectsWithPropertiesWithMultipleAllowedValues() throws ObjectConfigException {
    final String protocolName = "SMR";
    final String protocolVersion50 = "5.0.0";

    final Map<ObjectProperty, List<String>> requestMap = new HashMap<>();
    final List<String> wantedValues = new ArrayList<>();
    wantedValues.add("PRIVATE");
    wantedValues.add("PUBLIC");
    requestMap.put(ObjectProperty.PQ_PROFILE, wantedValues);
    final List<CosemObject> cosemObjectsWithSelectableObjects =
        this.objectConfigService.getCosemObjectsWithProperties(
            protocolName, protocolVersion50, requestMap);

    assertNotNull(cosemObjectsWithSelectableObjects);
    Assertions.assertThat(cosemObjectsWithSelectableObjects).hasSize(45);
  }

  @Test
  void testGetCosemObjectsWithPropertiesWithMultipleProperties() throws ObjectConfigException {
    final String protocolName = "SMR";
    final String protocolVersion50 = "5.0.0";

    final Map<ObjectProperty, List<String>> requestMap = new HashMap<>();
    final List<String> wantedValuesPqProfile = new ArrayList<>();
    wantedValuesPqProfile.add("PUBLIC");
    requestMap.put(ObjectProperty.PQ_PROFILE, wantedValuesPqProfile);
    final List<String> wantedValuesPqRequest = new ArrayList<>();
    wantedValuesPqRequest.add("PERIODIC_SP");
    requestMap.put(ObjectProperty.PQ_REQUEST, wantedValuesPqRequest);
    final List<CosemObject> cosemObjectsWithSelectableObjects =
        this.objectConfigService.getCosemObjectsWithProperties(
            protocolName, protocolVersion50, requestMap);

    assertNotNull(cosemObjectsWithSelectableObjects);
    Assertions.assertThat(cosemObjectsWithSelectableObjects).hasSize(9);
  }

  @Test
  void getListOfDlmsProfiles() {

    assertNotNull(this.objectConfigService.getConfiguredDlmsProfiles());
  }

  private void assertAllInConfig(
      final Protocol protocol,
      final DlmsObjectType dlmsObjectType,
      final boolean shouldHaveObjectType)
      throws ObjectConfigException {

    final Optional<CosemObject> optionalProfile =
        this.objectConfigService.getOptionalCosemObject(
            protocol.getName(), protocol.getVersion(), dlmsObjectType);
    if (!shouldHaveObjectType) {
      Assertions.assertThat(optionalProfile).isEmpty();
    } else {
      Assertions.assertThat(optionalProfile).isPresent();

      final List<String> selectableObjects =
          optionalProfile.orElseThrow().getListProperty(ObjectProperty.SELECTABLE_OBJECTS);

      final Map<DlmsObjectType, CosemObject> cosemObjects =
          this.objectConfigService.getCosemObjects(protocol.getName(), protocol.getVersion());

      // All selectable objects must be in config
      selectableObjects.forEach(
          selectableObject ->
              Assertions.assertThat(cosemObjects.get(DlmsObjectType.valueOf(selectableObject)))
                  .isNotNull());
    }
  }

  private void assertAllPqPeriodicObjectsInProfiles(
      final Protocol protocol, final List<DlmsObjectType> pqProfiles) throws ObjectConfigException {
    final List<String> allSelectableObjects = new ArrayList<>();
    for (final DlmsObjectType pqProfile : pqProfiles) {
      final CosemObject cosemObjectProfile =
          this.objectConfigService.getCosemObject(
              protocol.getName(), protocol.getVersion(), pqProfile);
      allSelectableObjects.addAll(
          cosemObjectProfile.getListProperty(ObjectProperty.SELECTABLE_OBJECTS));
    }

    final Map<DlmsObjectType, CosemObject> pqObjects =
        this.objectConfigService.getCosemObjects(protocol.getName(), protocol.getVersion());
    pqObjects.forEach(
        (dlmsObjectType, pqObject) -> {
          if (isPeriodicRequest(pqObject)) {
            Assertions.assertThat(allSelectableObjects).contains(pqObject.getTag());
          }
        });
  }

  private static boolean isPeriodicRequest(final CosemObject object) {
    return object.getProperty(ObjectProperty.PQ_REQUEST) != null
        && (object.getProperty(ObjectProperty.PQ_REQUEST).equals(PowerQualityRequest.PERIODIC_SP)
            || object
                .getProperty(ObjectProperty.PQ_REQUEST)
                .equals(PowerQualityRequest.PERIODIC_PP));
  }
}
