package com.study.board.DAO;

import com.study.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BoardDAO {

    // 게시글 작성
    Integer save(Board board);

    // 게시글 조회
    Optional<Board> findById(Integer boardId);

    // 게시글 목록 조회
    Page<Board> findAll(Pageable pageable);

    // 게시글 삭제
    Integer deleteById(Integer boardId);

    // 게시글 업데이트
    Integer updateById(Integer boardId, Board board);

    // 게시글 검색 (제목 포함)
    Page<Board> findByTitleContaining(String searchKeyword, Pageable pageable);
}