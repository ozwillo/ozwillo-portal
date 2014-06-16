package org.oasis_eu.portal.core.model.appstore;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * User: schambon
 * Date: 5/14/14
 */
public class Application extends AbstractApplication {

    @JsonProperty("payment_option")
    private PaymentOption paymentOption;
    @JsonProperty("target_audience")
    private Map<Audience, Boolean> targetAudience = new HashMap<>();


    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(PaymentOption paymentOption) {
        this.paymentOption = paymentOption;
    }

    public Map<Audience, Boolean> getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(Map<Audience, Boolean> targetAudience) {
        this.targetAudience = targetAudience;
    }

    public boolean isTargetedTo(Audience audience) {
        return targetAudience.get(audience);
    }

}
