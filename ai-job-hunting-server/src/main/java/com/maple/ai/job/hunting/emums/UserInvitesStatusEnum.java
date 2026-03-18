package com.maple.ai.job.hunting.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/5/10 11:35
 * Description:
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum UserInvitesStatusEnum {

    /**
     *
     */
    NULL,
    NORMAL(1, "正常"),
    ARCHIVE(2, "归档"),
    ;


    private Integer code;

    private String desc;


    public static UserInvitesStatusEnum getByDesc(String desc) {
        for (UserInvitesStatusEnum fileType : UserInvitesStatusEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return UserInvitesStatusEnum.NULL;
    }

    public static UserInvitesStatusEnum getByCode(Integer code) {
        for (UserInvitesStatusEnum fileType : UserInvitesStatusEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return UserInvitesStatusEnum.NULL;
    }
}
