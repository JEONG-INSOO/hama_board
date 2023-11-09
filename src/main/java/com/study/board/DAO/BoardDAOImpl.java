package com.study.board.DAO;

import com.study.board.entity.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
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
public class BoardDAOImpl implements  BoardDAO {

    //SQL과 JAVA사이에서 통역해주는 역할
    private final NamedParameterJdbcTemplate template;

    //게시글 작성
    @Override
    public Integer save(Board board) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO board (title, content ");

        //첨부파일이 있을 경우
        if (board.getFilename() != null && board.getFilepath() != null) {
            sql.append(", filename, filepath ");
        }

        sql.append(") VALUES (:title, :content ");

        //첨부파일이 있을 경우
        if (board.getFilename() != null && board.getFilepath() != null) {
            sql.append(", :filename, :filepath ");
        }

        sql.append(") ");

        // 메서드에 파라미터값 제공         // 주어진 객체의 프로퍼티 값을 자동으로 파라미터로 맵핑
        SqlParameterSource param = new BeanPropertySqlParameterSource(board);

        // 행이 추가됨으로 게시글이 생성된 것을 확인
        return template.update(sql.toString(), param);
    }

    //Board 객체 반환
    private RowMapper<Board> boardRowMapper() {
        return (rs, rowNum) -> {
            Board board = new Board();
            board.setId(rs.getInt("id"));
            board.setTitle(rs.getString("title"));
            board.setContent(rs.getString("content"));
            board.setFilename(rs.getString("filename"));
            board.setFilepath(rs.getString("filepath"));

            return board;
        };
    }
    //게시글 조회
    @Override
    public Optional<Board> findById(Integer boardId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id,title,content,filename,filepath ");
        sql.append("  from board ");
        sql.append(" where id = :id ");

        RowMapper<Board> myRowMapper = boardRowMapper();

        // 변경된 부분: Map을 생성하는 방식 변경
        try {
            //조회 : (단일행,단일열),(단일행,다중열),(다중행,단일열),(다중행,다중열)
            // SQL 파라미터 수동매핑
            Map<String, Integer> param = new HashMap<>();
            param.put("id", boardId);

            //RowMapper 수동 매핑
            Board board = template.queryForObject(sql.toString(), param, myRowMapper);
            return Optional.of(board);
        } catch (EmptyResultDataAccessException e) {
            // 조회결과가 없는 경우
            return Optional.empty();
        }
    }
    @Override
    public Page<Board> findAll(Pageable pageable) {
        // Total count query
        String countSql = "SELECT COUNT(*) FROM board";
        long total = template.queryForObject(countSql, new MapSqlParameterSource(), Long.class);

        // Data query
        String dataSql = "SELECT * FROM (SELECT id, title, content, filename, filepath, ROWNUM AS rnum FROM board ORDER BY id DESC) WHERE rnum BETWEEN :startRow AND :endRow";

        int startRow = pageable.getPageNumber() * pageable.getPageSize() + 1;
        int endRow = (pageable.getPageNumber() + 1) * pageable.getPageSize();

        List<Board> result = template.query(dataSql, new MapSqlParameterSource()
                        .addValue("startRow", startRow)
                        .addValue("endRow", endRow),
                new BeanPropertyRowMapper<>(Board.class));

        // Create Page object
        return new PageImpl<>(result, pageable, total);
    }
    @Override
    public Integer deleteById(Integer boardId) {
        String sql = "delete from board where id = :boardId";

        //SQL 파라미터 수동 매핑
        Integer deletedRowCnt = template.update(sql, Map.of("boardId", boardId));

        return deletedRowCnt;
    }

    @Override
    public Integer updateById(Integer boardId, Board board) {
        StringBuilder sql = new StringBuilder();
        sql.append("update board ");
        sql.append("   set title = :title, content = :content, filename = :filename, filepath = :filepath ");
        sql.append(" where id = :board_id ");

        // sql 파라미터 수동 매핑
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("title", board.getTitle())
                .addValue("content", board.getContent())
                .addValue("filename", board.getFilename())
                .addValue("filepath", board.getFilepath())
                .addValue("board_id", boardId);

        Integer updatedRows = template.update(sql.toString(), param);
        return updatedRows;
    }
    //검색
    @Override
    public Page<Board> findByTitleContaining(String searchKeyword, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, title, content, filename, filepath ");
        sql.append("FROM board ");
        sql.append("WHERE title LIKE '%" + searchKeyword + "%'");

        // Replace the BeanPropertyRowMapper with a custom row mapper to handle nullable filename and filepath columns
        RowMapper<Board> rowMapper = new RowMapper<Board>() {
            @Override
            public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
                Board board = new Board();
                board.setId(rs.getInt("id"));
                board.setTitle(rs.getString("title"));
                board.setContent(rs.getString("content"));

                String filename = rs.getString("filename");
                if (filename != null) {
                    board.setFilename(filename);
                }

                String filepath = rs.getString("filepath");
                if (filepath != null) {
                    board.setFilepath(filepath);
                }

                return board;
            }
        };

        List<Board> list = template.query(sql.toString(), rowMapper);
        return new PageImpl<>(list, pageable, list.size());
    }
}
