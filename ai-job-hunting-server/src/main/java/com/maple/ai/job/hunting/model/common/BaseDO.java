package com.maple.ai.job.hunting.model.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 杨锋
 * @since 2022/6/25 19:45
 * desc:
 */

@Getter
@Setter
@ToString
public class BaseDO extends UnIdBaseDO {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
}
