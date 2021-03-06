package com.orange.demo.app.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.orange.demo.app.model.constant.ClassLevel;
import com.orange.demo.common.core.annotation.RelationDict;
import com.orange.demo.common.core.annotation.RelationConstDict;
import com.orange.demo.common.core.annotation.DeletedFlagColumn;
import com.orange.demo.common.core.validator.UpdateGroup;
import com.orange.demo.common.core.validator.ConstDictRef;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.*;

import java.util.Date;
import java.util.Map;

/**
 * StudentClass实体对象。
 *
 * @author Jerry
 * @date 2020-09-24
 */
@ApiModel("StudentClass实体对象")
@Data
@Table(name = "zz_class")
public class StudentClass {

    /**
     * 班级Id。
     */
    @ApiModelProperty(value = "班级Id", required = true)
    @NotNull(message = "数据验证失败，班级Id不能为空！", groups = {UpdateGroup.class})
    @Id
    @Column(name = "class_id")
    private Long classId;

    /**
     * 班级名称。
     */
    @ApiModelProperty(value = "班级名称", required = true)
    @NotBlank(message = "数据验证失败，班级名称不能为空！")
    @Column(name = "class_name")
    private String className;

    /**
     * 学校Id。
     */
    @ApiModelProperty(value = "学校Id", required = true)
    @NotNull(message = "数据验证失败，所属校区不能为空！")
    @Column(name = "school_id")
    private Long schoolId;

    /**
     * 学生班长Id。
     */
    @ApiModelProperty(value = "学生班长Id", required = true)
    @NotNull(message = "数据验证失败，学生班长不能为空！")
    @Column(name = "leader_id")
    private Long leaderId;

    /**
     * 已完成课时数量。
     */
    @ApiModelProperty(value = "已完成课时数量", required = true)
    @NotNull(message = "数据验证失败，已完成课时不能为空！", groups = {UpdateGroup.class})
    @Column(name = "finish_class_hour")
    private Integer finishClassHour;

    /**
     * 班级级别(0: 初级班 1: 培优班 2: 冲刺提分班 3: 竞赛班)。
     */
    @ApiModelProperty(value = "班级级别(0: 初级班 1: 培优班 2: 冲刺提分班 3: 竞赛班)", required = true)
    @NotNull(message = "数据验证失败，班级级别不能为空！")
    @ConstDictRef(constDictClass = ClassLevel.class, message = "数据验证失败，班级级别为无效值！")
    @Column(name = "class_level")
    private Integer classLevel;

    /**
     * 创建用户。
     */
    @ApiModelProperty(value = "创建用户")
    @Column(name = "create_user_id")
    private Long createUserId;

    /**
     * 班级创建时间。
     */
    @ApiModelProperty(value = "班级创建时间")
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 逻辑删除标记字段(1: 正常 -1: 已删除)。
     */
    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
    @DeletedFlagColumn
    private Integer status;

    @ApiModelProperty(hidden = true)
    @RelationDict(
            masterIdField = "schoolId",
            slaveServiceName = "schoolInfoService",
            slaveModelClass = SchoolInfo.class,
            slaveIdField = "schoolId",
            slaveNameField = "schoolName")
    @Transient
    private Map<String, Object> schoolIdDictMap;

    @ApiModelProperty(hidden = true)
    @RelationDict(
            masterIdField = "leaderId",
            slaveServiceName = "studentService",
            slaveModelClass = Student.class,
            slaveIdField = "studentId",
            slaveNameField = "studentName")
    @Transient
    private Map<String, Object> leaderIdDictMap;

    @ApiModelProperty(hidden = true)
    @RelationConstDict(
            masterIdField = "classLevel",
            constantDictClass = ClassLevel.class)
    @Transient
    private Map<String, Object> classLevelDictMap;
}
