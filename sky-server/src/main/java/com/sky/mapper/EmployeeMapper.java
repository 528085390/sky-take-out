package com.sky.mapper;

import com.sky.dto.EmployeeDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */

    @Insert("insert into employee " +
            "(username, name, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{username},#{name},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})" )
    void insert(Employee employee);



    /**
     * 根据身份证号查询员工
     * @param idNumber
     * @return
     */
    @Select("select * from employee where id_number = #{idNumber}")
    Employee getByIdNumber(String idNumber);

    /**
     * 根据手机号查询员工
     * @param phone
     * @return
     */
    @Select("select * from employee where phone = #{phone}")
    Employee getByPhone(String phone);

    /**
     * 根据姓名查询员工
     * @param name
     * @return
     */
    @Select("select * from employee where name = #{name}")
    Employee getByName(String name);
}
