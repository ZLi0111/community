package com.qiqi.community.service;

import com.qiqi.community.dao.LoginTicketMapper;
import com.qiqi.community.dao.UserMapper;
import com.qiqi.community.entity.LoginTicket;
import com.qiqi.community.entity.User;
import com.qiqi.community.util.CommunityConstant;
import com.qiqi.community.util.CommunityUtil;
import com.qiqi.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        // null
        if(user == null){
            throw new IllegalArgumentException("user can't be null");
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","Username can't be null");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","Password can't be null");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","Email can't be null");
            return map;
        }

        //验证账号
        User u =  userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "this account is already been taken");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "this email is already been registered");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()) + user.getSalt());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // activate email
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"activate account",content);

        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }
        else
            return ACTIVATION_FAILURE;
    }

    public Map<String,Object> login(String username, String password, int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        // null
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","username can't be empty");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","password can't be empty");
            return map;
        }

        //
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","this account doesn't exist");
            return map;
        }

        if(user.getStatus() == 0){
            map.put("usernameMsg","this account did't activate, please check your email");
            return map;
        }

        password = CommunityUtil.md5(password) + user.getSalt();
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","Password incorrect");
            return map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    public Map<String, Object> resetPassword(String email, String password){
        Map<String,Object> map = new HashMap<>();

        if(StringUtils.isBlank(email)){
            map.put("emailMsg", "Email Can't Be Blank");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "Password Can't Be Blank");
            return map;
        }

        User user = userMapper.selectByEmail(email);
        if(user == null){
            map.put("emailMsg", "This Email Haven't Been Registered");
            return map;
        }

        password = CommunityUtil.md5(password) + user.getSalt();
        if(password.equals(user.getPassword())){
            map.put("passwordMsg","New password can't not be the same as old password");
            return map;
        }
        userMapper.updatePassword(user.getId(),password);

        map.put("user",user);
        return map;

    }

    public Map<String , Object> updatePassword(int userId, String oldPassword, String newPassword){

        Map<String , Object> map = new HashMap<>() ;

        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg", "Original Password Can't Be Blank");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg", "New Password Can't Be Blank");
            return map;
        }

        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword) + user.getSalt();
        if(!user.getPassword().equals(oldPassword)){
            map.put("oldPasswordMsg", "Enter the right password");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword) + user.getSalt();
        userMapper.updatePassword(userId,newPassword);
        return map;

    }

    public User findUserByName(String userName){
        return userMapper.selectByName(userName);
    }
}
