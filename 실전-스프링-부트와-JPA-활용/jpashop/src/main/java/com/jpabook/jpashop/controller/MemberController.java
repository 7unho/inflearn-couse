package com.jpabook.jpashop.controller;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm memberForm, BindingResult result) { // @Valid 어노테이션을 사용하면, memberForm의 데이터를 validation 해줌.
        /**
         *  BindingResult := 검증 오류를 보관하는 객체
         *
         *  hasErrors() 검증 과정에서 에러가 발생된다면 -> 다시 createMemberForm 호출
         */
        if(result.hasErrors()) return "members/createMemberForm";

        Address address = new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipCode());

        Member member = new Member();
        member.setName(memberForm.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }
}
