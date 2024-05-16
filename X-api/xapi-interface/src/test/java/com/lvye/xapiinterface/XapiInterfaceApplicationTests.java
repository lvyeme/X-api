package com.lvye.xapiinterface;

import com.lvye.xapiclientsdk.client.XApiClient;
import com.lvye.xapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class XapiInterfaceApplicationTests {

    @Resource
    private XApiClient xApiClient;
    @Test
    void contextLoads() {
        String lvye = xApiClient.getNameByGet("lvye");
        String lvye1 = xApiClient.getNameByPost("lvye");
        User user = new User();
        user.setUsername("lvye");
        xApiClient.getUserNameByPost(user);
        System.out.println(lvye);
        System.out.println(lvye1);

        // 调用yuApiClient的getNameByGet方法，并传入参数"yupi"，将返回的结果赋值给result变量
        // 创建一个User对象
        //User user = new User();
        // 设置User对象的username属性为"liyupi"
        // 调用yuApiClient的getUserNameByPost方法，并传入user对象作为参数，将返回的结果赋值给usernameByPost变量
        String usernameByPost = xApiClient.getUserNameByPost(user);
        // 打印result变量的值
        // 打印usernameByPost变量的值
        System.out.println(usernameByPost);
    }

}
