package com.jpabook.jpashop.repository.order;

import com.jpabook.jpashop.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class OrderSearch {
    private String memberName;
    private OrderStatus orderStatus;
}
