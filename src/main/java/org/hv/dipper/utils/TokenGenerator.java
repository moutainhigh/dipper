package org.hv.dipper.utils;

import org.hv.dipper.domain.aggregation.UserView;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan
 */
public class TokenGenerator {

    public static final String CLAIM_KEY_USER_AVATAR = "userAvatar";
    public static final String CLAIM_KEY_DEPARTMENT_UUID = "departmentUuid";
    public static final String CLAIM_KEY_WORK_DEPARTMENT_UUID = "workDepartmentUuid";

    public static String generateToken(UserView userView, String secret) {
        Map<String, Object> claims = new HashMap<>(7);
        claims.put(CLAIM_KEY_USER_AVATAR, userView.getAvatar());
        claims.put(CLAIM_KEY_DEPARTMENT_UUID, userView.getDepartmentUuid());
        claims.put(CLAIM_KEY_WORK_DEPARTMENT_UUID, userView.getBusinessDepartmentUuid());
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public static Claims getClaimsFromToken(String token, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
