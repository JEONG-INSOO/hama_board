
package com.study.board.controller;

import com.study.board.entity.Board;
import com.study.board.SVC.BoardSVCImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BoardController {

    @Autowired
    private BoardSVCImpl boardService;
    @GetMapping("/board/write")         //localhost:8080/board/write
    public String boardWriteForm(){
        return "boardwrite";
    }

    @PostMapping("/board/writepro")
    public String boardWritePro(Board board, Model model, MultipartFile file) throws Exception {

        // 게시물 정보 저장
        boardService.write(board, file);


        model.addAttribute("message", "글 작성이 완료됐습니다");
        model.addAttribute("searchUrl", "/board/list");

        return "message";
    }

    @GetMapping("/board/list")
    public String boardList(Model model,
                            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                            String searchKeyword) {

        Page<Board> list = null;
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            list = boardService.boardlist(pageable);
        } else {
            list = boardService.boardSearchList(searchKeyword, pageable);
        }

        int nowPage = list.getNumber() + 1; // 현재 페이지 번호 계산
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());

        model.addAttribute("list", list);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchKeyword", searchKeyword); // 검색어를 유지하기 위해 모델에 추가

        return "boardlist";
    }
    @GetMapping("/board/view")      //localhost:8090/board/view?id=1
    public String boardview(Model model, Integer id){

        model.addAttribute("board", boardService.boardview(id));
        return "boardview";
    }

    @GetMapping("/board/delete")
    public String boardDelete(Integer id){
        boardService.boardDelete(id);

        return "redirect:/board/list";
    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable("id") Integer id, Model model){

        model.addAttribute("board", boardService.boardview(id));

        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board, Model model, MultipartFile file) throws Exception {

        // 게시물 정보 가져오기
        Board boardTemp = boardService.boardview(id);

        // 게시물 정보 업데이트
        boardTemp.setTitle(board.getTitle());
        boardTemp.setContent(board.getContent());

        // 업데이트된 게시물을 저장
        boardService.write(boardTemp, file);

        model.addAttribute("message", "글 수정이 완료됐습니다");
        model.addAttribute("searchUrl", "/board/view?id=" + id);

        return "message";
    }
}
