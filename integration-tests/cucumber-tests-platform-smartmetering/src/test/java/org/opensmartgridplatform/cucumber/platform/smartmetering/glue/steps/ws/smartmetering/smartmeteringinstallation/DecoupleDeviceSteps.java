// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.ws.smartmetering.smartmeteringinstallation;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMBusDeviceAdministrativeAsyncResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMBusDeviceAdministrativeRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceAdministrativeAsyncRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceAdministrativeResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceAsyncRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceAsyncResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceRequest;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.installation.DecoupleMbusDeviceResponse;
import org.opensmartgridplatform.cucumber.core.ScenarioContext;
import org.opensmartgridplatform.cucumber.platform.PlatformKeys;
import org.opensmartgridplatform.cucumber.platform.smartmetering.glue.steps.ws.smartmetering.AbstractSmartMeteringSteps;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ws.smartmetering.installation.DecoupleMBusDeviceAdministrativeRequestFactory;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ws.smartmetering.installation.DecoupleMbusDeviceRequestFactory;
import org.opensmartgridplatform.cucumber.platform.smartmetering.support.ws.smartmetering.installation.SmartMeteringInstallationClient;
import org.opensmartgridplatform.shared.exceptionhandling.WebServiceSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.client.SoapFaultClientException;

public class DecoupleDeviceSteps extends AbstractSmartMeteringSteps {

  @Autowired private SmartMeteringInstallationClient smartMeteringInstallationClient;

  @When(
      "^the Decouple M-Bus device \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received for an unknown gateway$")
  public void theDecoupleMBusDeviceFromEMeterRequestIsReceivedForAnUnknownDevice(
      final String mBusDeviceIdentification, final String eMeter)
      throws WebServiceSecurityException {
    this.theDecoupleMBusDeviceFromEMeterRequestIsReceivedForAnInactiveOrUnknownDevice(
        mBusDeviceIdentification, eMeter);
  }

  @When(
      "^the Decouple M-Bus device \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received for an inactive gateway$")
  public void theDecoupleMBusDeviceFromEMeterRequestIsReceivedForAnInactiveDevice(
      final String mBusDeviceIdentification, final String eMeter)
      throws WebServiceSecurityException {
    this.theDecoupleMBusDeviceFromEMeterRequestIsReceivedForAnInactiveOrUnknownDevice(
        mBusDeviceIdentification, eMeter);
  }

  private void theDecoupleMBusDeviceFromEMeterRequestIsReceivedForAnInactiveOrUnknownDevice(
      final String mBusDeviceIdentification, final String eMeter)
      throws WebServiceSecurityException {

    final DecoupleMbusDeviceRequest request =
        DecoupleMbusDeviceRequestFactory.forGatewayAndMbusDevice(eMeter, mBusDeviceIdentification);

    try {
      this.smartMeteringInstallationClient.decoupleMbusDevice(request);
      Assertions.fail("A SoapFaultClientException should be thrown");
    } catch (final SoapFaultClientException e) {
      ScenarioContext.current().put(PlatformKeys.RESPONSE, e);
    }
  }

  @When("^the Administrative Decouple M-Bus device \"([^\"]*)\" request is received$")
  public void theAdministrativeDecoupleMBusDeviceRequestIsReceived(
      final String mBusDeviceIdentification) throws WebServiceSecurityException {

    final DecoupleMBusDeviceAdministrativeRequest request =
        DecoupleMBusDeviceAdministrativeRequestFactory.forMbusDevice(mBusDeviceIdentification);
    final DecoupleMBusDeviceAdministrativeAsyncResponse asyncResponse =
        this.smartMeteringInstallationClient.decoupleMbusDeviceAdministrative(request);

    this.checkAndSaveCorrelationId(asyncResponse.getCorrelationUid());
  }

  @When("^the Decouple M-Bus device \"([^\"]*)\" from E-meter \"([^\"]*)\" request is received$")
  public void theDecoupleMBusDeviceRequestIsReceived(
      final String mBusDeviceIdentification, final String eMeter)
      throws WebServiceSecurityException {

    final DecoupleMbusDeviceRequest request =
        DecoupleMbusDeviceRequestFactory.forGatewayAndMbusDevice(eMeter, mBusDeviceIdentification);
    final DecoupleMbusDeviceAsyncResponse asyncResponse =
        this.smartMeteringInstallationClient.decoupleMbusDevice(request);

    this.checkAndSaveCorrelationId(asyncResponse.getCorrelationUid());
  }

  @Then("^the Decouple response is \"([^\"]*)\"$")
  public void theDecoupleResponseIs(final String status) throws WebServiceSecurityException {

    final DecoupleMbusDeviceAsyncRequest decoupleMbusDeviceAsyncRequest =
        DecoupleMbusDeviceRequestFactory.fromScenarioContext();
    final DecoupleMbusDeviceResponse response =
        this.smartMeteringInstallationClient.getDecoupleMbusDeviceResponse(
            decoupleMbusDeviceAsyncRequest);

    assertThat(response.getResult()).as("Result").isNotNull();
    assertThat(response.getResult().name()).as("Result").isEqualTo(status);
  }

  @Then("^the Administrative Decouple response is \"([^\"]*)\"$")
  public void theAdministrativeDecoupleResponseIs(final String status)
      throws WebServiceSecurityException {

    final DecoupleMbusDeviceAdministrativeAsyncRequest
        decoupleMbusDeviceAdministrativeAsyncRequest =
            DecoupleMBusDeviceAdministrativeRequestFactory.fromScenarioContext();
    final DecoupleMbusDeviceAdministrativeResponse response =
        this.smartMeteringInstallationClient.getDecoupleMbusDeviceAdministrativeResponse(
            decoupleMbusDeviceAdministrativeAsyncRequest);

    assertThat(response.getResult()).as("Result").isNotNull();
    assertThat(response.getResult().name()).as("Result").isEqualTo(status);
  }

  @Then("^the Decouple response is \"([^\"]*)\" and contains$")
  public void theDecoupleResponseIsAndContains(final String status, final List<String> resultList)
      throws WebServiceSecurityException {

    final DecoupleMbusDeviceAsyncRequest decoupleMbusDeviceAsyncRequest =
        DecoupleMbusDeviceRequestFactory.fromScenarioContext();
    final DecoupleMbusDeviceResponse response =
        this.smartMeteringInstallationClient.getDecoupleMbusDeviceResponse(
            decoupleMbusDeviceAsyncRequest);

    assertThat(response.getResult()).as("Result").isNotNull();
    assertThat(response.getResult().name()).as("Result").isEqualTo(status);
    assertThat(this.checkDescription(response.getDescription(), resultList))
        .as("Description should contain all of " + resultList)
        .isTrue();
  }

  @Then("^retrieving the Decouple response results in an exception$")
  public void retrievingTheDecoupleResponseResultsInAnException()
      throws WebServiceSecurityException {

    final DecoupleMbusDeviceAsyncRequest asyncRequest =
        DecoupleMbusDeviceRequestFactory.fromScenarioContext();

    try {
      this.smartMeteringInstallationClient.getDecoupleMbusDeviceResponse(asyncRequest);
      Assertions.fail("A SoapFaultClientException should be thrown");
    } catch (final SoapFaultClientException e) {
      ScenarioContext.current().put(PlatformKeys.RESPONSE, e);
    }
  }
}
