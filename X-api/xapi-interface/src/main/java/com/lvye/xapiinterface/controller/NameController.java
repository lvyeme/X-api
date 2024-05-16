package com.lvye.xapiinterface.controller;

import com.lvye.xapiclientsdk.model.User;
import com.lvye.xapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * @author LVye
 * @version 1.0
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/")
    public String getNameByGet(String name) {

        return "GET 你的名字是" + name;
    }

    @PostMapping("/")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");

        if (!accessKey.equals("yun")){
            throw new RuntimeException("参数错误");
        }
        if (Long.parseLong(nonce) > 100000){
            throw new RuntimeException("参数错误");
        }
        //todo 时间和当前时间不能超过 5 分钟
        String serverSign = SignUtils.getSign(body, "abcdefgh");
        if (!sign.equals(serverSign)){
            throw new RuntimeException("参数错误");
        }
        return "POST 用户名字是" + user.getUsername();
    }
}
