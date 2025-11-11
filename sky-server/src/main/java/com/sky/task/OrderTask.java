package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void processTimeoutOrder() {
        log.info("处理支付超时订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> timeOutOrders = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if (timeOutOrders == null || timeOutOrders.isEmpty()) {
            log.info("没有超时订单");
            return;
        }
        Orders timeOutOrder = Orders.builder()
                .status(Orders.CANCELLED)
                .cancelReason("支付超时，取消订单")
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.updateStatusBatch(timeOutOrders, timeOutOrder);
        log.info("超时订单{}处理完毕", timeOutOrders);
    }

    /**
     * 处理派送超时订单
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryTimeoutOrder() {
        log.info("处理派送超时订单");
        List<Orders> timeOutOrders = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now());
        if (timeOutOrders == null || timeOutOrders.isEmpty()) {
            log.info("没有派送超时订单");
            return;
        }
        Orders timeOutOrder = Orders.builder()
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.updateStatusBatch(timeOutOrders, timeOutOrder);
    }
}
