package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.validation.PaymentRequestValidator;
import uk.gov.pay.api.validation.URLValidator;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionWithError;
import static uk.gov.pay.api.matcher.PaymentValidationExceptionMatcher.aValidationExceptionContaining;

@RunWith(MockitoJUnitRunner.class)
public class CreatePaymentRequestDeserializerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DeserializationContext ctx;

    private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    private CreatePaymentRequestDeserializer deserializer;

    @Before
    public void setup() {
        URLValidator urlValidator = URLValidator.urlValidatorValueOf(true);
        deserializer = new CreatePaymentRequestDeserializer(new PaymentRequestValidator(urlValidator));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithReturnUrlSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        ValidCreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(Optional.empty()));
        assertThat(paymentRequest.getDelayedCapture(), is(Optional.empty()));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithEnglishLanguageSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"en\"\n" +
                "}";

        ValidCreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage().get(), is(SupportedLanguage.ENGLISH));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithWelshLanguageSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"cy\"\n" +
                "}";

        ValidCreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage().get(), is(SupportedLanguage.WELSH));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithDelayedCaptureEqualsTrueSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": true\n" +
                "}";

        ValidCreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getDelayedCapture().get(), is(Boolean.TRUE));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithDelayedCaptureEqualsFalseSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": false\n" +
                "}";

        ValidCreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getDelayedCapture().get(), is(Boolean.FALSE));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithAnAgreementIdSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"agreement_id\": \"abc123\",\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\"\n" +
                "}";

        ValidCreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is(Optional.empty()));
        assertThat(paymentRequest.getAgreementId().get(), is("abc123"));
    }

    @Test
    public void deserialize_shouldThrowBadRequestException_whenJsonIsNotWellFormed() throws Exception {
        String invalidJson = "{" +
                "  \"amount\" : " +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(BadRequestException.class);
        expectedException.expect(aBadRequestExceptionWithError("P0197", "Unable to parse JSON"));

        deserializer.deserialize(jsonFactory.createParser(invalidJson), ctx);
    }

    @Test
    public void deserialize_shouldThrowBadRequestException_whenAmountIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: amount"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_asAmountIsMissing_whenAmountIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": null,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: amount"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsNotInteger() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": \"\",\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: amount. Must be a valid numeric format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsLessThanMinimum() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 0,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: amount. Must be greater than or equal to 1"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsMoreThanMaximum() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 10000001,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: amount. Must be less than or equal to 10000000"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsNotAStringValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1000000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": 1\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: return_url. Must be a valid URL format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlLengthIsMoreThan2000CharactersLength() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1000000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"" + RandomStringUtils.randomAlphanumeric(2001) + "\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: return_url. Must be less than or equal to 2000 characters length"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsAMalformedUrl() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"" + RandomStringUtils.randomAlphanumeric(50) + "\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: return_url. Must be a valid URL format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: return_url"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsReturnUrlIsMissing_whenReturnUrlIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: return_url"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: reference"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsReferenceIsMissing_whenReferenceIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": null,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: reference"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": 123,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: reference. Must be a valid string format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsMoreThan255CharactersLength() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"" + RandomStringUtils.randomAlphanumeric(256) + "\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: reference. Must be less than or equal to 255 characters length"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: description"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_asDescriptionIsMissing_whenDescriptionIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": null,\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: description"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": 432,\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: description. Must be a valid string format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsMoreThan255CharactersLength() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"" + RandomStringUtils.randomAlphanumeric(256) + "\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: description. Must be less than or equal to 255 characters length"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsAgreementIdIsMissing_whenAgreementIdIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"agreement_id\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: agreement_id"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAgreementIdIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"agreement_id\": 1234\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: agreement_id. Must be a valid agreement ID"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAgreementIdIsMoreThan26CharactersLength() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"agreement_id\": \"" + RandomStringUtils.randomAlphanumeric(27) + "\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: agreement_id. Must be less than or equal to 26 characters length"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLanguageIsNotSupported() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"fr\"\n" +
                "}";

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLanguageIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": 1234\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLanguageIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLanguageIsEmptyString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDelayedCaptureIsNotABoolean() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": \"true\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: delayed_capture. Must be true or false"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDelayedCaptureIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: delayed_capture. Must be true or false"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDelayedCaptureIsNumeric() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": 0\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: delayed_capture. Must be true or false"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @After
    public void tearDown() {
        verifyZeroInteractions(ctx);
    }
}
