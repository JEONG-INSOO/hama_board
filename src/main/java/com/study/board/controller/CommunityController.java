package com.study.board.controller;

import com.study.board.SVC.communitySVCImpl;
import com.study.board.entity.Community;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CommunityController {

    @Autowired
    private communitySVCImpl communityservice;

    @GetMapping("/community/write")
    public String boardWriteForm(Model model){
        model.addAttribute("community", new Community());
        return "communitywrite";
    }

    @PostMapping("/community/writepro")
    public String communityWritePro(Community community, Model model, MultipartFile file) throws Exception {

        // 게시물 정보 저장
        communityservice.write(community, file);


        model.addAttribute("message", "글 작성이 완료됐습니다");
        model.addAttribute("searchUrl", "/community/list");

        return "message";
    }

    @GetMapping("/community/list")
    public String communityList(Model model,
                                @PageableDefault(page = 0, size = 10, sort = "comu_post_id", direction = Sort.Direction.DESC) Pageable pageable,
                                String searchKeyword) {

        Page<Community> list = null;
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            list = communityservice.communitylist(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("comu_post_id"))));
        } else {
            list = communityservice.communitySearchList(searchKeyword, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("comu_post_id"))));
        }

        int nowPage = list.getNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());

        model.addAttribute("list", list);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchKeyword", searchKeyword);

        return "communitylist";
    }
    @GetMapping("/community/view")
    public String communityview(Model model, Integer comu_post_id){
        model.addAttribute("community", communityservice.communityview(comu_post_id));
        return "communityview";
    }

    @GetMapping("/community/delete")
    public String communityDelete(Integer comu_post_id){
        communityservice.communityDelete(comu_post_id);
        return "redirect:/community/list";
    }

    @GetMapping("/community/modify/{comu_post_id}")
    public String communityModify(@PathVariable("comu_post_id") Integer comu_post_id, Model model){
        model.addAttribute("community", communityservice.communityview(comu_post_id));
        return "communitymodify";
    }

    @PostMapping("/community/update/{comu_post_id}")
    public String boardUpdate(@PathVariable("comu_post_id") Integer comu_post_id,
                              @RequestParam("comu_gubun") String comu_gubun,
                              Community community,
                              Model model,
                              MultipartFile file) throws Exception {

        Community communityTemp = communityservice.communityview(comu_post_id);

        communityTemp.setTitle(community.getTitle());
        communityTemp.setContent(community.getContent());
        communityTemp.setComu_gubun(comu_gubun); // 여기서 comu_gubun을 설정해줍니다.

        communityservice.write(communityTemp, file);

        model.addAttribute("message", "글 수정이 완료됐습니다");
        model.addAttribute("searchUrl", "/community/view?comu_post_id=" + comu_post_id);

        return "message";
    }
}