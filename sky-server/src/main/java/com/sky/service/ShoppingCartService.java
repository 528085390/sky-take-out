package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    public void add(Long userId,ShoppingCartDTO shoppingCartDTO);


    /**
     * 查看购物车
     *
     * @return
     */
    public List<ShoppingCart> list(Long userId);


    /**
     * 删除购物车数据
     *
     * @param shoppingCartDTO
     */
    public void sub(Long userId,ShoppingCartDTO shoppingCartDTO);


    /**
     * 清空购物车
     */
    public void clean(Long userId);
}
