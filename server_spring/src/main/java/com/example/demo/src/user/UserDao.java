package com.example.demo.src.user;


import com.example.demo.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 유저 정보
    public GetUserInfoRes selectUserInfo(int userIdx){
        String selectUsersInfoQuery = "select u.userIdx  as userIdx, u.nickName as nickName, u.name as name, u.profileImgUrl as profileImgUrl, u.website as website, u.introduce as introduction,\n" +
                "    If(postCount is null, 0, postCount) as postCount,\n" +
                "    If(followerCount is null, 0, followerCount) as followerCount,\n"+
                "    If(followingCount is null, 0, followingCount) as followingCount\n"+
                "from User as u\n" +
                "    left join (select userIdx, count(postIdx) as postCount from Post where status = 'ACTIVE' group by userIdx) p on p.userIdx = u.userIdx\n" +
                "    left join (select followerIdx, count(followIdx) as followerCount from Follow where status = 'ACTIVE' group by followerIdx) fc on fc.followerIdx = u.userIdx\n" +
                "    left join (select followeeIdx, count(followIdx) as followingCount from Follow where status = 'ACTIVE' group by followeeIdx) f on f.followeeIdx = u.userIdx\n" +
                "where u.userIdx = ? and u.status = 'ACTIVE'";

        int selectUserInfoParam = userIdx;

        return this.jdbcTemplate.queryForObject(selectUsersInfoQuery, // 리스트 -> query, 아니면 queryForObject
                (rs,rowNum) -> new GetUserInfoRes(
                        // 모델과 동일 순서로 넣어주어야 함!
                        rs.getString("nickName"),
                        rs.getString("name"),
                        rs.getString("profileImgUrl"),
                        rs.getString("website"),
                        rs.getString("introduction"),
                        rs.getInt("followerCount"),
                        rs.getInt("followingCount"),
                        rs.getInt("postCount")
                ), selectUserInfoParam);
    }

    // 게시글 리스트
    public List<GetUserPostsRes> selectUserPosts(int userIdx){
        String selectUserPostsQuery = "select p.postIdx as postIdx, pi.imgUrl as postImgUrl\n" +
                "from Post as p\n" +
                "    join PostImgUrl as pi on pi.postIdx = p.postIdx and pi.status = 'ACTIVE'\n" +
                "    join User as u on u.userIdx = p.userIdx\n" +
                "where p.status = 'ACTIVE' and u.userIdx = ?\n" +
                "group by p.postIdx\n" +
                "HAVING min(pi.postImgUrlIdx)\n" +
                "order by p.postIdx;";


        int selectUserPostsParam = userIdx;

        return this.jdbcTemplate.query(selectUserPostsQuery, // 리스트 -> query, 아니면 queryForObject
                (rs,rowNum) -> new GetUserPostsRes(
                        // 모델과 동일 순서로 넣어주어야 함!
                        rs.getInt("postIdx"),
                        rs.getString("postImgUrl")
                ), selectUserPostsParam);
        // query 사용 - List로 반환하고 있기 때문에
    }

    public GetUserRes getUsersByEmail(String email){
        // GetUserRes 모델에 필요한 값 출력하도록 query문 작성
        String getUsersByEmailQuery = "select userIdx,name,nickName,email from User where email=? and status = 'ACTIVE' ";
        String getUsersByEmailParams = email; // ?에 들어갈 parameter / ?가 여러개인 경우, 리스트로 관리
        return this.jdbcTemplate.queryForObject(getUsersByEmailQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email")),
                getUsersByEmailParams);
        // queryForObject 사용 - 하나의 객체만 return 할 때 사용
    }


    public GetUserRes getUsersByIdx(int userIdx){
        String getUsersByIdxQuery = "select userIdx,name,nickName,email from User where userIdx=? and status = 'ACTIVE' ";
        int getUsersByIdxParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUsersByIdxQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email")),
                getUsersByIdxParams);
    }

    public int createUser(PostUserReq postUserReq){
        String createUserQuery = "insert into User (name, nickName, phone, email, password) VALUES (?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getName(), postUserReq.getNickName(),postUserReq.getPhone(), postUserReq.getEmail(), postUserReq.getPassword()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class);
    }

    public int checkEmail(String email){ // 이메일이 존재하는지 확인
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);

    }

    // userIdx가 유효한지 validation
    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);

    }

    public int modifyUserName(PatchUserReq patchUserReq){
        String modifyUserNameQuery = "update User set nickName = ? where userIdx = ? ";
        Object[] modifyUserNameParams = new Object[]{patchUserReq.getNickName(), patchUserReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserNameQuery,modifyUserNameParams);
    }

    public int patchUser(Integer userIdx){
        String patchUserQuery = "update User set status = 'DELETED' where userIdx = ? ";
        return this.jdbcTemplate.update(patchUserQuery, userIdx);
    }


}
