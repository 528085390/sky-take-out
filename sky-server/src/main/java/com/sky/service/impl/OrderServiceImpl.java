package com.sky.service.impl;


import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //构造订单数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());

        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        orders.setUserName(user.getName());

        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        //订单号 雪花算法
        orders.setNumber(IdUtil.getSnowflake().nextIdStr());

        orderMapper.insert(orders);


        //构造详细订单数据
        ShoppingCart cart = ShoppingCart.builder().
                userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(cart);

        List<OrderDetail> orderDetails = list.stream().map(shoppingCart -> {
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(orders.getId())
                    .name(shoppingCart.getName())
                    .dishFlavor(shoppingCart.getDishFlavor())
                    .dishId(shoppingCart.getDishId())
                    .setmealId(shoppingCart.getSetmealId())
                    .number(shoppingCart.getNumber())
                    .amount(shoppingCart.getAmount())
                    .image(shoppingCart.getImage())
                    .build();
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailMapper.insertBatch(orderDetails);


        //返回订单数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }
}
