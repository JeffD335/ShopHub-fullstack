package com.shopHub.utils;

import cn.hutool.core.util.StrUtil;

/**
 * @author 虎哥
 */
public class RegexUtils {
    /**
     * Check if phone number format is invalid
     * @param phone Phone number to validate
     * @return true: valid, false: invalid
     */
    public static boolean isPhoneInvalid(String phone){
        return mismatch(phone, RegexPatterns.PHONE_REGEX);
    }
    /**
     * Check if email format is invalid
     * @param email Email to validate
     * @return true: valid, false: invalid
     */
    public static boolean isEmailInvalid(String email){
        return mismatch(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * Check if verification code format is invalid
     * @param code Verification code to validate
     * @return true: valid, false: invalid
     */
    public static boolean isCodeInvalid(String code){
        return mismatch(code, RegexPatterns.VERIFY_CODE_REGEX);
    }

    // Check if string does not match regex pattern
    private static boolean mismatch(String str, String regex){
        if (StrUtil.isBlank(str)) {
            return true;
        }
        return !str.matches(regex);
    }
}
