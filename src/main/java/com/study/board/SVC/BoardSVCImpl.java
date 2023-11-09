package com.study.board.SVC;

import com.study.board.DAO.BoardDAO;
import com.study.board.entity.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardSVCImpl implements BoardSVC {

    @Autowired
    private BoardDAO boardDAO;
    private final NamedParameterJdbcTemplate template;

    // 글작성
    @Override
    public void write(Board board, MultipartFile file) throws Exception {
        // file이 있을 때 처리
        if (file != null && !file.isEmpty()) {
            String projectPath = System.getProperty("user.dir") + "/board/src/main/resources/static/files";

            UUID uuid = UUID.randomUUID();

            String fileName = uuid + "_" + file.getOriginalFilename();
            File saveFile = new File(projectPath, fileName);

            //transferTo : 파일 채널에서 읽은 데이터를 다른 채널로 전송하는 역할
            file.transferTo(saveFile);

            board.setFilename(fileName);
            board.setFilepath("/files/" + fileName); // 경로 수정
        } else {
            // 파일이 없는 경우
            Board existingBoard = boardDAO.findById(board.getId()).orElse(null);
            if (existingBoard != null) {
                board.setFilename(existingBoard.getFilename());
                board.setFilepath(existingBoard.getFilepath());
            }
        }

        // 글이 이미 존재하는 경우에는 수정, 없으면 새로 작성
        if (board.getId() != null && boardDAO.findById(board.getId()).isPresent()) {
            boardDAO.updateById(board.getId(), board);
        } else {

            board.setId(null); // 새로운 글로 인식되게 ID를 null로 설정
            boardDAO.save(board);
        }
    }

    // 게시글 리스트 (페이징)
    @Override
    public Page<Board> boardlist(Pageable pageable) {
        return boardDAO.findAll(pageable);
    }

    // 게시글 검색 (페이징 포함)
    @Override
    public Page<Board> boardSearchList(String searchKeyword, Pageable pageable) {
        return boardDAO.findByTitleContaining(searchKeyword, pageable);
    }
    // 게시글 조회
    @Override
    public Board boardview(Integer id) {
        Optional<Board> optionalBoard = boardDAO.findById(id);
        if (optionalBoard.isPresent()) {
            return optionalBoard.get();
        } else {
            return null;
        }
    }

    // 특정 게시글 삭제
    @Override
    public void boardDelete(Integer id) {
        boardDAO.deleteById(id);
    }

    // 게시글 업데이트
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

        return template.update(sql.toString(), param);
    }
}