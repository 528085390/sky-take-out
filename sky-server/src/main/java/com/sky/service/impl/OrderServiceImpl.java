package com.sky.service.impl;


import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.BaseException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.webSocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private WebSocketServer webSocketServer;


    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //构造订单数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orders.setAddress(address);
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

        //清空原购物车数据
        shoppingCartMapper.deleteAllByUserId(userId);

        //返回订单数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }


    /**
     * 订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getDetail(Long id) {
        Orders order = getOrders(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(id);

        //订单菜品信息
        String orderDishes = orderDetailList.stream().map(orderDetail -> {
            String name = orderDetail.getName();
            Integer number = orderDetail.getNumber();
            String info = name + "*" + number;
            return info;
        }).collect(Collectors.joining(" , "));

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        orderVO.setOrderDishes(orderDishes);

        return orderVO;
    }


    /**
     * 历史订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        List<OrderVO> list = orderMapper.page(ordersPageQueryDTO);

        list = list.stream().map(order -> {
            List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(order.getId());
            order.setOrderDetailList(orderDetailList);

            String orderDishes = orderDetailList.stream().map(orderDetail -> {
                String name = orderDetail.getName();
                Integer number = orderDetail.getNumber();
                String info = name + "*" + number;
                return info;
            }).collect(Collectors.joining(" , "));
            order.setOrderDishes(orderDishes);

            return order;
        }).collect(Collectors.toList());

        return new PageResult(list.size(), list);

    }


    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void cancel(Long id) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = getOrders(id);
        if (!orders.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }


    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @Transactional
    public void repetition(Long id) {
        //找回订单数据
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetails = orderDetailMapper.listByOrderId(id);

        //构造购物车数据
        List<ShoppingCart> shoppingCarts = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(userId);
            return shoppingCart;

        }).collect(Collectors.toList());

        shoppingCartMapper.deleteAllByUserId(userId);
        shoppingCartMapper.insertBatch(shoppingCarts);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map<String,Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content","订单号" +outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);


    }

    /**
     * 订单统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);

        return OrderStatisticsVO.builder()
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .toBeConfirmed(toBeConfirmed)
                .build();
    }


    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<OrderVO> list = orderMapper.page(ordersPageQueryDTO);
        list = list.stream().map(order -> {
            List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(order.getId());
            order.setOrderDetailList(orderDetailList);

            String orderDishes = orderDetailList.stream().map(orderDetail -> {
                String name = orderDetail.getName();
                Integer number = orderDetail.getNumber();
                String info = name + "x" + number;
                return info;
            }).collect(Collectors.joining(" , "));
            order.setOrderDishes(orderDishes);

            return order;
        }).collect(Collectors.toList());
        return new PageResult(list.size(), list);

    }


    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders ordersDB = getOrders(ordersConfirmDTO.getId());
        if (!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }


    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = getOrders(ordersRejectionDTO.getId());
        if (!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.REFUND)
                .build();
        orderMapper.update(orders);
    }


    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = getOrders(ordersCancelDTO.getId());
        if (!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS) && !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .payStatus(Orders.REFUND)
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }


    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders ordersDB = getOrders(id);
        if (!ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }


    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders ordersDB = getOrders(id);
        if (!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders ordersDB = getOrders(id);

        Map<String, Object> map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号" +ordersDB.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    private Orders getOrders(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return ordersDB;
    }

}


