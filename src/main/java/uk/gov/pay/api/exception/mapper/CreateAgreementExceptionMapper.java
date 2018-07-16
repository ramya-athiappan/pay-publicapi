package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.model.directdebit.agreement.AgreementError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.directdebit.agreement.AgreementError.anAgreementError;

public class CreateAgreementExceptionMapper implements ExceptionMapper<CreateAgreementException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAgreementExceptionMapper.class);

    @Override
    public Response toResponse(CreateAgreementException exception) {

        AgreementError agreementError = exception.getErrorStatus() == NOT_FOUND.getStatusCode()
                ? anAgreementError(AgreementError.Code.CREATE_AGREEMENT_ACCOUNT_ERROR)
                : anAgreementError(AgreementError.Code.CREATE_AGREEMENT_CONNECTOR_ERROR);

        LOGGER.error("Direct Debit connector invalid response was {}.\n Returning http status {} with error body {}",
                exception.getMessage(), INTERNAL_SERVER_ERROR, agreementError);

        return Response
                .status(INTERNAL_SERVER_ERROR)
                .entity(agreementError)
                .build();
    }
}