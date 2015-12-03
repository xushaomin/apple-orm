package com.appleframework.orm.mybatis.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * Dao顶层接口
 * 
 * @author cruise.xu
 * @since 2015-10-17
 */
public interface BaseMapper  {
	
	<T> int countByExample(T example);

	<T> int deleteByExample(T example);

    int deleteByPrimaryKey(Long id);

    <T> int insert(T record);

    <T> int insertSelective(T record);

    <T> List<T> selectByExample(T example);

    <T> T selectByPrimaryKey(Long id);

    <T> int updateByExampleSelective(@Param("record") T record, @Param("example") T example);

    <T> int updateByExample(@Param("record") T record, @Param("example") T example);

    <T> int updateByPrimaryKeySelective(T record);

    <T> int updateByPrimaryKey(T record);

}