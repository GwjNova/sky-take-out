package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Resource
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        //分类状态默认设置为0
        category.setStatus(0);
        //设置创建时间、修改时间、创建人、修改人
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        //mybatis-plus插入
        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        long total = page.getTotal();
        List<Category> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 根据id删除分类
     *
     * @param id
     */
    @Override
    public void delById(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        QueryWrapper<Dish> queryDish = new QueryWrapper<>();
        queryDish.eq("category_id", id);
        Integer count = dishMapper.selectCount(queryDish);
        if (count > 0) {
            // 当前分类下有菜品不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        QueryWrapper<Setmeal> querySetMeal = new QueryWrapper<>();
        querySetMeal.eq("category_id", id);
        count = setmealMapper.selectCount(querySetMeal);
        if (count > 0) {
            // 当前分类下有套餐不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        // 删除分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     */
    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
    }

    /**
     * 启用或者禁用分类
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        Category category = new Category();
        category.setStatus(status);
        categoryMapper.update(category, updateWrapper);
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", type);
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        queryWrapper.orderByAsc("sort");
        queryWrapper.orderByDesc("create_time");
        return categoryMapper.selectList(queryWrapper);
    }

}
