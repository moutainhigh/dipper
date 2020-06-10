package org.hv.dipper.adapters.api;

import org.hv.dipper.domain.port.in.AuthorityCheck;
import org.hv.biscuits.annotation.Action;
import org.hv.biscuits.annotation.Controller;
import org.hv.biscuits.controller.Body;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 供其他服务作鉴权(RPC)使用
 *
 * @author wujianchuan
 */
@Controller(bundleId = "authentication", auth = false)
public class AuthenticationApi {

    private final AuthorityCheck authorityCheck;

    public AuthenticationApi(AuthorityCheck authorityCheck) {
        this.authorityCheck = authorityCheck;
    }

    @Action(actionId = "identify")
    public Body identify(@RequestParam String serviceId, @RequestParam String bundleId, @RequestParam String actionId, @RequestParam String token) {
        return Body.success().data(authorityCheck.check(serviceId, bundleId, actionId, token));
    }

}
