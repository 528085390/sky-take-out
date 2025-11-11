package com.sky.service.impl;

import com.sky.dto.OrderStatisticsDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 生成日期列表
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }

        // 一次性查询所有日期的营业额数据
        List<OrderStatisticsDTO> statisticsList = orderMapper.getTurnoverStatisticsByDateRange(begin, end, Orders.COMPLETED);

        // 创建日期到营业额的映射
        Map<LocalDate, BigDecimal> statisticsMap = statisticsList.stream()
                .collect(Collectors.toMap(OrderStatisticsDTO::getDate, OrderStatisticsDTO::getTurnover));

        // 构建营业额列表，缺失数据用0填充
        List<BigDecimal> turnoverList = dateList.stream()
                .map(date -> statisticsMap.getOrDefault(date, BigDecimal.ZERO))
                .collect(Collectors.toList());

        // 构建并返回 TurnoverReportVO 对象
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    /**
     * 销量排名
     *
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        orderDetailMapper.getSalesTop10(begin, end);
        return null;
    }


    /**
     * 用户统计
     *
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 创建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }

        // 查询新用户数
        List<Integer> newUserList = new ArrayList<>();
        // 查询总用户数
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Integer newUser = userMapper.countByDate(null, null, date);
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);

            Integer totalUser = userMapper.countByDate(null, date, null);
            totalUser = totalUser == null ? 0 : totalUser;
            totalUserList.add(totalUser);
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }


    /**
     * 订单统计
     *
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 创建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        // 获取日总订单数据
        List<Integer> orderCountList = new ArrayList<>();
        // 获取日有效订单数据
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            Integer total = orderMapper.countByDate(null, null, date, null);
            total = total == null ? 0 : total;
            orderCountList.add(total);

            Integer valid = orderMapper.countByDate(null, null, date, Orders.COMPLETED);
            valid = valid == null ? 0 : valid;
            validOrderCountList.add(valid);
        }

        // 获取订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 获取有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        // 获取订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
}
