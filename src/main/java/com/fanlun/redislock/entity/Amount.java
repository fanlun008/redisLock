package com.fanlun.redislock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Amount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long count;
}
