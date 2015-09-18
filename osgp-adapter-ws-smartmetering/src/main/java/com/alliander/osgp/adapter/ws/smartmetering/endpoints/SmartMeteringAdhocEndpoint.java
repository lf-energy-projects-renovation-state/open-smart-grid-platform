package com.alliander.osgp.adapter.ws.smartmetering.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.alliander.osgp.adapter.ws.endpointinterceptors.OrganisationIdentification;
import com.alliander.osgp.adapter.ws.schema.smartmetering.adhoc.SynchronizeTimeReadsRequest;
import com.alliander.osgp.adapter.ws.schema.smartmetering.adhoc.SynchronizeTimeReadsResponse;
import com.alliander.osgp.adapter.ws.schema.smartmetering.common.AsyncResponse;
import com.alliander.osgp.adapter.ws.schema.smartmetering.monitoring.PeriodicMeterReadsRequest;
import com.alliander.osgp.adapter.ws.schema.smartmetering.monitoring.PeriodicMeterReadsResponse;
import com.alliander.osgp.adapter.ws.smartmetering.application.mapping.InstallationMapper;
import com.alliander.osgp.adapter.ws.smartmetering.application.services.InstallationService;
import com.alliander.osgp.shared.exceptionhandling.ComponentType;
import com.alliander.osgp.shared.exceptionhandling.OsgpException;
import com.alliander.osgp.shared.exceptionhandling.TechnicalException;


//MethodConstraintViolationException is deprecated.
//Will by replaced by equivalent functionality defined
//by the Bean Validation 1.1 API as of Hibernate Validator 5.
@SuppressWarnings("deprecation")
@Endpoint
public class SmartMeteringAdhocEndpoint {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmartMeteringAdhocEndpoint.class);
    private static final String SMARTMETER_ADHOC_NAMESPACE = "http://www.alliander.com/schemas/osgp/smartmetering/sm-adhoc/2014/10";

    @Autowired
    private InstallationService installationService;

    @Autowired
    private InstallationMapper installationMapper;

    public SmartMeteringAdhocEndpoint() {
    }
    
    
    @PayloadRoot(localPart = "SynchronizeTimeReadsRequest", namespace = SMARTMETER_ADHOC_NAMESPACE)
    @ResponsePayload
	public SynchronizeTimeReadsResponse requestPeriodicData(
    		@OrganisationIdentification final String organisationIdentification,
//            @RequestPayload final PeriodicMeterReadsRequest request) throws OsgpException {
            @RequestPayload final SynchronizeTimeReadsRequest request) throws OsgpException {

        // LOGGER.info("Incoming PeriodicMeterReadsRequest for meter: {}.",
        // request.getPeriodicMeterReadsrequest().);

        final SynchronizeTimeReadsResponse response = new SynchronizeTimeReadsResponse();

        // try {

        // final SmartMeteringDevice device =
        // this.installationMapper.map(request.getDevice(),
        // SmartMeteringDevice.class);
        //
        // final String correlationUid =
        // this.installationService.addDevice(organisationIdentification,
        // device);
        //
        final AsyncResponse asyncResponse = new AsyncResponse();
        asyncResponse.setCorrelationUid("1234");
        asyncResponse.setDeviceId("5678123465");
        response.setAsyncResponse(asyncResponse);
        //
        // } catch (final MethodConstraintViolationException e) {
        //
        //
        // LOGGER.error("Exception: {} while adding device: {} for organisation {}.",
        // // new Object[] { e.getMessage(),
        // // request.getDevice().getDeviceIdentification(),
        // // organisationIdentification }, e);
        // //
        // // throw new
        // // FunctionalException(FunctionalExceptionType.VALIDATION_ERROR,
        // // ComponentType.WS_CORE,
        // // new ValidationException(e.getConstraintViolations()));
        // //
        // // } catch (final Exception e) {
        // //
        // //
        // LOGGER.error("Exception: {} while adding device: {} for organisation {}.",
        // // new Object[] { e.getMessage(),
        // // request.getDevice().getDeviceIdentification(),
        // // organisationIdentification }, e);
        // //
        // // this.handleException(e);
        // // }
        //
        return response;
    }

    private void handleException(final Exception e) throws OsgpException {
        // Rethrow exception if it already is a functional or technical
        // exception,
        // otherwise throw new technical exception.
        if (e instanceof OsgpException) {
            LOGGER.error("Exception occurred: ", e);
            throw (OsgpException) e;
        } else {
            LOGGER.error("Exception occurred: ", e);
            throw new TechnicalException(ComponentType.WS_SMART_METERING, e);
        }
    }
}
