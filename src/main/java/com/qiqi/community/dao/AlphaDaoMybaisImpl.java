package com.qiqi.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaDaoMybaisImpl implements AlphaDao{
    @Override
    public String slect() {
        return "MyBatis";
    }
}
