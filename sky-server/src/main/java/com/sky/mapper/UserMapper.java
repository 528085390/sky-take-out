package com.sky.mapper;

import com.sky.dto.UserStatisticsDTO;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserMapper {


    /**
     * 根据openid查询用户
     *
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);


    /**
     * 插入用户数据
     *
     * @param user
     */
    void insert(User user);


    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);


    /**
     * 根据日期查询新增用户数量
     *
     * @param begin
     * @param end
     * @return
     */
    List<UserStatisticsDTO> countByDate(LocalDate begin, LocalDate end, LocalDate date);

    /**
     * 根据日期查询总用户数量
     *
     * @param begin
     * @param end
     * @return
     */
    UserStatisticsDTO countTotalByDate(LocalDate begin, LocalDate end, LocalDate date);
}
