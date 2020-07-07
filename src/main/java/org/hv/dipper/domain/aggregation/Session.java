package org.hv.dipper.domain.aggregation;

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
        logger.info("======================================= 令牌超时时间为: {} =======================================", dateFormat.format(new Date(expirationTime)));
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
            logger.info("======================================= 令牌已超时: {} =======================================", dateFormat.format(new Date(expirationTime)));
            return true;
        } else {
            if (currentTime > rebirthTimePeriphery) {
                expirationTime = currentTime + lifeLength;
                rebirthTimePeriphery = rebirthTimePeriphery + lifeLength;
                logger.info("======================================= 令牌超时时间更新为: {} =======================================", dateFormat.format(new Date(expirationTime)));
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
