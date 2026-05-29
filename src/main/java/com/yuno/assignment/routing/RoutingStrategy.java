package com.yuno.assignment.routing;

import com.yuno.assignment.enums.PaymentMethod;

public interface RoutingStrategy {

    PaymentRoute route(PaymentMethod paymentMethod);
}
