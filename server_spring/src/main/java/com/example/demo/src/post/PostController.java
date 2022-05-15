package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PatchPostsReq;
import com.example.demo.src.post.model.PostPostsReq;
import com.example.demo.src.post.model.PostPostsRes;
import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.UserService;
import com.example.demo.src.user.model.GetUserFeedRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.POST_POSTS_EMPTY_IMGURL;
import static com.example.demo.config.BaseResponseStatus.POST_POSTS_INVALID_CONTENTS;

@RestController
@RequestMapping("/posts")
public class PostController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PostProvider postProvider;
    @Autowired
    private final PostService postService;
    @Autowired
    private final JwtService jwtService;

    // command + n
    public PostController(PostProvider postProvider, PostService postService, JwtService jwtService) {
        this.postProvider = postProvider;
        this.postService = postService;
        this.jwtService = jwtService;
    }

    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetPostsRes>> getPosts(@RequestParam int userIdx) { // scroll 하면서 계속 보기 때문에 List
        // 반환값, 응답값: GetUserRes
        try{

            List<GetPostsRes> getPostsRes = postProvider.retrievePosts(userIdx);
            // 조회 - provider에서 처리 / 생성 - service에서 생성
            return new BaseResponse<>(getPostsRes);
        } catch(BaseException exception){
            logger.error("Error!", exception);
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostPostsRes> createPosts(@RequestBody PostPostsReq postPostsReq) { // 생성한 글의 postIdx 반환
        // 반환값, 응답값: GetUserRes
        try{
            // 형식적 validation 처리
            if(postPostsReq.getContent().length() > 450){ // 게시글 길이 제한
                return new BaseResponse<>(POST_POSTS_INVALID_CONTENTS);
            }

            // 이미지 없는 경우
            if(postPostsReq.getPostImgUrls().size() < 1){
                return new BaseResponse<>(POST_POSTS_EMPTY_IMGURL);
            }

            // 조회 - provider에서 처리 / 생성 - service에서 생성
            PostPostsRes postPostsRes = postService.createPosts(postPostsReq.getUserIdx(), postPostsReq);
            // 받은 body에서 userIdx 값과 postPostsReq를 service에 넘김김 - 이후 jwt로 userIdx를 받아올거기 때문에 그때 편리하게 하기 위해
            return new BaseResponse<>(postPostsRes);
        } catch(BaseException exception){
            logger.error("Error!", exception);
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @PatchMapping("/{postIdx}")
    public BaseResponse<String> modifyPost(@PathVariable ("postIdx") int postIdx, @RequestBody PatchPostsReq patchPostsReq) { /
        try{
            // 내용 validation 체크
            if(patchPostsReq.getContent().length() > 450){ // 게시글 길이 제한
                return new BaseResponse<>(POST_POSTS_INVALID_CONTENTS);
            }

            postService.modifyPost(patchPostsReq.getUserIdx(), postIdx, patchPostsReq);
            // userIdx를 따로 받는 이유는 이후 jwt 구현할 경우, 더 편하게 구현 가능

            // 오류가 발생하지 않으면 다음 string 값 출력해주는 로직
            String result = "회원 정보 수정을 완료하였습니다.";
            return new BaseResponse<>(result);

        } catch(BaseException exception){
            logger.error("Error!", exception);
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
