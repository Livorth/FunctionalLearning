package cn.livorth.functionallearning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class FunctionalLearningApplicationTests {

    @Autowired
    private StringRedisTemplate strRedis;

    @Test
    void redisTestStringSet(){
        strRedis.opsForValue().set("RedisTest", "Test");
        strRedis.opsForValue().set("RedisTest中文", "中文测试");
        strRedis.opsForValue().set("RedisTest繁體測試", "繁體测试");
    }

    @Test
    void redisTestStringGet(){
        String redisTest = strRedis.opsForValue().get("RedisTest") + "====" + strRedis.opsForValue().get("RedisTest中文");
        System.out.println(redisTest);
    }

}
//    public JSONResult test() {
//        SysUser user = new SysUser();
//        user.setId("100111");
//        user.setUsername("spring boot");
//        user.setPassword("abc123");
//        user.setIsDelete(0);
//        user.setRegistTime(new Date());
//        strRedis.opsForValue().set("json:user", JsonUtils.objectToJson(user));
//
//        return JSONResult.ok(user);
//    }
