package org.hv.dipper.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author
 */
public class EncodeUtil {

    public static String encoderByMd5(String code) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return Base64.encodeBase64String(md5.digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static String abcEncoder(String code) {
        Object var2 = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(code.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
            return null;
        }
    }
}
