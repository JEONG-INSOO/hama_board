package com.study.board.SVC;

import com.study.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BoardSVC {

    // 글작성
    void write(Board board, MultipartFile file) throws Exception;

    // 게시글 리스트 (페이징)
    Page<Board> boardlist(Pageable pageable);

    // 게시글 검색 (페이징 포함)
    Page<Board> boardSearchList(String searchKeyword, Pageable pageable);

    // 게시글 조회
    Board boardview(Integer id);

    // 특정 게시글 삭제
    void boardDelete(Integer id);

    // 게시글 업데이트
    Integer updateById(Integer boardId, Board board);
}