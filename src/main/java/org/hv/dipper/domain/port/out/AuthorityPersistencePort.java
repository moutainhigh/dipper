package org.hv.dipper.domain.port.out;

import java.sql.SQLException;

/**
 * @author wujianchuan
 */
public interface AuthorityPersistencePort {
    /**
     * 更新访问actionId所对应的动作需要的权限
     *
     * @param serverId 服务标识
     * @param bundleId bundle标识
     * @param actionId action标识
     * @param authId   authority标识
     * @throws SQLException sql e
     */
    void updateAuthForAction(String serverId, String bundleId, String actionId, String authId) throws SQLException;
}
