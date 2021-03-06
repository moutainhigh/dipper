package org.hv.dipper.domain.workunit;

import org.hv.biscuits.annotation.Service;
import org.hv.biscuits.aspect.ServiceAspect;
import org.hv.biscuits.service.AbstractService;
import org.hv.biscuits.spine.model.Department;
import org.hv.biscuits.spine.model.User;
import org.hv.biscuits.spine.viewmodel.UserView;
import org.hv.dipper.config.TokenConfig;
import org.hv.dipper.domain.aggregation.AuthorityView;
import org.hv.dipper.domain.aggregation.BundleView;
import org.hv.dipper.domain.aggregation.Session;
import org.hv.dipper.domain.factory.AuthorityFactory;
import org.hv.dipper.domain.factory.SessionFactory;
import org.hv.dipper.domain.port.in.AdjustAuthority;
import org.hv.dipper.domain.port.in.AuthorityCheck;
import org.hv.dipper.domain.port.out.AuthorityLoadPort;
import org.hv.dipper.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan
 */
@Order
@Service(session = "authentication")
public class AuthorityService extends AbstractService implements AdjustAuthority, AuthorityCheck, CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(AuthorityService.class);
    private final AuthorityLoadPort authorityLoadPort;
    private final TokenConfig tokenConfig;
    private final AuthorityFactory authorityFactory = AuthorityFactory.INSTANCE;

    public AuthorityService(AuthorityLoadPort authorityLoadPort, TokenConfig tokenConfig) {
        this.authorityLoadPort = authorityLoadPort;
        this.tokenConfig = tokenConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        this.refreshHeapAuth();
    }

    @Override
    public void putAuth(String serviceId, String bundleId, String actionId, String authId) throws NullPointerException {
        String heapAuthId = authorityFactory.getAuthId(serviceId, bundleId, actionId);
        if (heapAuthId == null || heapAuthId.equals(authId)) {
            throw new IllegalArgumentException("非法操作");
        } else {
            // TODO 更新关联逻辑
        }
    }

    @Override
    @ConditionalOnBean(ServiceAspect.class)
    public void refreshHeapAuth() throws SQLException {
        List<AuthorityView> authorityViews = authorityLoadPort.loadAllAuthorityView();
        for (AuthorityView authorityView : authorityViews) {
            authorityFactory.putActionAuthBranch(authorityView.getServiceId(), authorityView.getBundleId(), authorityView.getActionId(), authorityView.getAuthorityId());
        }
        List<BundleView> bundleViews = authorityLoadPort.loadFreeBundle();
        for (BundleView bundleView : bundleViews) {
            authorityFactory.putFreeBundle(bundleView.getServiceId(), bundleView.getBundleId());
        }
    }

    @Override
    public Session login(@NotNull final String avatar, @NotNull final String password, String businessDepartmentUuid) throws SQLException {
        User user = authorityLoadPort.loadUserByAvatarAndPassword(avatar, password);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (businessDepartmentUuid == null) {
            businessDepartmentUuid = user.getDepartmentUuid();
        }
        Department department = authorityLoadPort.loadDepartmentByUuid(businessDepartmentUuid);
        UserView userView = UserView.fromUser(user)
                .setAuthorities(authorityLoadPort.loadAuthorityViewByUserUuid(user.getUuid()))
                .setBusinessDepartmentUuid(department.getUuid())
                .setBusinessDepartmentName(department.getName())
                .setStationUuid(department.getStationUuid())
                .setStationCode(department.getStationCode());
        String token = TokenGenerator.generateToken(userView, tokenConfig.getSecret());
        SessionFactory.INSTANCE.register(token, userView, tokenConfig.getExpiration(), tokenConfig.getRefreshTime());
        return SessionFactory.INSTANCE.getSession(token);
    }

    @Override
    public Session switchBusinessDepartment(@NotNull String token, @NotNull String businessDepartmentUuid) throws SQLException {
        Session session = SessionFactory.INSTANCE.getSession(token);
        if (session != null) {
            Department department = authorityLoadPort.loadDepartmentByUuid(businessDepartmentUuid);
            if (department != null) {
                session.getUserView()
                        .setBusinessDepartmentUuid(department.getUuid())
                        .setBusinessDepartmentName(department.getName())
                        .setStationUuid(department.getStationUuid())
                        .setStationCode(department.getStationCode());
            } else {
                logger.warn("无效的部门标识：{}", businessDepartmentUuid);
            }
        } else {
            logger.warn("非法访问，token不合法：{}", token);
        }
        return session;
    }

    @Override
    public void logout(String token) {
        SessionFactory.INSTANCE.expel(token);
    }

    @Override
    public Map<String, Object> check(String serviceId, String bundleId, String actionId, String token) throws NullPointerException {
        Map<String, Object> result = new HashMap<>(8);
        Session session = SessionFactory.INSTANCE.getSession(token);
        if (session == null) {
            result.put("expired", true);
            result.put("errorMessage", "非法访问");
            return result;
        }
        boolean expired = session.checkExpiation();
        result.put("expired", expired);
        if (expired) {
            result.put("errorMessage", "会话已过期");
        } else {
            UserView userView = session.getUserView();
            result.put("userUuid", userView.getUuid());
            result.put("avatar", userView.getAvatar());
            result.put("name", userView.getName());
            result.put("departmentUuid", userView.getDepartmentUuid());
            result.put("departmentName", userView.getDepartmentName());
            result.put("businessDepartmentUuid", userView.getBusinessDepartmentUuid());
            result.put("businessDepartmentName", userView.getBusinessDepartmentName());
            result.put("stationUuid", userView.getStationUuid());
            result.put("stationCode", userView.getStationCode());
            String authorityId = authorityFactory.getAuthId(serviceId, bundleId, actionId);
            if (authorityFactory.isFree(serviceId, bundleId) || authorityId == null) {
                result.put("allowed", true);
            } else {
                boolean allowed;
                allowed = userView.getAuthIds().contains(authorityId);
                result.put("allowed", allowed);
                result.put("errorMessage", allowed ? "通过" : String.format("没有权限访问 /%s/%s/%s", serviceId.toLowerCase(), bundleId, actionId));
            }
        }
        return result;
    }

    @Override
    public UserView parseUserView(String token) {
        Session session = SessionFactory.INSTANCE.getSession(token);
        if (session != null && !session.checkExpiation()) {
            return session.getUserView();
        }
        return null;
    }

    @Override
    public boolean checkExpiation(String token) {
        Session session = SessionFactory.INSTANCE.getSession(token);
        if (session == null) {
            return true;
        }
        return session.checkExpiation();
    }

    @Override
    public boolean freeAction(String serviceId, String bundleId, String actionId) {
        return authorityFactory.isFree(serviceId.toUpperCase(), bundleId) || authorityFactory.getAuthId(serviceId.toUpperCase(), bundleId, actionId) == null;
    }
}
