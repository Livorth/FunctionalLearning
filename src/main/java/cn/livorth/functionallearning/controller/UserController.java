package cn.livorth.functionallearning.controller;

import cn.livorth.functionallearning.common.log.LogAnnotation;
import cn.livorth.functionallearning.entity.User;
import cn.livorth.functionallearning.handler.exception.CustomException;
import cn.livorth.functionallearning.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @LogAnnotation(logModule = "user", logType = "select",  logDescription = "获取所有用户信息")
    @GetMapping
    public List<User> getAllUser(){
        return userService.getAllUser();
    }

    /**
     * /user/page/3/5
     * @param thePage 当前多少页
     * @param pageSize 每页多少个
     * @return
     */
    @LogAnnotation(logModule = "user", logType = "select",  logDescription = "通过分页获取所有用户信息")
    @GetMapping("page/{thePage}/{pageSize}")
    public List<User> getAllUserByPage(@PathVariable("thePage") int thePage, @PathVariable("pageSize") int pageSize){
        Page<User> page = new Page<>(thePage, pageSize);
        return userService.getAllUserByPage(page);
    }

    @GetMapping("testExceptionHandler")
    public List<User> testExceptionHandler() throws Exception {
//        int a = 1 / 0;
        throw new Exception();
    }


    @GetMapping("testCustomException")
    public List<User> testCustomException() throws Exception {
        throw new CustomException(505, "这里是自定义异常");
    }


}
