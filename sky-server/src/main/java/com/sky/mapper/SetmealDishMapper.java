package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);


    /**
     * 根据菜品id查询套餐
     *
     * @param dishIds
     * @return
     */
    List<Setmeal> getByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐数据
     *
     * @param dishList
     */
    void insertBatch(List<SetmealDish> dishList);

    /**
     * 根据套餐id查询菜品数据
     *
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据套餐id删除菜品数据
     *
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);


    /**
     * 批量删除
     *
     * @param ids
     */
    void delete(List<Long> ids);
}
