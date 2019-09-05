package com.fanlun.redislock.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanlun.redislock.entity.Amount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AmountDao extends BaseMapper<Amount> {
}
