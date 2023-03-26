package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 *  课程计划树型结构dto
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    //小章节list
    List<TeachplanDto> teachPlanTreeNodes;

}
