package com.qiqi.community.util;

public interface CommunityConstant {
    //success
    int ACTIVATION_SUCCESS = 0;

    //repeat
    int ACTIVATION_REPEAT = 1;

    //FAIL
    int ACTIVATION_FAILURE = 2;

    //default login time
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    //remember me
    int REMEMBERME_EXPIRED_SECONDS = 3600 * 24 * 14;

    //实体类型： 帖子  （1）
    int ENTITY_TYPE_POST = 1;

    //实体类型： 评论  （2）
    int ENTITY_TYPE_COMMENT = 2;

    //实体类型： 用户  （3）
    int ENTITY_TYPE_USER = 3;
}
