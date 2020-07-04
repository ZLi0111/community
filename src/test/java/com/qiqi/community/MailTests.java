package com.qiqi.community;

import com.qiqi.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("johnli.lzr@gmail.com","TEST","qiqi community test");
    }
    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","ZhengrongLi");

        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("lizhengrong1003@hotmail.com","TEST",content);

    }


}
