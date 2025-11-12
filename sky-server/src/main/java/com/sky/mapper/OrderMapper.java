package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrderStatisticsDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     *
     * @param orders
     */
    void insert(Orders orders);


    /**
     * 根据id查询订单数据
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);


    /**
     * 历史订单分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> page(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);


    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);


    /**
     * 统计指定状态的订单数量
     *
     * @param status
     * @return
     *
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Integer status);


    /**
     * 获取超时的订单
     *
     * @param status
     * @param time
     *
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);


    /**
     * 批量更新订单信息
     *
     * @param timeOutOrders
     */
    void updateStatusBatch(List<Orders> timeOutOrders, Orders orders);


    /**
     * 根据日期统计营业额数据
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    @Select("SELECT DATE(order_time) as date, COALESCE(SUM(amount), 0) as turnover " +
            "FROM orders WHERE status = #{status} AND DATE(order_time) BETWEEN #{begin} AND #{end} " +
            "GROUP BY DATE(order_time)")
    List<OrderStatisticsDTO> getTurnoverByDateRange(LocalDate begin, LocalDate end, Integer status);


    /**
     * 根据日期统计订单数数据
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    Integer countByDate(LocalDate begin, LocalDate end, LocalDate date, Integer status);


    /**
     * 获取订单总览数据
     *
     * @return
     */
    OrderOverViewVO getOverviewOrders(
                                      Integer cancelledStatus,
                                      Integer completedStatus,
                                      Integer deliveringStatus,
                                      Integer confirmedStatus);


}
