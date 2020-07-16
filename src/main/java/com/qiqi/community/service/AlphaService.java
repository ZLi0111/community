package com.qiqi.community.service;

import com.qiqi.community.dao.AlphaDao;
import com.qiqi.community.dao.DiscussPostMapper;
import com.qiqi.community.dao.UserMapper;
import com.qiqi.community.entity.DiscussPost;
import com.qiqi.community.entity.User;
import com.qiqi.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

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

    //REQUIRE: 支持当前事务（外部事务），如果不存在则创建新事务，
    //REQUIRED_NEW: 创建一个新的事务，并且暂停当前事务（外部事务）
    //NESTED: 如果当前存在外部事务，则嵌套在该事务中执行（独立提交和回滚）
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1(){
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123") + user.getSalt());
        user.setEmail("alpha@gmail.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hello");
        discussPost.setContent("new here");
        discussPostMapper.insertDiscussPost(discussPost);

        Integer.valueOf("abc");
        return "Ok";
    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {

            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {

                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123") + user.getSalt());
                user.setEmail("beta@gmail.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("hello1");
                discussPost.setContent("new here1");
                discussPostMapper.insertDiscussPost(discussPost);

                Integer.valueOf("abc");
                return "Ok";
            }
        });
    }
}
