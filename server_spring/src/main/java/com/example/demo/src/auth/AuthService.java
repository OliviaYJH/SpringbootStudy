package com.example.demo.src.auth;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponseStatus;
import com.example.demo.src.auth.model.PostLoginReq;
import com.example.demo.src.auth.model.PostLoginRes;
import com.example.demo.src.auth.model.User;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Service Create, Update, Delete의 로직 처리
@Service
public class AuthService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthDao authDao;
    private final AuthProvider authProvider;
    private final JwtService jwtService;

    @Autowired
    public AuthService(AuthDao authDao, AuthProvider authProvider, JwtService jwtService) {
        this.authDao = authDao;
        this.authProvider = authProvider;
        this.jwtService = jwtService;
    }

    public PostLoginRes logIn(PostLoginReq postLoginReq) throws BaseException{
        User user = authDao.getPwd(postLoginReq);

        // 새로 받은 비밀번호 암호화
        String encryptPwd;

        try{
            // 암호화
            encryptPwd = new SHA256().encrypt(postLoginReq.getPwd());

            // postLoginReq 요청 객체를 보내 알맞는 user 반환 -> 기존에 저장된 pwd와 입력받은 pwd 비교 필요!

        }catch (Exception exception){
            throw new BaseException(BaseResponseStatus.PASSWORD_ENCRYPTION_ERROR);
        }

        // User에서 받아온 비밀번호와 암호화해준 비밀번호 비교
        if(user.getPwd().equals(encryptPwd)){
            int userIdx = user.getUserIdx();
            // jwt 생성
            String jwt = jwtService.createJwt(userIdx);
            return new PostLoginRes(userIdx, jwt);
        }else{
            // pwd 일치하지 않는 경우
            throw new BaseException(BaseResponseStatus.FAILED_TO_LOGIN);
        }
    }
}
