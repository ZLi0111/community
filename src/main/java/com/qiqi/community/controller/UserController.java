package com.qiqi.community.controller;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.qiqi.community.annotation.LoginRequired;
import com.qiqi.community.entity.User;
import com.qiqi.community.service.FollowService;
import com.qiqi.community.service.LikeService;
import com.qiqi.community.service.UserService;
import com.qiqi.community.util.CommunityConstant;
import com.qiqi.community.util.CommunityUtil;
import com.qiqi.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    private String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){

        if(headerImage == null){
            model.addAttribute("error","you need to submit the image");
            return "/site/setting";
        }

        String fileName =  headerImage.getOriginalFilename();
        String suffix =  fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","you need to submit the right image format");
            return "/site/setting";
        }

        String home = System.getProperty("user.home");
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(home + uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("upload image fail" + e.getMessage());
            throw new RuntimeException("upload image fail, server broke",e);
        }

        //更新用户头像路径
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器 存放路径
        String home = System.getProperty("user.home");
        fileName = home + uploadPath + "/" + fileName;
        String suffix =  fileName.substring(fileName.lastIndexOf("."));
        //响应文件
        response.setContentType("image/" + suffix);
        try(
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
                ) {

            byte[] buffer = new byte[1024];
            int b = 0;
            while(((b = fis.read(buffer)) != -1)){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("fail to read the user image" + e.getMessage());

        }
    }

    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model){

        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(),oldPassword,newPassword);
        if(map == null || map.isEmpty()){
            return "redirect:/logout";
        }
        else{
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String profilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("***[This user doesn't exits]***");
        }

        model.addAttribute("user", user);

        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
