// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.cucumber.platform.smartmetering.glue.hooks;

import static org.opensmartgridplatform.cucumber.platform.PlatformDefaults.DEFAULT_SMART_METER_DEVICE_IDENTIFICATION;
import static org.opensmartgridplatform.cucumber.platform.PlatformDefaults.EXPECTED_RESULT_OK;
import static org.opensmartgridplatform.cucumber.platform.PlatformKeys.KEY_DEVICE_IDENTIFICATION;
import static org.opensmartgridplatform.cucumber.platform.PlatformKeys.KEY_RESULT;
import static org.opensmartgridplatform.cucumber.platform.smartmetering.PlatformSmartmeteringKeys.KEY_DEVICE_AUTHENTICATIONKEY;
import static org.opensmartgridplatform.cucumber.platform.smartmetering.PlatformSmartmeteringKeys.KEY_DEVICE_ENCRYPTIONKEY;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import java.util.HashMap;
import java.util.Map;
import org.opensmartgridplatform.cucumber.core.ScenarioContext;
import org.opensmartgridplatform.cucumber.platform.smartmetering.SecurityKey;
import org.opensmartgridplatform.cucumber.platform.smartmetering.database.DlmsDatabase;
import org.opensmartgridplatform.cucumber.platform.smartmetering.database.WsSmartMeteringNotificationDatabase;
import org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.simulator.DeviceSimulatorSteps;
import org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.ws.smartmetering.smartmeteringconfiguration.ReplaceKeysSteps;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ServiceEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/** Class with all the scenario hooks when each scenario runs. */
public class ScenarioHooks {

  @Value("${alarm.notifications.host}")
  private String alarmNotificationsHost;

  @Value("${alarm.notifications.port}")
  private int alarmNotificationsPort;

  @Autowired private DlmsDatabase dlmsDatabase;
  @Autowired private WsSmartMeteringNotificationDatabase wsSmartMeteringNotificationDatabase;

  @Autowired private ReplaceKeysSteps replaceKeysSteps;

  @Autowired private DeviceSimulatorSteps deviceSimulatorSteps;

  @Autowired private ServiceEndpoint serviceEndpoint;

  @Value("${service.endpoint.host}")
  private String serviceEndpointHost;

  /**
   * Executed before each scenario.
   *
   * <p>Remove all stuff from the database before each test. Each test should stand on its own.
   * Therefore you should guarantee that the scenario is complete.
   *
   * <p>Order 1000 ensures this will be run as one of the first hooks before the scenario.
   */
  @Before(order = 1000)
  public void beforeScenario() {
    this.deviceSimulatorSteps.clearDlmsAttributeValues();
    this.dlmsDatabase.prepareDatabaseForScenario();
    this.wsSmartMeteringNotificationDatabase.prepareDatabaseForScenario();
    this.prepareServiceEndpoint();
  }

  /**
   * Executed after each scenario.
   *
   * <p>Order 1000 ensures this will be run as one of the first hooks after the scenario.
   */
  @After(order = 1000)
  public void afterScenario() {
    // Destroy scenario context as the scenario is finished.
    ScenarioContext.context = null;
  }

  // The platform project contains an @After hook with order 99.999 that clears the context
  // So if you need the ScenarioContext in your @After hook the order should be 100.000 or higher.
  @After(order = 100000, value = "@ResetKeysOnDevice")
  public void resetKeysScenario() throws Throwable {
    final Map<String, String> settings = this.initSettings();
    final Map<String, String> responseParameters = this.initResponseParameters();

    this.replaceKeysSteps.theReplaceKeysRequestIsReceived(settings);
    this.replaceKeysSteps.theReplaceKeysResponseShouldBeReturned(responseParameters);
  }

  private void prepareServiceEndpoint() {
    this.serviceEndpoint.setServiceEndpoint(this.serviceEndpointHost);
    this.serviceEndpoint.setAlarmNotificationsHost(this.alarmNotificationsHost);
    this.serviceEndpoint.setAlarmNotificationsPort(this.alarmNotificationsPort);
  }

  private Map<String, String> initSettings() {
    final Map<String, String> map = new HashMap<>();
    putSettings(map, KEY_DEVICE_IDENTIFICATION, DEFAULT_SMART_METER_DEVICE_IDENTIFICATION);
    map.put(KEY_DEVICE_AUTHENTICATIONKEY, SecurityKey.SECURITY_KEY_A.name());
    map.put(KEY_DEVICE_ENCRYPTIONKEY, SecurityKey.SECURITY_KEY_E.name());
    return map;
  }

  private Map<String, String> initResponseParameters() {
    final Map<String, String> map = new HashMap<>();
    putSettings(map, KEY_DEVICE_IDENTIFICATION, DEFAULT_SMART_METER_DEVICE_IDENTIFICATION);
    putSettings(map, KEY_RESULT, EXPECTED_RESULT_OK);
    return map;
  }

  private static void putSettings(
      final Map<String, String> map, final String key, final String defaultValue) {
    map.put(key, (String) ScenarioContext.current().get(key, defaultValue));
  }
}
