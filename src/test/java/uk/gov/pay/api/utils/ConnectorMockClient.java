package uk.gov.pay.api.utils;

import com.google.common.collect.ImmutableMap;
import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;

public class ConnectorMockClient {
    public static final String CONNECTOR_MOCK_ACCOUNTS_PATH = "/v1/api/accounts/%s";
    public static final String CONNECTOR_MOCK_CHARGES_PATH = CONNECTOR_MOCK_ACCOUNTS_PATH + "/charges";
    public static final String CONNECTOR_MOCK_CHARGE_PATH = CONNECTOR_MOCK_CHARGES_PATH + "/%s";
    public static final String CONNECTOR_MOCK_CHARGE_EVENTS_PATH = CONNECTOR_MOCK_CHARGE_PATH + "/events";
    private static final String REFERENCE_KEY = "reference";
    private static final String STATUS_KEY = "status";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private final MockServerClient mockClient;
    private final String baseUrl;

    public ConnectorMockClient(int port, String baseUrl) {
        this.mockClient = new MockServerClient("localhost", port);
        this.baseUrl = baseUrl;
    }

    private String createChargePayload(long amount, String returnUrl, String description, String reference) {
        return jsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    private String createChargeResponse(long amount, String chargeId, String status, String returnUrl, String description,
                                        String reference, String paymentProvider, String createdDate, ImmutableMap<?, ?>... links) {
        return jsonStringBuilder()
                .add("charge_id", chargeId)
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("status", status)
                .add("return_url", returnUrl)
                .add("payment_provider", paymentProvider)
                .add("created_date", createdDate)
                .add("links", asList(links))
                .build();
    }

    private String createChargeEventsResponse(String chargeId, List<Map<String,String>> events, ImmutableMap<?, ?>... links) {
        return jsonStringBuilder()
                .add("charge_id", chargeId)
                .add("events", events)
                .add("links", asList(links))
                .build();
    }

    private ImmutableMap<String, String> validLink(String href, String rel) {
        return ImmutableMap.of(
                "href", href,
                "rel", rel,
                "method", GET);
    }


    private String nextUrl(String chargeId) {
        return "http://Frontend/charge/" + chargeId;
    }

    private String chargeLocation(String accountId, String chargeId) {
        return baseUrl + format(CONNECTOR_MOCK_CHARGE_PATH, accountId, chargeId);
    }

    private String chargeEventsLocation(String accountId, String chargeId) {
        return baseUrl + format(CONNECTOR_MOCK_CHARGE_EVENTS_PATH, accountId, chargeId);
    }

    public void respondOk_whenCreateCharge(long amount, String gatewayAccountId, String chargeId, String status, String returnUrl,
                                           String description, String reference, String paymentProvider, String createdDate) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl, description, reference)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(gatewayAccountId, chargeId))
                        .withBody(createChargeResponse(
                                amount,
                                chargeId,
                                status,
                                returnUrl,
                                description,
                                reference,
                                paymentProvider,
                                createdDate,
                                validLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                                validLink(nextUrl(chargeId), "next_url"))));
    }

    public void respondOk_whenSearchCharges(String accountId, String reference, String status, String fromDate, String toDate, String expectedResponse) {
        whenSearchCharges(accountId, reference, status, fromDate, toDate)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(expectedResponse)
                );

    }

    public void respondUnknownGateway_whenCreateCharge(long amount, String gatewayAccountId, String errorMsg, String returnUrl, String description, String reference) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl, description, reference)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg));
    }

    public void respondOk_withEmptyBody(long amount, String gatewayAccountId, String chargeId, String returnUrl, String description, String reference) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl, description, reference)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(gatewayAccountId, chargeId)));
    }

    public void respondWithChargeFound(long amount, String gatewayAccountId, String chargeId, String status, String returnUrl,
                                       String description, String reference, String paymentProvider, String createdDate) {
        whenGetCharge(gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(createChargeResponse(amount, chargeId, status, returnUrl,
                                description, reference, paymentProvider, createdDate, validLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                                validLink(nextUrl(chargeId), "next_url"))));
    }

    public void respondWithChargeEventsFound(String gatewayAccountId, String chargeId, List<Map<String,String>> events) {
        whenGetChargeEvents(gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(createChargeEventsResponse(chargeId, events, validLink(chargeEventsLocation(gatewayAccountId, chargeId), "self"))));
    }


    public void respondChargeNotFound(String gatewayAccountId, String chargeId, String errorMsg) {
        whenGetCharge(gatewayAccountId, chargeId)
                .respond(withStatusAndErrorMessage(NOT_FOUND_404, errorMsg));
    }

    public void respondChargeEventsNotFound(String gatewayAccountId, String chargeId, String errorMsg) {
        whenGetChargeEvents(gatewayAccountId, chargeId)
                .respond(withStatusAndErrorMessage(NOT_FOUND_404, errorMsg));
    }

    public void respondOk_whenCancelCharge(String paymentId, String accountId) {
        whenCancelCharge(paymentId, accountId)
                .respond(response()
                        .withStatusCode(NO_CONTENT_204));
    }

    public void respondChargeNotFound_WhenCancelCharge(String paymentId, String accountId, String errorMsg) {
        whenCancelCharge(paymentId, accountId)
                .respond(withStatusAndErrorMessage(NOT_FOUND_404, errorMsg));
    }

    public void respondBadRequest_WhenCancelChargeNotAllowed(String paymentId, String accountId, String errorMsg) {
        whenCancelCharge(paymentId, accountId)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg));
    }

    public void respondBadRequest_WhenAccountIdIsMissing(String paymentId, String accountId, String errorMessage) {
        whenCancelCharge(paymentId, accountId)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, errorMessage));
    }

    private ForwardChainExpectation whenCreateCharge(long amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(createChargePayload(amount, returnUrl, description, reference))
        );
    }

    private ForwardChainExpectation whenGetCharge(String gatewayAccountId, String chargeId) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGE_PATH, gatewayAccountId, chargeId))
        );
    }

    private ForwardChainExpectation whenGetChargeEvents(String gatewayAccountId, String chargeId) {
        return mockClient.when(request()
                        .withMethod(GET)
                        .withPath(format(CONNECTOR_MOCK_CHARGE_EVENTS_PATH, gatewayAccountId, chargeId))
        );
    }

    private ForwardChainExpectation whenSearchCharges(String gatewayAccountId, String reference, String status, String fromDate, String toDate) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withQueryStringParameters(notNullQueryParamsFrom(reference, status, fromDate, toDate))
        );
    }

    private Parameter[] notNullQueryParamsFrom(String reference, String status, String fromDate, String toDate) {
        List<Parameter> params = newArrayList();
        if (isNotBlank(reference)) {
            params.add(Parameter.param(REFERENCE_KEY, reference));
        }
        if (isNotBlank(status)) {
            params.add(Parameter.param(STATUS_KEY, status));
        }
        if (isNotBlank(fromDate)) {
            params.add(Parameter.param(FROM_DATE_KEY, fromDate));
        }
        if (isNotBlank(toDate)) {
            params.add(Parameter.param(TO_DATE_KEY, toDate));
        }
        return params.toArray(new Parameter[0]);
    }

    private ForwardChainExpectation whenCancelCharge(String paymentId, String accountId) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(connectorCancelChargePathFor(paymentId, accountId)));
    }

    private String connectorCancelChargePathFor(String paymentId, String accountId) {
        return format(CONNECTOR_MOCK_CHARGE_PATH + "/cancel", accountId, paymentId);
    }

    private HttpResponse withStatusAndErrorMessage(int statusCode, String errorMsg) {
        return response()
                .withStatusCode(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(jsonString("message", errorMsg));
    }

    public void verifyCreateCharge(long amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                        .withBody(createChargePayload(amount, returnUrl, description, reference)),
                once()
        );
    }


    public void verifyCancelCharge(String paymentId, String accountId) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(connectorCancelChargePathFor(paymentId, accountId)),
                once());
    }
}
