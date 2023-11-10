package com.study.board.DAO;

import com.study.board.entity.Community;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class communityDAOImpl implements communityDAO {

    //SQL과 JAVA사이에서 통역해주는 역할
    private final NamedParameterJdbcTemplate template;

    //게시글 작성
    @Override
    public Integer save(Community community) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO community (comu_gubun, title, content");

        //첨부파일이 있을 경우
        if (community.getFilename() != null && community.getFilepath() != null) {
            sql.append(", filename, filepath");
        }

        sql.append(") VALUES (:comu_gubun, :title, :content");

        //첨부파일이 있을 경우
        if (community.getFilename() != null && community.getFilepath() != null) {
            sql.append(", :filename, :filepath");
        }

        sql.append(")");

        // 메서드에 파라미터값 제공
        MapSqlParameterSource param = new MapSqlParameterSource()
            .addValue("comu_gubun", community.getComu_gubun()) // 추가된 부분
            .addValue("title", community.getTitle())
            .addValue("content", community.getContent())
            .addValue("filename", community.getFilename())
            .addValue("filepath", community.getFilepath());

        // 행이 추가됨으로 게시글이 생성된 것을 확인
        return template.update(sql.toString(), param);
    }

    //community 객체 반환
    private RowMapper<Community> communityRowMapper() {
        return (rs, rowNum) -> {
            Community community = new Community();
            community.setComu_post_id(rs.getInt("comu_post_id"));
            community.setComu_gubun(rs.getString("comu_gubun"));  // 추가된 부분
            community.setTitle(rs.getString("title"));
            community.setContent(rs.getString("content"));
            community.setFilename(rs.getString("filename"));
            community.setFilepath(rs.getString("filepath"));

            return community;
        };
    }
    //게시글 조회
    @Override
    public Optional<Community> findById(Integer comu_post_id) {
        StringBuilder sql = new StringBuilder();
        sql.append("select comu_gubun, comu_post_id,title,content,filename,filepath ")
            .append("  from community ")
            .append(" where comu_post_id = :comu_post_id ");

        RowMapper<Community> myRowMapper = communityRowMapper();

        try {
            // 조회 : (단일행,단일열),(단일행,다중열),(다중행,단일열),(다중행,다중열)
            // SQL 파라미터 수동매핑
            Map<String, Integer> param = new HashMap<>();
            param.put("comu_post_id", comu_post_id);

            // RowMapper 수동 매핑
            Community community = template.queryForObject(sql.toString(), param, myRowMapper);
            return Optional.of(community);
        } catch (EmptyResultDataAccessException e) {
            // 조회결과가 없는 경우
            return Optional.empty();
        }
    }
    @Override
    public Page<Community> findAll(Pageable pageable) {
        // Total count query
        String countSql = "SELECT COUNT(*) FROM community";
        long total = template.queryForObject(countSql, new MapSqlParameterSource(), Long.class);

        // Data query with ORDER BY comu_post_id DESC for pages and ASC for each page

        String dataSql = "SELECT * FROM (SELECT comu_post_id, title, content, comu_gubun, filename, filepath, ROWNUM AS rnum FROM (SELECT comu_post_id, title, content, comu_gubun, filename, filepath FROM community ORDER BY comu_post_id DESC)) WHERE rnum BETWEEN :startRow AND :endRow";

        int startRow = pageable.getPageNumber() * pageable.getPageSize() + 1;
        int endRow = (pageable.getPageNumber() + 1) * pageable.getPageSize();

        List<Community> result = template.query(dataSql, new MapSqlParameterSource()
                .addValue("startRow", startRow)
                .addValue("endRow", endRow),
            new BeanPropertyRowMapper<>(Community.class));

        // Create Page object
        return new PageImpl<>(result, pageable, total);
    }
    @Override
    public Integer deleteById(Integer comu_post_id) {
        String sql = "delete from community where comu_post_id = :comu_post_id";

        //SQL 파라미터 수동 매핑
        Integer deletedRowCnt = template.update(sql, Map.of("comu_post_id", comu_post_id));

        return deletedRowCnt;
    }

    @Override
    public Integer updateById(Integer comu_post_id, Community community) {
        StringBuilder sql = new StringBuilder();
        sql.append("update community ");
        sql.append("   set comu_gubun = :comu_gubun, title = :title, content = :content, filename = :filename, filepath = :filepath ");
        sql.append(" where comu_post_id = :comu_post_id ");

        // sql 파라미터 수동 매핑
        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("comu_gubun", community.getComu_gubun())
            .addValue("title", community.getTitle())
            .addValue("content", community.getContent())
            .addValue("filename", community.getFilename())
            .addValue("filepath", community.getFilepath())
            .addValue("comu_post_id", comu_post_id);

        Integer updatedRows = template.update(sql.toString(), param);
        return updatedRows;
    }
    //검색
    @Override
    public Page<Community> findByTitleContaining(String searchKeyword, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT comu_post_id, title, content, filename, filepath ");
        sql.append("FROM community ");
        sql.append("WHERE title LIKE '%" + searchKeyword + "%'");

        // Replace the BeanPropertyRowMapper with a custom row mapper to handle nullable filename and filepath columns
        RowMapper<Community> rowMapper = new RowMapper<Community>() {
            @Override
            public Community mapRow(ResultSet rs, int rowNum) throws SQLException {
                Community community = new Community();
                community.setComu_post_id(rs.getInt("comu_post_id"));
                community.setTitle(rs.getString("title"));
                community.setContent(rs.getString("content"));

                String filename = rs.getString("filename");
                if (filename != null) {
                    community.setFilename(filename);
                }

                String filepath = rs.getString("filepath");
                if (filepath != null) {
                    community.setFilepath(filepath);
                }

                return community;
            }
        };

        List<Community> list = template.query(sql.toString(), rowMapper);
        return new PageImpl<>(list, pageable, list.size());
    }
}
