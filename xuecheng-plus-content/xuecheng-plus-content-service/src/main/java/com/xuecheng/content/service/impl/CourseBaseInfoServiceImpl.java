package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto courseParamsDto) {

        //拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()),
                CourseBase::getName,
                courseParamsDto.getCourseName());

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                courseParamsDto.getAuditStatus());

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()),
                CourseBase::getStatus,
                courseParamsDto.getPublishStatus());

        // 根据培训机构id拼装查询条件
        queryWrapper.eq(CourseBase::getCompanyId,companyId);

        // page对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> items = pageResult.getRecords();
        long total = pageResult.getTotal();

        // 返回结果

        return new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
    }


    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //参数的合法性校验
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {

            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }

        //向课程基本表course_base中写入数据
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象-属性对象名称一致时就可以拷贝
        BeanUtils.copyProperties(dto, courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());

        if (courseBaseMapper.insert(courseBaseNew) <= 0) {
            throw new XueChengPlusException("新增课程基本信息失败");
        }

        //向课程营销表course_market中写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        //课程id
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(dto, courseMarketNew);
        //保存营销信息
        courseMarketNew.setId(courseId);

        if (saveCourseMarket(courseMarketNew) <= 0) {
            throw new XueChengPlusException("保存课程营销信息失败");
        }

        //从数据查询课程详细信息
        return getCourseBaseInfo(courseId);
    }

    //保存课程营销信息，存在则更新，不存在就添加
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        //收费规则
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new XueChengPlusException("收费规则没有选择");
        }
        //收费规则为收费，价格校验
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0 || courseMarketNew.getOriginalPrice() < 0) {
                throw new XueChengPlusException("课程为收费价格不能为空且必须大于0");
            }
        }

        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());

        if (courseMarketObj == null) {
            return courseMarketMapper.insert(courseMarketNew);
        } else {
            //将新增数据的拷贝到数据库中查出来的旧数据
            BeanUtils.copyProperties(courseMarketNew, courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        //从课程基本表中查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        //从课程营销表中查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //组装
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());

        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        //先获取课程id
        Long courseId = dto.getId();
        //查询课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        //数据合法性校验
        //本机构只能修改本机构的课程
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("不能允许修改其他机构的课程");
        }

        //封装课程基本数据
        BeanUtils.copyProperties(dto, courseBase);
        //修改时间
        courseBase.setChangeDate(LocalDateTime.now());

        //封装课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);

        if (courseBaseMapper.updateById(courseBase) <= 0 || saveCourseMarket(courseMarket) <= 0) {
            XueChengPlusException.cast("修改课程失败");
        }

        return getCourseBaseInfo(courseId);
    }

    @Transactional
    @Override
    public void deleteCourseBase(Long companyId, Long courseId) {

        LambdaQueryWrapper<Teachplan> qw = new LambdaQueryWrapper<>();
        qw.eq(Teachplan::getCourseId, courseId);

        LambdaQueryWrapper<CourseTeacher> qw1 = new LambdaQueryWrapper<>();
        qw1.eq(CourseTeacher::getCourseId, courseId);


        courseBaseMapper.deleteById(courseId);
        courseMarketMapper.deleteById(courseId);
        teachplanMapper.delete(qw);
        courseTeacherMapper.delete(qw1);

    }

}
