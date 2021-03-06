package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.RefundLinksForSearch;

import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundForSearchRefundsResult {


    @JsonProperty("refund_id")
    private String refundId;

    @JsonProperty("created_date")
    private String createdDate;

    private String chargeId;

    private Long amount;

    private RefundLinksForSearch links = new RefundLinksForSearch();
    
    @JsonProperty("status")
    private String status;

    public RefundForSearchRefundsResult() {
    }

    public RefundForSearchRefundsResult(String refundId, String createdDate, String status, String chargeId, Long amount, URI paymentURI, URI refundsURI) {
        this.refundId = refundId;
        this.createdDate = createdDate;
        this.status = status;
        this.chargeId = chargeId;
        this.amount = amount;
        this.links.addSelf(refundsURI.toString()); 
        this.links.addPayment(paymentURI.toString()); 
    }

    public String getRefundId() {
        return refundId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getStatus() {
        return status;
    }

    @JsonProperty("payment_id")
    public String getChargeId() {
        return chargeId;
    }

    @JsonProperty("charge_id")
    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    @JsonProperty("amount_submitted")
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    @JsonProperty("amount")
    public Long getAmount() {
        return amount;
    }
    
    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.RefundLinksForSearch")
    @JsonProperty("_links")
    public RefundLinksForSearch getLinks() {
        return links;
    }

    public static RefundForSearchRefundsResult valueOf(RefundForSearchRefundsResult refundResult, URI paymentURI, URI refundsURI) {
        return new RefundForSearchRefundsResult(
                refundResult.getRefundId(),
                refundResult.getCreatedDate(),
                refundResult.getStatus(),
                refundResult.getChargeId(),
                refundResult.getAmount(),
                paymentURI,
                refundsURI);
    }

    @Override
    public String toString() {
        return "RefundForSearchRefundsResult{" +
                "refundId='" + refundId + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", status='" + status + '\'' +
                ", amount=" + amount + '\'' +
                ", links=" + links + '\'' +
                '}';
    }
}
