package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     *
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);


    /**
     * 插入员工数据
     *
     * @param employee
     */

    @Insert("insert into employee " +
            "(username, name, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{username},#{name},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);


    /**
     * 根据身份证号查询员工
     *
     * @param idNumber
     * @return
     */
    @Select("select * from employee where id_number = #{idNumber}")
    Employee getByIdNumber(String idNumber);

    /**
     * 根据手机号查询员工
     *
     * @param phone
     * @return
     */
    @Select("select * from employee where phone = #{phone}")
    Employee getByPhone(String phone);

    /**
     * 根据姓名查询员工
     *
     * @param name
     * @return
     */
    @Select("select * from employee where name = #{name}")
    Employee getByName(String name);

    /**
     * 分页查询员工数据
     *
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);


    /**
     * 修改员工账号信息
     *
     * @param employee
     * @return
     */
    void update(Employee employee);
}

