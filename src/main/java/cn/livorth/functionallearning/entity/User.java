package cn.livorth.functionallearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @program: FunctionalLearning
 * @description: 用户
 * @author: livorth
 * @create: 2021-10-02 13:15
 **/
@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String userName;
    private String password;
}
