package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

public interface WorkSpaceService {

    /**
     * 获取营业数据
     *
     * @return
     */
    BusinessDataVO getBusinessData();


    /**
     * 获取套餐总览
     *
     * @return
     */
    SetmealOverViewVO getOverviewSetmeals();


    /**
     * 获取菜品总览
     *
     * @return
     */
    DishOverViewVO getOverviewDishes();


    /**
     * 获取订单总览
     *
     * @return
     */
    OrderOverViewVO getOverviewOrders();
}
