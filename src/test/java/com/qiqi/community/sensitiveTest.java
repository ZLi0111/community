package com.qiqi.community;

import com.qiqi.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class sensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSentiveFilter(){
        String text = "you are a fucking stupid";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
