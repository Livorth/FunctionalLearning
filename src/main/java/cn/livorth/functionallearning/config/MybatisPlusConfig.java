package cn.livorth.functionallearning.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: FunctionalLearning
 * @description: MybatisPlus配置
 * @author: livorth
 * @create: 2021-10-02 17:50
 **/
@Configuration
@MapperScan("cn.livorth.functionallearning.dao")
public class MybatisPlusConfig {

    /**
     * 新版
     * 这里主要是在MybatisPlus的拦截器中添加page的拦截器，相较于老版泛用性更高
     * 实际使用上还是差不多的，然后记得在新建PaginationInnerInterceptor的时候注意自己是使用的哪个数据库
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 添加一个单页最大数量，-1则不受限制
        paginationInterceptor.setMaxLimit(100L);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}
