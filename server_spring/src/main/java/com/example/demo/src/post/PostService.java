package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PatchPostsReq;
import com.example.demo.src.post.model.PostPostsReq;
import com.example.demo.src.post.model.PostPostsRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;
import static com.example.demo.config.BaseResponseStatus.USERS_EMPTY_USER_ID;

@Service  // Create, Update, Delete 의 로직 처리
public class PostService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PostDao postDao;
    private final PostProvider postProvider;
    private final JwtService jwtService;

    @Autowired
    public PostService(PostDao postDao, PostProvider postProvider, JwtService jwtService) {
        this.postDao = postDao;
        this.postProvider = postProvider;
        this.jwtService = jwtService;
    }

    public PostPostsRes createPosts(int userIdx, PostPostsReq postPostsReq) throws BaseException {
        try{
            int postIdx = postDao.insertPosts(userIdx, postPostsReq.getContent()); // userIdx와 게시물의 내용만 넣어줌

            // 게시물의 이미지는 리스트로 넣어주어야 함 - 반복문 for 사용해 다른 함수로 처리
            for(int i = 0; i< postPostsReq.getPostImgUrls().size(); i++){
                postDao.insertPostImg(postIdx, postPostsReq.getPostImgUrls().get(i)); // 반복문을 돌면서 이미지들이 하나씩 db에 저장
            }

            return new PostPostsRes(postIdx);
        }catch (Exception exception){
            logger.error("Error!", exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void modifyPost(int userIdx, int postIdx, PatchPostsReq patchPostsReq) throws BaseException {
        try{
            // postDao에서는 result 값 받 => postDao에서 update문이 잘 실행되면 result로 일을 전달하고 아니라면 0을 전달해 에러코드 전달
            int result = postDao.updatePosts(postIdx, patchPostsReq.getContent());

            // void는 return 값 필요 없음
        }catch (Exception exception){
            logger.error("Error!", exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
