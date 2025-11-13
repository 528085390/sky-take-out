package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.dto.OrderStatisticsDTO;
import com.sky.dto.UserStatisticsDTO;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 获取营业数据
     *
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDate begin, LocalDate end, LocalDate date) {

        // 营业额
        List<OrderStatisticsDTO> orderStatisticsDTO = orderMapper.getTurnoverByDateRange(begin, end, date, Orders.COMPLETED);
        BigDecimal turnover = orderStatisticsDTO.stream().map(OrderStatisticsDTO::getTurnover).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 有效订单数 订单完成率
        Integer totalOrderCount = orderMapper.countByDate(begin, end, date, null);
        Integer validOrderCount = orderMapper.countByDate(begin, end, date, Orders.COMPLETED);
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        // 新增用户数
        List<UserStatisticsDTO> newUsersList = userMapper.countByDate(begin, end, date);
        Integer newUsers = newUsersList.stream().map(UserStatisticsDTO::getTotal).reduce(0, Integer::sum);

        // 平均客单价
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = turnover.doubleValue() / validOrderCount;
        }
        return BusinessDataVO.builder()
                .turnover(turnover.doubleValue())
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 套餐总览
     *
     * @return
     */
    @Override
    public SetmealOverViewVO getOverviewSetmeals() {
        // 获取所有套餐
        List<Setmeal> list = setmealMapper.list(new Setmeal());

        // 获取在售套餐数 停售套餐数
        Integer sold = Math.toIntExact(list.stream().filter(s -> s.getStatus().equals(StatusConstant.ENABLE)).count());
        Integer discontinued = list.size() - sold;


        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }


    /**
     * 菜品总览
     *
     * @return
     */
    @Override
    public DishOverViewVO getOverviewDishes() {
        // 获取所有菜品
        List<Dish> list = dishMapper.list(new Dish());

        // 获取在售菜品数 停售菜品数
        Integer sold = Math.toIntExact(list.stream().filter(d -> d.getStatus().equals(StatusConstant.ENABLE)).count());
        Integer discontinued = list.size() - sold;

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }


    /**
     * 订单总览
     *
     * @return
     */
    @Override
    public OrderOverViewVO getOverviewOrders() {
        OrderOverViewVO orderOverview = orderMapper.getOverviewOrders(
                Orders.CANCELLED,
                Orders.COMPLETED,
                Orders.DELIVERY_IN_PROGRESS,
                Orders.TO_BE_CONFIRMED
        );
        return orderOverview;

    }


}
