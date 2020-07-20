package org.hv.dipper.utils;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wujianchuan
 */
public class EncodeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodeUtil.class);

    public static String encoderByMd5(String code) throws NoSuchAlgorithmException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return Base64.encodeBase64String(md5.digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    public static String abcEncoder(String code) throws NoSuchAlgorithmException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(code.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }
}
