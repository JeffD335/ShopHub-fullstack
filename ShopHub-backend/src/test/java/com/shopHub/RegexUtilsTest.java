package com.shopHub;

import com.shopHub.utils.RegexUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexUtilsTest {

    @Test
    void validatesPhoneNumbers() {
        assertFalse(RegexUtils.isPhoneInvalid("13686869696"));

        assertTrue(RegexUtils.isPhoneInvalid("123456"));
        assertTrue(RegexUtils.isPhoneInvalid(""));
        assertTrue(RegexUtils.isPhoneInvalid(null));
    }

    @Test
    void validatesVerificationCodes() {
        assertFalse(RegexUtils.isCodeInvalid("A1b2C3"));

        assertTrue(RegexUtils.isCodeInvalid("12345"));
        assertTrue(RegexUtils.isCodeInvalid("1234567"));
    }
}
