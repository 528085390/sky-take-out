package com.sky.mapper;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 根据用户id查询购物车
     *
     * @param userId
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> list(Long userId);

    /**
     * 插入购物车数据
     *
     * @param shoppingCart
     */
    @Select("insert into shopping_cart (name, image, dish_id, setmeal_id, dish_flavor, number, amount, create_time, user_id) " +
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
     * 根据用户id和菜品id查询购物车数据
     *
     * @return
     */
    ShoppingCart getByUserIdAndDishId(Long userId, ShoppingCartDTO shoppingCartDTO);

    /**
     * 根据用户id和套餐id查询购物车数据
     *
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId} and setmeal_id = #{shoppingCartDTO.setmealId}")
    ShoppingCart getByUserIdAndSetmealId(Long userId, ShoppingCartDTO shoppingCartDTO);


    /**
     * 根据用户id和购物车数据id添加购物车数据
     *
     * @param
     */
    @Select("update shopping_cart set number = number + 1 where id = #{id}")
    void increaseNumber(Long id);


    /**
     * 根据用户id和购物车数据id删除购物车数据
     *
     * @param
     */
    @Delete("delete from shopping_cart where id = #{id} and user_id = #{userId}")
    void deleteById(Long userId,Long id);


    /**
     * 根据用户id和购物车数据id减少购物车数据
     *
     * @param
     */
    @Select("update shopping_cart set number = number - 1 where id = #{id}")
    void decreaseNumber(Long id);
}


