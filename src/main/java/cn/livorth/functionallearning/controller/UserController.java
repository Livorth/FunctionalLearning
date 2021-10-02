package cn.livorth.functionallearning.controller;

import cn.livorth.functionallearning.entity.User;
import cn.livorth.functionallearning.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: FunctionalLearning
 * @description: 用户Controller
 * @author: livorth
 * @create: 2021-10-02 13:27
 **/
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> categories(){
        return userService.getAllUser();
    }

    /**
     * /user/page/3/5
     * @param thePage 当前多少页
     * @param pageSize 每页多少个
     * @return
     */
    @GetMapping("page/{thePage}/{pageSize}")
    public List<User> getAllUserByPage(@PathVariable("thePage") int thePage, @PathVariable("pageSize") int pageSize){
        Page<User> page = new Page<>(thePage, pageSize);
        return userService.getAllUserByPage(page);
    }
}
