package com.secondhand.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.seckill.entity.TransactionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionLogMapper extends BaseMapper<TransactionLog> {
}
