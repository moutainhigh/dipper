package org.hv.dipper.adapters.persistence;

import org.hv.dipper.domain.aggregation.BundleView;
import org.hv.dipper.domain.port.out.AuthorityLoadPort;
import org.hv.dipper.domain.aggregation.AuthorityView;
import org.hv.dipper.domain.aggregation.UserAuthorityView;
import org.hv.biscuits.repository.AbstractRepository;
import org.hv.biscuits.spine.model.Department;
import org.hv.biscuits.spine.model.User;
import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.query.SQLQuery;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan
 */
@Component
public class AuthorityLoad extends AbstractRepository implements AuthorityLoadPort {

    @Override
    public List<AuthorityView> loadAllAuthorityView() throws SQLException {
        SQLQuery sqlQuery = this.getSession().createSQLQuery("SELECT " +
                "    T1.SERVER_ID as serviceId, " +
                "    T1.BUNDLE_ID as bundleId, " +
                "    T1.ACTION_ID as actionId, " +
                "    T1.AUTH_ID as authorityId " +
                "FROM T_MAPPER T1 ", AuthorityView.class);
        return sqlQuery.list();
    }

    @Override
    public List<BundleView> loadFreeBundle() throws SQLException {
        SQLQuery sqlQuery = this.getSession().createSQLQuery("SELECT T.SERVER_ID as serviceId, T.BUNDLE_ID as bundleId FROM T_BUNDLE T WHERE T.WITH_AUTH = 0",
                BundleView.class);
        return sqlQuery.list();
    }

    @Override
    public User loadUserByAvatarAndPassword(String avatar, String password) throws SQLException {
        Criteria criteria = this.getSession().createCriteria(User.class);
        criteria.add(Restrictions.equ("password", password))
                .add(Restrictions.equ("avatar", avatar));
        return criteria.unique();
    }

    @Override
    public List<UserAuthorityView> loadAuthorityViewByUserUuid(String userUuid) throws SQLException {
        SQLQuery sqlQuery = this.getSession().createSQLQuery("SELECT DISTINCT    " +
                " T2.DEPARTMENT_UUID departmentUuid,    " +
                " T0.SERVER_ID serviceId,    " +
                " T0.BUNDLE_ID bundleId,    " +
                " T0.ID authorityId     " +
                "FROM    " +
                " T_AUTHORITY T0    " +
                " LEFT JOIN T_ROLE_AUTH T1 ON T0.UUID = T1.AUTH_UUID    " +
                " LEFT JOIN T_ROLE T2 ON T1.ROLE_UUID = T2.UUID    " +
                " LEFT JOIN T_USER_ROLE T3 ON T2.UUID = T3.ROLE_UUID " +
                "WHERE " +
                "    t3.USER_UUID = :USER_UUID", UserAuthorityView.class);
        sqlQuery.setParameter("USER_UUID", userUuid);
        return sqlQuery.list();
    }

    @Override
    public Department loadDepartmentByUuid(String departmentUuid) throws SQLException {
        return this.getSession().findOne(Department.class, departmentUuid);
    }
}
