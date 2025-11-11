package com.sky.service.impl;

import com.sky.dto.OrderStatisticsDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
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
}
