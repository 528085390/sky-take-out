package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrderStatisticsDTO;
import com.sky.dto.UserStatisticsDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkSpaceService workSpaceService;


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
        List<OrderStatisticsDTO> statisticsList = orderMapper.getTurnoverByDateRange(begin, end, null, Orders.COMPLETED);

        // 创建日期到营业额的映射
        Map<LocalDate, BigDecimal> map = statisticsList.stream()
                .collect(Collectors.toMap(OrderStatisticsDTO::getDate, OrderStatisticsDTO::getTurnover));

        // 构建营业额列表，缺失数据用0填充
        List<BigDecimal> turnoverList = dateList.stream()
                .map(date -> map.getOrDefault(date, BigDecimal.ZERO))
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
        List<GoodsSalesDTO> salesTop10 = orderDetailMapper.getSalesTop10(begin, end);

        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();

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


//        for (LocalDate date : dateList) {
//            Integer newUser = userMapper.countByDate(null, null, date);
//            newUser = newUser == null ? 0 : newUser;
//            newUserList.add(newUser);
//
//            Integer totalUser = userMapper.countByDate(null, date, null);
//            totalUser = totalUser == null ? 0 : totalUser;
//            totalUserList.add(totalUser);
//        }
        // 查询新用户数
        List<UserStatisticsDTO> newUserStatistics = userMapper.countByDate(begin, end, null);
        Map<LocalDate, Integer> newUsersMap = newUserStatistics.stream()
                .collect(Collectors.toMap(UserStatisticsDTO::getDate, UserStatisticsDTO::getTotal));
        List<Integer> newUserList = dateList.stream()
                .map(date -> newUsersMap.getOrDefault(date, 0))
                .collect(Collectors.toList());


        // 查询总用户数
        List<UserStatisticsDTO> totalUserStatistics = new ArrayList<>();
        for (LocalDate date : dateList) {
            UserStatisticsDTO totalUser = userMapper.countTotalByDate(null, date, null);
            totalUser.setDate(date);
            totalUserStatistics.add(totalUser);
        }
        Map<LocalDate, Integer> totalUsersMap = totalUserStatistics.stream()
                .collect(Collectors.toMap(UserStatisticsDTO::getDate, UserStatisticsDTO::getTotal));
        List<Integer> totalUserList = dateList.stream()
                .map(date -> totalUsersMap.getOrDefault(date, 0))
                .collect(Collectors.toList());


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


    /**
     * 数据导出
     *
     * @return
     */
    @Override
    public void export(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workSpaceService.getBusinessData(begin, end, null);

        // 创建Excel
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            // 创建Excel
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 获取第一个Sheet
            XSSFSheet sheet = excel.getSheetAt(0);

            // 填充时间
            sheet.getRow(1).getCell(1).setCellValue("时间" + begin + "- " + end);

            // 填充营业额数据
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                BusinessDataVO dayBusinessData = workSpaceService.getBusinessData(null, null, date);
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dayBusinessData.getTurnover());
                row.getCell(3).setCellValue(dayBusinessData.getValidOrderCount());
                row.getCell(4).setCellValue(dayBusinessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dayBusinessData.getUnitPrice());
                row.getCell(6).setCellValue(dayBusinessData.getNewUsers());
            }


            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            in.close();
            out.close();
            excel.close();
        } catch (Exception e) {
            log.error("文件处理异常", e);
        }


    }
}
