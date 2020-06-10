package org.hv.dipper.adapters.web;

import org.hv.dipper.domain.port.in.AuthorityCheck;
import org.hv.dipper.domain.aggregation.Session;
import org.hv.dipper.utils.EncodeUtil;
import org.hv.biscuits.annotation.Action;
import org.hv.biscuits.annotation.Controller;
import org.hv.biscuits.controller.Body;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.SQLException;
import java.util.Map;

/**
 * 供web端登录使用
 *
 * @author wujianchuan
 */
@Controller(bundleId = "authentication", auth = false)
public class GuestController {

    private final AuthorityCheck authorityCheck;

    public GuestController(AuthorityCheck authorityCheck) {
        this.authorityCheck = authorityCheck;
    }

    @Action(actionId = "login", method = RequestMethod.POST, responseEncrypt = true)
    public Body login(@RequestBody Map<String, String> userInfo) throws SQLException {
        //TODO 各个适配器都应该有自己的加密方式（加解密不下放到领域模型中）
        String avatar = userInfo.get("userName");
        String password = userInfo.get("password");
        String departmentUuid = userInfo.get("departmentUuid");
        Session session = authorityCheck.login(avatar, EncodeUtil.abcEncoder(password), departmentUuid);
        return Body.success().message(String.format("%s 登录成功", session.getUserView().getName())).data(session);
    }

    @Action(actionId = "logout")
    public Body logout(@RequestHeader(name = "Authorization") String token) {
        authorityCheck.logout(token);
        return Body.success();
    }
}
