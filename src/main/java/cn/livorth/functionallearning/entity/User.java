package cn.livorth.functionallearning.entity;

import lombok.Data;

/**
 * @program: FunctionalLearning
 * @description: 用户
 * @author: livorth
 * @create: 2021-10-02 13:15
 **/
@Data
public class User {
    private int userId;
    private String userName;
    private String password;
}
