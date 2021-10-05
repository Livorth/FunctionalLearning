package cn.livorth.functionallearning.service.impl;

import cn.livorth.functionallearning.dao.UserMapper;
import cn.livorth.functionallearning.entity.User;
import cn.livorth.functionallearning.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: FunctionalLearning
 * @description: 用户服务实现
 * @author: livorth
 * @create: 2021-10-02 13:25
 **/
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, User> redisTemplate;

    @Override
    public List<User> getAllUser() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public List<User> getAllUserByPage(Page<User> page) {
        // 调用分页方法
        IPage<User> allUserByPage = userMapper.getAllUserByPage(page);
        // 转换为List
        List<User> records = allUserByPage.getRecords();
        return records;
    }

    @Override
    public Boolean setAllUserInCacheById(int id) {
        User user = userMapper.selectById(id);
        redisTemplate.opsForValue().set("UserInfo:" + id, user);
        return true;
    }

    @Override
    public User getAllUserByCacheById(int id) {
        User user = (User) redisTemplate.opsForValue().get("UserInfo:" + id);
        return user;
    }


}
