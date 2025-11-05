package com.sky.mapper;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;


import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询购物车
     *
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, dish_id, setmeal_id, dish_flavor, number, amount, create_time, user_id) " +
            "values (#{name}, #{image}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime}, #{userId})")
    void insert(ShoppingCart shoppingCart);


    /**
     * 根据用户id删除购物车数据
     *
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteAllByUserId(Long userId);


    /**
     * 根据用户id和购物车数据id删除购物车数据
     *
     * @param
     */
    @Delete("delete from shopping_cart where id = #{id} and user_id = #{userId}")
    void deleteById(Long userId, Long id);


    /**
     * 修改购物车数据
     *
     * @param
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart shoppingCart);
}


