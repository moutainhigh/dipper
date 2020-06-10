package org.hv.dipper.domain.port.out;

import org.hv.dipper.domain.aggregation.AuthorityView;
import org.hv.dipper.domain.aggregation.BundleView;
import org.hv.dipper.domain.aggregation.UserAuthorityView;
import org.hv.biscuits.spine.model.Department;
import org.hv.biscuits.spine.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan
 */
public interface AuthorityLoadPort {

    /**
     * 获取所有的动作和权限的映射关系{@link AuthorityView}
     *
     * @return 动作和权限的映射关系
     * @throws SQLException sql e
     */
    List<AuthorityView> loadAllAuthorityView() throws SQLException;

    /**
     * 加载所有不需要权限就能访问的bundle
     *
     * @return bundleId list
     * @throws SQLException sql e
     */
    List<BundleView> loadFreeBundle() throws SQLException;

    /**
     * 查询用户
     *
     * @param avatar   用户名
     * @param password 密码
     * @return 用户 {@link User}
     * @throws SQLException sql e
     */
    User loadUserByAvatarAndPassword(String avatar, String password) throws SQLException;

    /**
     * 查询用户所有的权限(user->department->service->bundle->authority)
     *
     * @param userUuid 用户标识
     * @return 权限集合
     * @throws SQLException sql e
     */
    List<UserAuthorityView> loadAuthorityViewByUserUuid(String userUuid) throws SQLException;

    /**
     * 根据部门标识获取部门数据
     *
     * @param departmentUuid 部门数据标识
     * @return 部门
     * @throws SQLException sql e
     */
    Department loadDepartmentByUuid(String departmentUuid) throws SQLException;
}
