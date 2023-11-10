package com.study.board.SVC;

import com.study.board.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface communitySVC {

    // 글작성
    void write(Community community, MultipartFile file) throws Exception;

    // 게시글 리스트 (페이징)
    Page<Community> communitylist(Pageable pageable);

    // 게시글 검색 (페이징 포함)
    Page<Community> communitySearchList(String searchKeyword, Pageable pageable);

    // 게시글 조회
    Community communityview(Integer comu_post_id);

    // 특정 게시글 삭제
    void communityDelete(Integer comu_post_id);

    // 게시글 업데이트
    Integer updateById(Integer comu_post_id, Community community);
}