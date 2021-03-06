package com.example.demo.src.post;

import com.example.demo.src.post.model.GetPostImgRes;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PostImgUrlsReq;
import com.example.demo.src.user.model.GetUserPostsRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {
    private JdbcTemplate jdbcTemplate;
    private List<GetPostImgRes> getPostImgRes;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 게시물 리스트 조회
    public List<GetPostsRes> selectPosts(int userIdx){
        String selectPostsQuery = "\n" +
                "        SELECT p.postIdx as postIdx,\n" +
                "            u.userIdx as userIdx,\n" +
                "            u.nickName as nickName,\n" +
                "            u.profileImgUrl as profileImgUrl,\n" +
                "            p.content as content,\n" +
                "            IF(postLikeCount is null, 0, postLikeCount) as postLikeCount,\n" +
                "            IF(commentCount is null, 0, commentCount) as commentCount,\n" +
                "            case\n" +
                "                when timestampdiff(second, p.updatedAt, current_timestamp) < 60\n" +
                "                    then concat(timestampdiff(second, p.updatedAt, current_timestamp), '초 전')\n" +
                "                when timestampdiff(minute , p.updatedAt, current_timestamp) < 60\n" +
                "                    then concat(timestampdiff(minute, p.updatedAt, current_timestamp), '분 전')\n" +
                "                when timestampdiff(hour , p.updatedAt, current_timestamp) < 24\n" +
                "                    then concat(timestampdiff(hour, p.updatedAt, current_timestamp), '시간 전')\n" +
                "                when timestampdiff(day , p.updatedAt, current_timestamp) < 365\n" +
                "                    then concat(timestampdiff(day, p.updatedAt, current_timestamp), '일 전')\n" +
                "                else timestampdiff(year , p.updatedAt, current_timestamp)\n" +
                "            end as updatedAt,\n" +
                "            IF(pl.status = 'ACTIVE', 'Y', 'N') as likeOrNot\n" +
                "        FROM Post as p\n" +
                "            join User as u on u.userIdx = p.userIdx\n" +
                "            left join (select postIdx, userIdx, count(postLikeidx) as postLikeCount from PostLike WHERE status = 'ACTIVE' group by postIdx) plc on plc.postIdx = p.postIdx\n" +
                "            left join (select postIdx, count(commentIdx) as commentCount from Comment WHERE status = 'ACTIVE' group by postIdx) c on c.postIdx = p.postIdx\n" +
                "            left join Follow as f on f.followeeIdx = p.userIdx and f.status = 'ACTIVE'\n" +
                "            left join PostLike as pl on pl.userIdx = f.followerIdx and pl.postIdx = p.postIdx\n" +
                "        WHERE f.followerIdx = ? and p.status = 'ACTIVE'\n" +
                "        group by p.postIdx;\n" ;

        int selectPostsParam = userIdx;

        return this.jdbcTemplate.query(selectPostsQuery, // 리스트 -> query, 아니면 queryForObject
                (rs,rowNum) -> new GetPostsRes(
                        // 모델과 동일 순서로 넣어주어야 함!
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        rs.getString("updatedAt"),
                        rs.getString("likeOrNot"),
                        getPostImgRes = this.jdbcTemplate.query("select pi.postImgUrlIdx, pi.imgUrl\n" +
                                "from PostImgUrl as pi\n" +
                                "    join Post as p on p.postIdx = pi.postIdx\n" +
                                "where pi.status = 'ACTIVE' and p.postIdx = ?;",
                            (rk, rownum) -> new GetPostImgRes(
                                    rk.getInt("postImgUrlIdx"),
                                    rk.getString("imgUrl")
                            ), rs.getInt("postIdx")
                        )
                ), selectPostsParam);
    }

    // userIdx가 유효한지 validation
    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);
    }

    public int insertPosts(int userIdx, String content){
        String insertPostQuery = "INSERT INTO Post(userIdx, content) VALUES (?,?)";
        Object []insertPostParams = new Object[] {userIdx, content};
        // insert문은 return이 아니라 update를 해주는 것임
        this.jdbcTemplate.update(insertPostQuery, insertPostParams); // data 들어감

        // 함수 호출한 후, postIdx를 클라이언트에 전달
        String lastInsertIdxQuery = "select last_insert_id()"; // 가장 마지막에 들어간 idx값을 자동으로 리턴해줌
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);
    }

    // 이미지 넣는 함수
    public int insertPostImg(int postIdx, PostImgUrlsReq postImgUrlsReq){
        String insertPostImgsQuery = "INSERT INTO PostImgUrl(postIdx, imgUrl) VALUES (?,?)";
        Object []insertPostImgxParams = new Object[] {postIdx, postImgUrlsReq.getImgUrl()};
        this.jdbcTemplate.update(insertPostImgsQuery, insertPostImgxParams); // data 들어감

        // 함수 호출한 후, postIdx를 클라이언트에 전달
        String lastInsertIdxQuery = "select last_insert_id()"; // 가장 마지막에 들어간 idx값을 자동으로 리턴해줌
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);
    }

    public int updatePosts(int postIdx, String content){
        String updatePostQuery = "update Post set content=? where postIdx=?";
        Object []updatePostParams = new Object[] {content, postIdx};
        return this.jdbcTemplate.update(updatePostQuery, updatePostParams); // data 들어감
    }

    // validation 처리
    public int checkPostExist(int postIdx){
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx = ?)";
        int checkPostExistParams = postIdx;
        return this.jdbcTemplate.queryForObject(checkPostExistQuery,
                int.class,
                checkPostExistParams);
    }

    // 게시물 삭제
    public int deletePost(int postIdx){
        String deletePostQuery = "update Post set status = 'INACTIVE' where postIdx=?";
        // Object[] deletePostParams = new Object[] {postIdx};
        return this.jdbcTemplate.update(deletePostQuery, postIdx); // data 들어감
    }
}
