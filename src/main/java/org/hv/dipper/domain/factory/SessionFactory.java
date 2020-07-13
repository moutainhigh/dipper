package org.hv.dipper.domain.factory;

import org.hv.biscuits.spine.viewmodel.UserView;
import org.hv.dipper.domain.aggregation.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan
 */
public enum SessionFactory {
    /**
     * 获取用户会话
     */
    INSTANCE;
    private final Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * 创建会话
     *
     * @param token         令牌
     * @param userView      用户信息
     * @param lifeLength    会话寿命
     * @param rebirthPeriod 会话过期前可刷新会话的时间段长度
     */
    public void register(String token, UserView userView, long lifeLength, long rebirthPeriod) {
        Session session = Session.newInstance(token, userView, lifeLength, rebirthPeriod);
        sessionPool.put(token, session);
    }

    /**
     * 获取会话
     *
     * @param token 令牌
     * @return 会话
     */
    public Session getSession(String token) {
        return sessionPool.get(token);
    }

    /**
     * 销毁会话
     *
     * @param token 令牌
     */
    public void expel(String token) {
        sessionPool.remove(token);
    }
}
