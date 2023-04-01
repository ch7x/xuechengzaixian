package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 课程计划service接口实现类
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;


    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //课程计划id
        Long teachplanDtoId = saveTeachplanDto.getId();
        //修改课程计划
        if (teachplanDtoId == null) {
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            //确定排序字段
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            //设置排序号
            teachplan.setOrderby(count);

            teachplanMapper.insert(teachplan);
        } else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanDtoId);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);

            teachplanMapper.updateById(teachplan);
        }
    }


    /**
     * 取最新的排序号
     *
     * @param courseId 课程id
     * @param parentId 父课程计划id
     * @return int 最新排序号
     */
    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper) + 1;
    }


    /**
     * 删除课程计划
     *
     * @param teachplanId 课程计划id
     */
    @Override
    @Transactional
    public void deleteTeachplan(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer grade = teachplan.getGrade();
        //大章
        if (grade == 1) {
            //查询大章下是否还有小章
            int count = getTeachplanCount(teachplan.getCourseId(), teachplanId);
            //还有小章
            if (count > 1) {
                XueChengPlusException.cast("本章节中还有小章节");
            } else {
                if (teachplanMapper.deleteById(teachplanId) <= 0) {
                    XueChengPlusException.cast("删除失败");
                }
            }
        }
        //小章
        else {
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, teachplanId);

            // 判断有没有绑定
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
            if (teachplanMedia != null) {
                if (teachplanMediaMapper.deleteById(teachplanMedia.getId()) <= 0) {
                    XueChengPlusException.cast("删除失败");
                }
            }

            //删除课程计划表和删除课程媒资表
            if (teachplanMapper.deleteById(teachplanId) <= 0) {
                XueChengPlusException.cast("删除失败");
            }

        }
    }

    /**
     * 上移课程
     *
     * @param teachplanId 课程id
     */
    @Transactional
    @Override
    public void moveUp(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();

        LambdaQueryWrapper<Teachplan> qw = new LambdaQueryWrapper<>();
        qw.eq(Teachplan::getCourseId, courseId);
        qw.eq(Teachplan::getParentid, parentid);
        qw.lt(Teachplan::getOrderby, teachplan.getOrderby());

        //找到比当前小的所有值
        List<Teachplan> teachplanList = teachplanMapper.selectList(qw);
        if (teachplanList.size() == 0) {
            XueChengPlusException.cast("无法继续向上移动");
        }

        teachplanList.sort(Comparator.comparing(Teachplan::getOrderby).reversed());

        Teachplan teachplan1 = teachplanList.get(0);
        swap(teachplan, teachplan1);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplan1);

    }


    @Transactional
    @Override
    public void removeAssociationMedia(Long teachplanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId, teachplanId).eq(TeachplanMedia::getMediaId, mediaId);
        teachplanMediaMapper.delete(queryWrapper);
    }

    /**
     * 下移课程
     *
     * @param teachplanId 课程id
     */
    @Transactional
    @Override
    public void moveDown(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();

        LambdaQueryWrapper<Teachplan> qw = new LambdaQueryWrapper<>();
        qw.eq(Teachplan::getCourseId, courseId);
        qw.eq(Teachplan::getParentid, parentid);
        qw.gt(Teachplan::getOrderby, teachplan.getOrderby());

        List<Teachplan> teachplanList = teachplanMapper.selectList(qw);
        if (teachplanList.size() == 0) {
            XueChengPlusException.cast("无法继续向下移动");
        }
        teachplanList.sort(Comparator.comparing(Teachplan::getOrderby));

        Teachplan teachplan1 = teachplanList.get(0);
        swap(teachplan, teachplan1);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplan1);

    }

    @Transactional
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 课程计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChengPlusException.cast("课程计划不存在");
        }

        // 先删除原有记录,根据课程计划id删除它绑定的媒资
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));

        // 再添加新记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    private void swap(Teachplan t1, Teachplan t2) {
        Integer temp = t1.getOrderby();
        t1.setOrderby(t2.getOrderby());
        t2.setOrderby(temp);
    }
}

