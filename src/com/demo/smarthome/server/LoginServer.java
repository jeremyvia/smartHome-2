package com.demo.smarthome.server;

import com.demo.smarthome.service.Cfg;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by leishi on 15/10/26.
 *
 * 通过用户名密码登录到服务器,将设备列表储存到Cfg.devInfo静态变量中
 *
 */

public class LoginServer {


    public LoginServer() {
    }

    public static ServerReturnResult LoginServerMethod() {

        Gson gson = new Gson();
        ServerReturnResult loginResult = new ServerReturnResult();
        String jsonResult;

        loginResult.setCode(String.valueOf(Cfg.CODE_PWD_ERROR));
        if(Cfg.userName == null|| Cfg.userPassword ==null){
            return loginResult;
        }

        String[] paramsName = {"userName", "pwd"};
        String[] paramsValue = {Cfg.userName, Cfg.userPassword};
        String methodName = "login";

        setServerURL regiterUser = new setServerURL();

        if((jsonResult = regiterUser.sendParamToServer(methodName, paramsName, paramsValue)).isEmpty()){
            return loginResult;
        }
        try {
            loginResult = gson.fromJson(jsonResult
                    , com.demo.smarthome.server.ServerReturnResult.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if(loginResult.getRows().size() != 0) {
            Cfg.devInfo = loginResult.getRows().get(0).split(",");
            Cfg.devNumber = Cfg.devInfo.length;
        }else{
            Cfg.devInfo = null;
            Cfg.devNumber = 0;
        }
        return loginResult;
    }
}
