package org.hv.dipper.adapters.persistence;

import org.hv.dipper.domain.port.out.AuthorityPersistencePort;
import org.hv.biscuits.repository.AbstractRepository;
import org.hv.biscuits.spine.model.Mapper;
import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.criteria.Modern;
import org.hv.pocket.criteria.Restrictions;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * @author wujianchuan
 */
@Component
public class AuthorityPersistence extends AbstractRepository implements AuthorityPersistencePort {
    @Override
    public void updateAuthForAction(String serviceId, String bundleId, String actionId, String authId) throws SQLException {
        Criteria criteria = this.getSession().createCriteria(Mapper.class);
        criteria.add(Restrictions.equ("serviceId", serviceId))
                .add(Restrictions.equ("bundleId", bundleId))
                .add(Restrictions.equ("actionId", actionId))
                .add(Modern.set("authId", authId))
                .update();
    }
}
