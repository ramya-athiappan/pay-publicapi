package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementRequest;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementResponse;
import uk.gov.pay.api.model.directdebit.agreement.connector.CreateMandateResponse;
import uk.gov.pay.api.model.directdebit.agreement.support.Utils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints for an agreements")
@Produces({"application/json"})
public class AgreementsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsResource.class);

    private final Client client;
    private final String baseUrl;
    private final String connectorDDUrl;

    @Inject
    public AgreementsResource(Client client, PublicApiConfig configuration) {
        this.client = client;
        this.baseUrl = configuration.getBaseUrl();
        this.connectorDDUrl = configuration.getConnectorDDUrl();
    }

    @POST
    @Timed
    @Path("/v1/agreements")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create a new agreement",
            notes = "Create a new agreement",
            code = 201,
            nickname = "newAgreement")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = CreateAgreementResponse.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createNewAgreement(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                       @ApiParam(value = "requestPayload", required = true) CreateAgreementRequest createAgreementRequest) {

        LOGGER.info("Agreement create request - [ {} ]", createAgreementRequest);

        Response connectorResponse = client
                .target(getDDConnectorUrl(format("/v1/api/accounts/%s/mandates", account.getName())))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildAgreementRequestPayload(createAgreementRequest));

        if (connectorResponse.getStatus() == HttpStatus.SC_CREATED) {
            CreateMandateResponse createMandateResponse = connectorResponse.readEntity(CreateMandateResponse.class);
            CreateAgreementResponse createAgreementResponse = Utils.createMandateResponse2CreateAgreementResponse(createMandateResponse);
            URI agreementUri = UriBuilder.fromUri(baseUrl)
                    .path("/v1/agreements/{agreementId}")
                    .build(createAgreementResponse.getAgreementId());
            LOGGER.info("Agreement returned (created): [ {} ]", createAgreementResponse);
            return Response.created(agreementUri).entity(createAgreementResponse).build();
        }

        throw new CreateAgreementException(connectorResponse);
    }

    private Entity buildAgreementRequestPayload(CreateAgreementRequest requestPayload) {
        return json(new JsonStringBuilder()
                .add(CreateAgreementRequest.RETURN_URL_FIELD_NAME, requestPayload.getReturnUrl())
                .add(CreateAgreementRequest.AGREEMENT_TYPE_FIELD_NAME, requestPayload.getAgreementType().toString())
                .build());
    }

    private String getDDConnectorUrl(String urlPath) {
        UriBuilder builder = UriBuilder
                .fromPath(connectorDDUrl)
                .path(urlPath);

        return builder.toString();
    }
}
