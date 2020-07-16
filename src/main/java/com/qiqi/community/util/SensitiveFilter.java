package com.qiqi.community.util;
import java.io.*;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "**";

    private TireNode root = new TireNode();

    @PostConstruct
    public void init(){
       try(
               InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
               BufferedReader reader = new BufferedReader(new InputStreamReader(is));
       ){
           String keyword;
           while((keyword = reader.readLine()) != null){
               this.addKeyword(keyword);
           }
       }catch (IOException e){
           logger.error("###[sensitive word error]###" + e);
       }
    }
    //过滤敏感词
    //@param text 过滤前
    //@return  过滤后
    public String filter(String text){
        if(StringUtils.isBlank(text))
            return null;

        TireNode tempNode = root;
        int begin = 0;
        int position = 0;
        StringBuilder result = new StringBuilder();

        while (begin < text.length()){
            char c = text.charAt(position);

            if(isSymbol(c)){
                if(tempNode == root){
                    result.append(c);
                    begin++;
                }
                position ++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //not sensitive word
                result.append(text.charAt(begin));
                position = ++ begin;
                tempNode = root;
            }else if(tempNode.isKeyWordEnd()){
                //found sensitive word replace  [begin - position]
                result.append(REPLACEMENT);
                begin = ++position;
                tempNode = root;
            }else {
                if(position < text.length() - 1)
                    position ++;
            }

        }

    //the last char
        result.append(text.substring(begin));
        return result.toString();

    }

    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private void addKeyword(String keyword){
        TireNode tempNode = root;
        for(int i = 0; i < keyword.length(); i ++){
            char c = keyword.charAt(i);
            TireNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                subNode = new TireNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点
            tempNode = subNode;

            //结束标志
            if(i == keyword.length() - 1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    //前缀树
    private class TireNode{
        //单词结尾
        private boolean isKeyWordEnd = false;
        //子节点(key是下级字符，value 是下级节点)
        private Map<Character, TireNode> subNode = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }
        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }
        //添加子节点
        public void addSubNode(Character c, TireNode node){
            subNode.put(c, node);
        }
        public TireNode getSubNode(Character c){
            return subNode.get(c);
        }

    }

}
