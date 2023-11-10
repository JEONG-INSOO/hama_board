package com.study.board.SVC;

import com.study.board.DAO.communityDAO;
import com.study.board.entity.Community;
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
public class communitySVCImpl implements communitySVC {

    @Autowired
    private communityDAO communityDAO;
    private final NamedParameterJdbcTemplate template;

    // 글작성
    @Override
    public void write(Community community, MultipartFile file) throws Exception {
        // file이 있을 때 처리
        if (file != null && !file.isEmpty()) {
            String projectPath = System.getProperty("user.dir") + "/community/src/main/resources/static/files";

            UUID uuid = UUID.randomUUID();

            String fileName = uuid + "_" + file.getOriginalFilename();
            File saveFile = new File(projectPath, fileName);

            //transferTo : 파일 채널에서 읽은 데이터를 다른 채널로 전송하는 역할
            file.transferTo(saveFile);

            community.setFilename(fileName);
            community.setFilepath("/files/" + fileName); // 경로 수정
        } else {
            // 파일이 없는 경우
            Community existingBoard = communityDAO.findById(community.getComu_post_id()).orElse(null);
            if (existingBoard != null) {
                community.setFilename(existingBoard.getFilename());
                community.setFilepath(existingBoard.getFilepath());
            }
        }

        // 글이 이미 존재하는 경우에는 수정, 없으면 새로 작성
        if (community.getComu_post_id() != null && communityDAO.findById(community.getComu_post_id()).isPresent()) {
            communityDAO.updateById(community.getComu_post_id(), community);
        } else {

            community.setComu_post_id(null); // 새로운 글로 인식되게 ID를 null로 설정
            communityDAO.save(community);
        }
    }

    // 게시글 리스트 (페이징)
    @Override
    public Page<Community> communitylist(Pageable pageable) {
        return communityDAO.findAll(pageable);
    }

    // 게시글 검색 (페이징 포함)
    @Override
    public Page<Community> communitySearchList(String searchKeyword, Pageable pageable) {
        return communityDAO.findByTitleContaining(searchKeyword, pageable);
    }
    // 게시글 조회
    @Override
    public Community communityview(Integer comu_post_id) {
        Optional<Community> optionalCommunity = communityDAO.findById(comu_post_id);
        if (optionalCommunity.isPresent()) {
            return optionalCommunity.get();
        } else {
            return null;
        }
    }

    // 특정 게시글 삭제
    @Override
    public void communityDelete(Integer comu_post_id) {
        communityDAO.deleteById(comu_post_id);
    }

    // 게시글 업데이트
    @Override
    public Integer updateById(Integer comu_post_id, Community community) {
        StringBuilder sql = new StringBuilder();
        sql.append("update community ");
        sql.append("   set title = :title, content = :content, filename = :filename, filepath = :filepath, comu_gubun = :comu_gubun ");
        sql.append(" where comu_post_id = :comu_post_id ");

        // sql 파라미터 수동 매핑
        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("title", community.getTitle())
            .addValue("content", community.getContent())
            .addValue("filename", community.getFilename())
            .addValue("filepath", community.getFilepath())
            .addValue("comu_gubun", community.getComu_gubun())
            .addValue("comu_post_id", comu_post_id);

        return template.update(sql.toString(), param);
    }
}