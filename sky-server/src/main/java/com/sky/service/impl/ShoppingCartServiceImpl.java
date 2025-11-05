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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void add(Long userId, ShoppingCartDTO shoppingCartDTO) {

        // 判断当前添加的购物车项，是否在购物车中存在
        ShoppingCart shoppingCartDish = shoppingCartMapper.getByUserIdAndDishId(userId, shoppingCartDTO);
        ShoppingCart shoppingCartSetmeal = shoppingCartMapper.getByUserIdAndSetmealId(userId, shoppingCartDTO);
        if (shoppingCartDish != null) {
            shoppingCartMapper.increaseNumber(shoppingCartDish.getId());
            return;
        } else if (shoppingCartSetmeal != null) {
            shoppingCartMapper.increaseNumber(shoppingCartSetmeal.getId());
            return;
        }


        // 创建一个新购物车数据
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .dishId(shoppingCartDTO.getDishId())
                .setmealId(shoppingCartDTO.getSetmealId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .createTime(LocalDateTime.now())
                .number(1)
                .build();
        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCart.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else if (shoppingCartDTO.getSetmealId() != null) {
            SetmealVO setmeal = setmealMapper.getById(shoppingCart.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        } else {
            throw new BaseException(MessageConstant.SHOPPING_CART_ERROR);
        }
        shoppingCartMapper.insert(shoppingCart);
    }


    /**
     * 查看购物车
     *
     * @param userId
     * @return
     */
    @Override
    public List<ShoppingCart> list(Long userId) {
        List<ShoppingCart> list = shoppingCartMapper.list(userId);
        return list;
    }

    /**
     * 删除购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void sub(Long userId, ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCartDish = shoppingCartMapper.getByUserIdAndDishId(userId, shoppingCartDTO);
        ShoppingCart shoppingCartSetmeal = shoppingCartMapper.getByUserIdAndSetmealId(userId, shoppingCartDTO);

        if (shoppingCartDish != null) {
            if (shoppingCartDish.getNumber() == 1) {
                shoppingCartMapper.deleteById(userId,shoppingCartDish.getId());
            } else {
                shoppingCartMapper.decreaseNumber(shoppingCartDish.getId());
            }
        } else if (shoppingCartSetmeal != null) {
            if (shoppingCartSetmeal.getNumber() == 1) {
                shoppingCartMapper.deleteById(userId,shoppingCartSetmeal.getId());
            }else {
                shoppingCartMapper.decreaseNumber(shoppingCartSetmeal.getId());
            }

        }
    }


    /**
     * 清空购物车
     */
    @Override
    public void clean(Long userId) {
        shoppingCartMapper.deleteAllByUserId(userId);
    }
}
