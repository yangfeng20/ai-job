package com.maple.ai.job.hunting.consts;

import java.util.regex.Pattern;

/**
 * @author maple
 * Created Date: 2024/5/13 16:14
 * Description:
 */

public class RePatternConstant {

    // 编译正则表达式
    public static final Pattern PHONE_PATTERN = Pattern.compile("(?:电话|phone|tel|联系方式|联系电话|(?:\\s+))(?: |.)(1[34578][0-9]\\d{8})");

    public static final Pattern EMAIL_PATTERN = Pattern.compile("(?:邮箱|mail|(?:\\s+))(?: |.|：|)(\\w+@\\w+\\.[a-z]+)");
}
