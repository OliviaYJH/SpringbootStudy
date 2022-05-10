package com.example.demo.src.post.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostPostsReq {
    private int userIdx;
    private String content; // 게시을 내용
    private List<PostImgUrlsReq> postImgUrls; // 여러개의 사진을 PostImgUrls 객체로 받아옴

}
