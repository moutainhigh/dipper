package org.hv.dipper.domain.port.in;

import java.sql.SQLException;

/**
 * @author wujianchuan
 */
public interface AdjustAuthority {
    /**
     * 为路由指定权限
     * 影响持久化权限映射数据
     * 影响内存中的权限映射数据
     *
     * @param serviceId 服务标识
     * @param bundleId  bundle标识
     * @param actionId  action标识
     * @param authId    authority标识
     * @throws NullPointerException 空
     */
    void putAuth(String serviceId, String bundleId, String actionId, String authId) throws NullPointerException;

    /**
     * 初始化(把数据库中的数据同步到内存中)内存中的权限映射数据
     * @throws SQLException sql e
     */
    void refreshHeapAuth() throws SQLException;
}
