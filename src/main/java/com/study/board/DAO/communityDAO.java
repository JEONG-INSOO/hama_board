package com.study.board.DAO;

import com.study.board.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface communityDAO {

    // 게시글 작성
    Integer save(Community community);

    // 게시글 조회
    Optional<Community> findById(Integer comu_post_id);

    // 게시글 목록 조회
    Page<Community> findAll(Pageable pageable);

    // 게시글 삭제
    Integer deleteById(Integer comu_post_id);

    // 게시글 업데이트
    Integer updateById(Integer comu_post_id, Community community);

    // 게시글 검색 (제목 포함)
    Page<Community> findByTitleContaining(String searchKeyword, Pageable pageable);
}