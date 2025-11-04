package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void save(SetmealDTO setmealDTO) {

        // 向套餐表插入数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);


        // 向套餐菜品关系表插入数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes.isEmpty()) {
            throw new BaseException(MessageConstant.SETMEAL_NOT_FOUND);
        }
        setmealDishes.forEach(dish -> dish.setSetmealId(setmeal.getId()));
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());

    }


    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        // 根据id查询套餐数据
        SetmealVO setmealVO = setmealMapper.getById(id);

        // 根据id查询套餐菜品关系数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }


    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        Long setmealId = setmealDTO.getId();

        // 修改套餐表数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 删除套餐菜品关系数据
        setmealDishMapper.deleteBySetmealId(setmealId);

        // 插入新的套餐菜品关系数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes.isEmpty()) {
            throw new BaseException(MessageConstant.SETMEAL_NOT_FOUND);
        }
        setmealDishes.forEach(dish -> dish.setSetmealId(setmealId));
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 启用、禁用套餐
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealDishes.forEach(setmealDish -> {
            Dish dish = dishMapper.getById(setmealDish.getDishId());
            if (dish.getStatus().equals(StatusConstant.DISABLE)) {
                throw new BaseException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        });

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }


    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            SetmealVO setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus().equals(1)) {
                throw new BaseException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        setmealMapper.deleteBatch(ids);
        setmealDishMapper.delete(ids);

    }


    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }


    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }


}
