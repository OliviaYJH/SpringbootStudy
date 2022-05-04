package com.example.demo.src.user;


import com.example.demo.config.BaseException;
import com.example.demo.src.user.model.GetUserFeedRes;
import com.example.demo.src.user.model.GetUserInfoRes;
import com.example.demo.src.user.model.GetUserPostsRes;
import com.example.demo.src.user.model.GetUserRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

//Provider : Read의 비즈니스 로직 처리
@Service
public class UserProvider {

    private final UserDao userDao;
    private final JwtService jwtService;


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(UserDao userDao, JwtService jwtService) {
        this.userDao = userDao;
        this.jwtService = jwtService;
    }


    public GetUserFeedRes retrieveUserFeed(int userIdxByJwt, int userIdx) throws BaseException{
        Boolean isMyFeed = true;

        try{
            if(userIdxByJwt != userIdx) // 내 idx와 보려는 피드의 idx 비교
                isMyFeed = false;

            // 2가지 객체 - 유저 정보, 유저의 게시물 리스트 가져오는 객체
            GetUserInfoRes getUserInfoRes = userDao.selectUserInfo(userIdx);
            List<GetUserPostsRes> getUserPostsRes = userDao.selectUserPosts(userIdx); // 이미지 여러개니깐

            // 유저 피드를 하나 만들어 isMyFeed, getUserInfoRes, getUserPostsRes 전달
            GetUserFeedRes getUsersRes = GetUserFeedRes(isMyFeed, getUserInfoRes, getUserPostsRes);
            return getUsersRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
                    }


    public GetUserRes getUsersByIdx(int userIdx) throws BaseException{
        try{
            GetUserRes getUsersRes = userDao.getUsersByIdx(userIdx);
            return getUsersRes;
        }
        catch (Exception exception) {
            logger.error("Error!", exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }


    public int checkEmail(String email) throws BaseException{
        try{
            return userDao.checkEmail(email); // dao에서 이메일 전달받음
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }



}
