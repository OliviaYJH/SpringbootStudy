package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;
import static com.example.demo.config.BaseResponseStatus.USERS_EMPTY_USER_ID;

@Service // read의 비지니스 로직 처리
public class PostProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PostDao postDao;
    private final JwtService jwtService;

    @Autowired
    public PostProvider(PostDao postDao, JwtService jwtService) {
        this.postDao = postDao;
        this.jwtService = jwtService;
    }

    public List<GetPostsRes> retrievePosts(int userIdx) throws BaseException{
        if(checkUserExist(userIdx)==0){
            throw new BaseException(USERS_EMPTY_USER_ID);
        }

        try{
            List<GetPostsRes> getPosts = postDao.selectPosts(userIdx);
            return getPosts;
        }catch (Exception exception){
            logger.error("Error!", exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // user 체크
    public int checkUserExist(int userIdx) throws BaseException{
        try{
            return postDao.checkUserExist(userIdx);
        } catch (Exception exception){
            logger.error("Error!", exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // post 체크
    public int checkPostExist(int postIdx) throws BaseException{
        try{
            return postDao.checkPostExist(postIdx);
        }catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
