package com.maple.ai.job.hunting.model.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author maple
 * Created Date: 2024/5/21 20:35
 * Description:
 */

@Getter
@Setter
@ToString
public class UnIdBaseDO implements Serializable {

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdDate;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedId;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedDate;

    /**
     * 数据状态
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic(value = "1", delval = "0")
    private Boolean isActive;
}
