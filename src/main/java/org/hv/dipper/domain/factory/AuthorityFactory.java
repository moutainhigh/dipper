package org.hv.dipper.domain.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan
 */
public enum AuthorityFactory {
    /**
     * 权限映射结构
     */
    INSTANCE;
    /**
     * serviceId->bundleId->actionId->authId
     */
    private final Map<String, Map<String, Map<String, String>>> authTree = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> freeBundleTree = new HashMap<>(16);

    /**
     * 填充/修改 权限映射结构
     *
     * @param serverId 服务标识
     * @param bundleId bundle标识
     * @param actionId action标识
     * @param authId   authority标识
     */
    public void putActionAuthBranch(String serverId, String bundleId, String actionId, String authId) {
        if (authId != null) {
            this.authTree.putIfAbsent(serverId, new ConcurrentHashMap<>());
            Map<String, Map<String, String>> heapBundleToActionAuthBranch = this.authTree.get(serverId);
            heapBundleToActionAuthBranch.putIfAbsent(bundleId, new ConcurrentHashMap<>());
            Map<String, String> heapActionAuthBranch = heapBundleToActionAuthBranch.get(bundleId);
            heapActionAuthBranch.put(actionId, authId);
        }
    }

    /**
     * 设置无需权限就可以访问的bundle
     *
     * @param serverId server id
     * @param bundleId bundle id
     */
    public void putFreeBundle(String serverId, String bundleId) {
        this.freeBundleTree.putIfAbsent(serverId, new TreeSet<>());
        Set<String> heapFreeBundles = this.freeBundleTree.get(serverId);
        heapFreeBundles.add(bundleId);
    }

    /**
     * 判断访问该bundle时是否需要相应的权限
     *
     * @param serverId server id
     * @param bundleId bundle id
     * @return 是否可自由访问
     */
    public boolean isFree(String serverId, String bundleId) {
        return this.freeBundleTree.getOrDefault(serverId, new TreeSet<>()).contains(bundleId);
    }

    /**
     * 获取可访问actionId所指向的动作所需要的authority标识
     *
     * @param serviceId 服务标识
     * @param bundleId  bundle标识
     * @param actionId  action标识
     * @return authority标识
     */
    public String getAuthId(String serviceId, String bundleId, String actionId) {
        return authTree.getOrDefault(serviceId, new HashMap<>(0)).getOrDefault(bundleId, new HashMap<>(0)).get(actionId);
    }
}
