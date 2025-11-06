package com.sky.service.impl;


import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.BaseException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.beans.Beans;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;

@Service

public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车数据
     *
     * @param shoppingCartDTO
     */
    @Override
    public void add(Long userId, ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 判断当前添加的购物车项，是否在购物车中存在
        if (list != null && !list.isEmpty()) {
            shoppingCart = list.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.update(shoppingCart);
            return;
        }


        // 创建一个新购物车数据
        if (shoppingCart.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCart.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            SetmealVO setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());

        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        shoppingCarts.add(shoppingCart);
        shoppingCartMapper.insertBatch(shoppingCarts);
    }


    /**
     * 查看购物车
     *
     * @param userId
     * @return
     */
    @Override
    public List<ShoppingCart> list(Long userId) {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 删除购物车数据
     *
     * @param shoppingCartDTO
     */
    @Override
    public void sub(Long userId, ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCartDish = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCartDish);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCartDish);

        if (list != null && !list.isEmpty()) {
            ShoppingCart shoppingCart = list.get(0);
            if (shoppingCart.getNumber() == 1) {
                shoppingCartMapper.deleteById(userId, shoppingCart.getId());
            } else {
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.update(shoppingCart);
            }
        }

    }


    /**
     * 清空购物车数据
     */
    @Override
    public void clean(Long userId) {
        shoppingCartMapper.deleteAllByUserId(userId);
    }
}
