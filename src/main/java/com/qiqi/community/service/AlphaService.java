package com.qiqi.community.service;

import com.qiqi.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("hello, this is qiqi");
    }

    @PostConstruct
    public void init(){
        System.out.println("init AlphaService");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("see you again");
    }

    public String find(){
        return alphaDao.slect();
    }
}
