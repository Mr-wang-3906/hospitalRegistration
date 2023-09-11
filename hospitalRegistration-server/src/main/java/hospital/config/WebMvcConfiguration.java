package hospital.config;

import hospital.intercepter.JwtTokenDoctorInterceptor;
import hospital.intercepter.JwtTokenPatientInterceptor;
import hospital.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenDoctorInterceptor JwtTokenDoctorInterceptor;

    @Autowired
    private JwtTokenPatientInterceptor JwtTokenPatientInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(JwtTokenDoctorInterceptor)
                .addPathPatterns("/doctor/**")
                .excludePathPatterns("/login");
//                .excludePathPatterns("/doctor/registerOther");

        registry.addInterceptor(JwtTokenPatientInterceptor)
                .addPathPatterns("/patient/**")
                .excludePathPatterns("/login")
                .excludePathPatterns("/patient/register");
    }


    /**
     * 通过knife4j生成接口文档
     *
     * @return
     */
    @Bean
    public Docket docketAdmin() {
        log.info("准备生成接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("医院挂号统接口文档") //标题
                .version("1.0") //版本
                .description("医院挂号统接口文档") //简介
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("xx医院挂号系统")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("hospital.controller")) //指定接口文档需要扫描的包
                .paths(PathSelectors.any())
                .build();
        return docket;
    }


    /**
     * 设置静态资源映射
     *
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) { //和接口文档配合使用.
        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 拓展springMVC框架的消息转换器
     *
     * @param messageConverters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        log.info("扩展消息转换器..");
        //创建一个消息转化器
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //为消息转化器设置一个对象转化器，将Java对象序列化为JSON数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转换器加入容器中
        messageConverters.add(0, converter);
    }

}
