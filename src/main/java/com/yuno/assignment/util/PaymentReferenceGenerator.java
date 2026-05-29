package com.yuno.assignment.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentReferenceGenerator {

    public String nextReference() {
        return "pay_" + UUID.randomUUID().toString().replace("-", "");
    }
}
