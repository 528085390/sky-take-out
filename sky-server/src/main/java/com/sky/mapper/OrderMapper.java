package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Indexed;


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
}
