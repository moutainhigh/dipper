package org.hv.dipper.domain.port.in;

import org.hv.dipper.domain.aggregation.Session;
import org.hv.dipper.domain.aggregation.UserView;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author wujianchuan
 */
public interface AuthorityCheck {

    /**
     * 用户登录
     * 验证信息是否合法并
     * 使用会话工厂创建会话
     *
     * @param avatar             用户名
     * @param password           密码
     * @param workDepartmentUuid 以该科室的身份进行登录
     * @return 用户会话
     * @throws SQLException sql e
     */
    Session login(@NotNull String avatar, @NotNull String password, @NotNull String workDepartmentUuid) throws SQLException;

    /**
     * 切换工作部门
     *
     * @param avatar             用户
     * @param workDepartmentUuid 部门标识
     * @return 用户会话
     */
    Session switchBusinessDepartment(@NotNull String avatar, @NotNull String workDepartmentUuid) throws SQLException;

    /**
     * 注销登录
     * 销毁会话
     *
     * @param token 令牌
     */
    void logout(String token);

    /**
     * 检测令牌是否有效
     * 验证用户是否可访问actionId指向的动作
     *
     * @param serviceId 服务标识
     * @param bundleId  bundle标识
     * @param actionId  action标识
     * @param token     令牌: 包含用户信息和业务信息——部门数据标识（用户以该部门的身份来访问，若为空则部根据部门标识过滤）
     * @return 令牌是否已过期 {expired}，用户是否可访问actionId指向的动作 {allowed}，用户昵称 {avatar}, 错误提示信息 {errorMessage}
     * @throws NullPointerException 空
     */
    Map<String, Object> check(String serviceId, String bundleId, String actionId, String token) throws NullPointerException;

    /**
     * 从令牌中解析出用户信息
     *
     * @param token 令牌
     * @return 用户信息
     */
    UserView parseUserView(String token);

    /**
     * 检测令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    boolean checkExpiation(String token);

    /**
     * 判断指定action是否不需要权限
     *
     * @param serviceId 服务
     * @param bundleId  bundle
     * @param actionId  action
     * @return 是否不需要权限
     */
    boolean freeAction(String serviceId, String bundleId, String actionId);
}
