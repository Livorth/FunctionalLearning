package cn.livorth.functionallearning.service;

import cn.livorth.functionallearning.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @program: FunctionalLearning
 * @description: 用户服务
 * @author: livorth
 * @create: 2021-10-02 13:21
 **/
public interface UserService {

    /**
     * 返回全部用户的信息
     * @return
     */
    List<User> getAllUser();

    /**
     * 返回分页后的用户信息
     * @return
     */
    List<User> getAllUserByPage(Page<User> page);

}
