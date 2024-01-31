package com.jpabook.jpashop.api;

import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderItem;
import com.jpabook.jpashop.repository.order.OrderDto;
import com.jpabook.jpashop.repository.order.OrderRepository;
import com.jpabook.jpashop.repository.order.OrderSearch;
import com.jpabook.jpashop.repository.order.query.OrderQueryDto;
import com.jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 간단한 주문 조회 V1: 엔티티를 직접 노출
     * [ Hibernate5 모듈 적용 안하면 StackOverflow 발생됨 ]
     * @return 주문 내역
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        // Lazy Loading Init
        for(Order order: orders) {
            order.getMember().getName();
            order.getDelievery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }

        return orders;
    }

    /**
     * 간단한 주문 조회 V2: 엔티티를 DTO로 변환
     * @return 주문 내역
     */
    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                                       .map(OrderDto::new)
                                       .collect(Collectors.toList());

        return new Result(result);
    }

    /**
     * 간단한 주문 조회 V3: Fetch Join 최적화
     * @return 주문 내역
     *
     * [문제점 1] 조인된 row를 그대로 entity로 가져오기 때문에, 불필요한 데이터 중복이 발생한다.
     * -> order: orderItem = 1: N 관계에서 두 테이블의 조인에 따른 order의 불필요한 중복 발생.
     *
     * (해결 1) JPA 자체에서 SELECT 절에 DISTINCT 를 제공한다
     *
     * [* 문제점 2] 컬렉션 페치 조인 사용시 페이징이 불가하다.
     * 1. 해당 쿼리의 모든 데이터를 DB에서 읽어오고, 애플리케이션 메모리 자체에서 페이징을 진행한다.
     *    -> 서버 메모리 고갈로 인해 매우 위험함 !!
     *
     * (해결 2) 주문 정보 API v3.1
     *
     * [문제점 3] 컬렉션 페치 조인은 1개만 사용할 수 있다.
     * -> 컬렉션 둘 이상에 페치 조인을 사용할 경우, 데이터가 부정합하게 조회될 수 있다.
     */
    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                                      .map(OrderDto::new)
                                      .collect(Collectors.toList());

        return new Result(result);
    }

    /**
     * 간단한 주문 조회 V3.1: 페이징 한계 돌파
     *
     * @XToOne 관계의 엔티티만 Fetch Join으로 처리하자 !
     *
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
            @RequestParam(value = "limit", defaultValue = "10", required = false) int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelievery(offset, limit);
        List<OrderDto> result = orders.stream()
                                       .map(OrderDto::new)
                                       .collect(Collectors.toList());

        return new Result(result);
    }

    /**
     * 간단한 주문 조회 V4: JPA에서 컬렉션을 포함한 DTO 직접 조회
     * @return
     */
    @GetMapping("/api/v4/orders")
    public Result ordersV4() {
        List<OrderQueryDto> orders = orderQueryRepository.findOrderQueryDtos();

        return new Result(orders);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
}
