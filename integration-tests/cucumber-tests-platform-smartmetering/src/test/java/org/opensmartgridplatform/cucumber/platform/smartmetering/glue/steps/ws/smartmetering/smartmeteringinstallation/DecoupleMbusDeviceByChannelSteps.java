// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.ws.smartmetering.smartmeteringinstallation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opensmartgridplatform.cucumber.platform.smartmetering.PlatformSmartmeteringKeys.MBUS_DEVICE_IDENTIFICATION;
import static org.opensmartgridplatform.cucumber.platform.smartmetering.PlatformSmartmeteringKeys.MBUS_IDENTIFICATION_NUMBER;
import static org.opensmartgridplatform.cucumber.platform.smartmetering.PlatformSmartmeteringKeys.RESULT_STRING;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceByChannelAsyncRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceByChannelAsyncResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceByChannelRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceByChannelResponse;
import org.opensmartgridplatform.cucumber.core.ScenarioContext;
import org.opensmartgridplatform.cucumber.platform.PlatformKeys;
import org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.ws.smartmetering.AbstractSmartMeteringSteps;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ws.smartmetering.installation.DecoupleMbusDeviceByChannelRequestFactory;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ws.smartmetering.installation.SmartMeteringInstallationClient;
import org.opensmartgridplatform.shared.exceptionhandling.WebServiceSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.client.SoapFaultClientException;

public class DecoupleMbusDeviceByChannelSteps extends AbstractSmartMeteringSteps {

  @Autowired private SmartMeteringInstallationClient smartMeteringInstallationClient;

  @When(
      "^the Decouple M-Bus Device By Channel \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received$")
  public void theDeCoupleMbusDeviceByChannelFromEmeterRequestIsReceived(
      final String channel, final String eMeter) throws WebServiceSecurityException {

    final DecoupleMbusDeviceByChannelRequest request =
        DecoupleMbusDeviceByChannelRequestFactory.fromGatewayAndChannel(eMeter, channel);
    final DecoupleMbusDeviceByChannelAsyncResponse asyncResponse =
        this.smartMeteringInstallationClient.decoupleMbusDeviceByChannel(request);

    this.checkAndSaveCorrelationId(asyncResponse.getCorrelationUid());
  }

  @Then("^the Decouple M-Bus Device By Channel response is \"([^\"]*)\" with responsedata$")
  public void theDecoupleResponseIs(final String status, final Map<String, String> settings)
      throws WebServiceSecurityException {

    final DecoupleMbusDeviceByChannelResponse response = this.getAndCheckResponse(status);

    if (settings.containsKey(MBUS_DEVICE_IDENTIFICATION)) {
      assertThat(response.getMbusDeviceIdentification())
          .as(MBUS_DEVICE_IDENTIFICATION)
          .isEqualTo(settings.get(MBUS_DEVICE_IDENTIFICATION));
    }
    if (settings.containsKey(MBUS_IDENTIFICATION_NUMBER)) {
      assertThat(response.getChannelElementValues().getIdentificationNumber())
          .as(MBUS_IDENTIFICATION_NUMBER)
          .isEqualTo(settings.get(MBUS_IDENTIFICATION_NUMBER));
    }
    if (settings.containsKey(RESULT_STRING)) {
      assertThat(response.getResultString())
          .as(RESULT_STRING)
          .isEqualTo(settings.get(RESULT_STRING));
    }
  }

  private DecoupleMbusDeviceByChannelResponse getAndCheckResponse(final String status)
      throws WebServiceSecurityException {
    final DecoupleMbusDeviceByChannelAsyncRequest request =
        DecoupleMbusDeviceByChannelRequestFactory.fromScenarioContext();
    final DecoupleMbusDeviceByChannelResponse response =
        this.smartMeteringInstallationClient.getDecoupleMbusDeviceByChannelResponse(request);

    assertThat(response.getResult()).as("Result").isNotNull();
    assertThat(response.getResult().name()).as("Result").isEqualTo(status);
    return response;
  }

  @Then("^retrieving the Decouple By Channel response results in an exception$")
  public void retrievingTheDecoupleResponseResultsInAnException()
      throws WebServiceSecurityException {

    final DecoupleMbusDeviceByChannelAsyncRequest asyncRequest =
        DecoupleMbusDeviceByChannelRequestFactory.fromScenarioContext();

    try {
      this.smartMeteringInstallationClient.getDecoupleMbusDeviceByChannelResponse(asyncRequest);
      fail("A SoapFaultClientException should be thrown");
    } catch (final SoapFaultClientException e) {
      ScenarioContext.current().put(PlatformKeys.RESPONSE, e);
    }
  }

  @When(
      "^the Decouple M-Bus Device By Channel \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received for an unknown gateway$")
  public void theDecoupleGMeterFromEMeterRequestIsReceivedForAnUnknownDevice(
      final String channel, final String eMeter) throws WebServiceSecurityException {

    final DecoupleMbusDeviceByChannelRequest request =
        DecoupleMbusDeviceByChannelRequestFactory.fromGatewayAndChannel(eMeter, channel);

    try {
      this.smartMeteringInstallationClient.decoupleMbusDeviceByChannel(request);
      fail("A SoapFaultClientException should be thrown");
    } catch (final SoapFaultClientException e) {
      ScenarioContext.current().put(PlatformKeys.RESPONSE, e);
    }
  }
}
