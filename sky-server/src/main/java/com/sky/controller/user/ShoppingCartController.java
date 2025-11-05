package com.sky.controller.user;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     *
     * @param ShoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO ShoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户{}添加购物车：{}", userId, ShoppingCartDTO);
        shoppingCartService.add(userId, ShoppingCartDTO);
        return Result.success();
    }


    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户{}查看购物车...", userId);
        List<ShoppingCart> list = shoppingCartService.list(userId);
        return Result.success(list);
    }

    /**
     * 删除购物车中的数据
     *
     * @param shoppingCartDTO
     * @return
     */

    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户{}删除购物车数据：{}", userId, shoppingCartDTO);
        shoppingCartService.sub(userId, shoppingCartDTO);
        return null;
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @DeleteMapping("/clean")
    public Result clean() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户{}清空购物车...", userId);
        shoppingCartService.clean(userId);
        return Result.success();
    }

}
