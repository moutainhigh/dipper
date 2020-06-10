package org.hv.dipper.domain.aggregation;

import java.io.Serializable;

/**
 * @author wujianchuan
 */
public class Session implements Serializable {
    private static final long serialVersionUID = 487195561134671815L;
    private final UserView userView;
    private final String token;
    private long expirationTime;
    private final long lifeLength;
    private long rebirthTimePeriphery;

    private Session(String token, UserView userView, long expirationTime, long lifeLength, long rebirthTimePeriphery) {
        this.token = token;
        this.userView = userView;
        this.expirationTime = expirationTime;
        this.lifeLength = lifeLength;
        this.rebirthTimePeriphery = rebirthTimePeriphery;
    }

    /**
     * 创建会话
     *
     * @param token         令牌
     * @param userView      用户信息
     * @param lifeLength    令牌寿命
     * @param rebirthPeriod 令牌过期前可刷新令牌的时间段
     * @return 用户会话
     */
    public static Session newInstance(String token, UserView userView, long lifeLength, long rebirthPeriod) {
        long expirationTime = System.currentTimeMillis() + lifeLength;
        return new Session(token, userView, expirationTime, lifeLength, expirationTime - rebirthPeriod);
    }

    /**
     * 检测当前会话是否过期
     * 到达刷新寿命临界点时则延长会话寿命
     *
     * @return 会话时候过期
     */
    public boolean checkExpiation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > expirationTime) {
            return true;
        } else {
            if (currentTime > rebirthTimePeriphery) {
                expirationTime = currentTime + lifeLength;
                rebirthTimePeriphery = rebirthTimePeriphery + lifeLength;
            }
            return false;
        }
    }

    public UserView getUserView() {
        return userView;
    }

    public String getToken() {
        return token;
    }
}
