package cn.livorth.functionallearning.dao;

import cn.livorth.functionallearning.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @program: FunctionalLearning
 * @description: 用户Mapper
 * @author: livorth
 * @create: 2021-10-02 13:21
 **/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 返回分页后的用户信息
     * @param page
     * @return
     */
    IPage<User> getAllUserByPage(Page<User> page);
}
