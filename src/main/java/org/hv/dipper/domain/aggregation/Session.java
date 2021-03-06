package org.hv.dipper.domain.aggregation;

import org.hv.biscuits.spine.viewmodel.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wujianchuan
 */
public class Session implements Serializable {
    private static final long serialVersionUID = 487195561134671815L;
    private final Logger logger = LoggerFactory.getLogger(Session.class);
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private UserView userView;
    private String token;
    private long expirationTime;
    private long lifeLength;
    private long rebirthPeriod;
    private long rebirthTimePeriphery;

    public Session() {
    }

    private Session(String token, UserView userView, long lifeLength, long rebirthPeriod) {
        this.token = token;
        this.userView = userView;
        this.lifeLength = lifeLength;
        this.expirationTime = System.currentTimeMillis() + lifeLength;
        this.rebirthPeriod = rebirthPeriod;
        this.rebirthTimePeriphery = expirationTime - rebirthPeriod;
        logger.info("======================= 令牌超时时间为: {} 续时截点为：{} =======================", dateFormat.format(new Date(expirationTime)), dateFormat.format(new Date(rebirthTimePeriphery)));
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
        return new Session(token, userView, lifeLength, rebirthPeriod);
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
            logger.info("======================================= 令牌已超时: {} =======================================", dateFormat.format(new Date(expirationTime)));
            return true;
        } else {
            if (currentTime > rebirthTimePeriphery) {
                expirationTime = currentTime + lifeLength;
                rebirthTimePeriphery = currentTime + rebirthPeriod;
                logger.info("======================= 令牌超时时间更新为: {} 续时截点为：{} =======================", dateFormat.format(new Date(expirationTime)), dateFormat.format(new Date(rebirthTimePeriphery)));
            }
            return false;
        }
    }

    public void setUserView(UserView userView) {
        this.userView = userView;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setLifeLength(long lifeLength) {
        this.lifeLength = lifeLength;
    }

    public void setRebirthTimePeriphery(long rebirthTimePeriphery) {
        this.rebirthTimePeriphery = rebirthTimePeriphery;
    }

    public UserView getUserView() {
        return userView;
    }

    public String getToken() {
        return token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public long getLifeLength() {
        return lifeLength;
    }

    public long getRebirthTimePeriphery() {
        return rebirthTimePeriphery;
    }
}
