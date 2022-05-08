package com.example.demo.src.post.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetPostsRes { // 게시물 객체
    private int postIdx;
    private int userIdx;
    private String nickName;
    private String profileImgUrl;
    private String content;
    private int postLikeCount;
    private int commentCount;
    private String updatedAt;
    private String likeOrNot; // 좋아요 칼럼
    private List<GetPostImgRes> imgs; // 사진들이 리스트로 있어야 함
}
