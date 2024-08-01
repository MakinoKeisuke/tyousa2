package com.example.nagoyameshi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Member;
import com.example.nagoyameshi.repository.MemberRepository;
import com.example.nagoyameshi.repository.ShopeRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.MemberService;
@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final ShopeRepository shopeRepository;
	
	
	public SubscriptionController(MemberRepository memberRepository, MemberService memberService, ShopeRepository shopeRepository) {
		this.memberRepository = memberRepository;
		this.memberService = memberService;
		this.shopeRepository = shopeRepository;
	}
	
	@GetMapping("/cancel")
	public String cancel() {
		return "subscription/cancel";
	}
	
	/*@GetMapping("/create")
	public String createMenyu(
            @ModelAttribute ReservationInputForm reservationInputForm,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            HttpServletRequest httpServletRequest,
            Model model) {
     // データベースからshopeオブジェクトを取得する
		Shope shope = shopeRepository.findById(1).orElse(null);

     // shopeがnullの場合、エラーハンドリング
     if (shope == null) {
         model.addAttribute("errorMessage", "指定された店舗が見つかりません。");
         return "redirect:/";
     }

     Member member = userDetailsImpl.getMember();

     //予約日と予約時間を取得する
     String fromReservationDate = reservationInputForm.getFromReservationDate();
     String fromReservationTime = reservationInputForm.getFromReservationTime();
     Integer numberOfPeople = reservationInputForm.getNumberOfPeople();
     if (numberOfPeople == null) {
         numberOfPeople = 1;  // デフォルト値を設定
     }
     ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm(
             shope.getId(), member.getId(), fromReservationDate, fromReservationTime, reservationInputForm.getNumberOfPeople());

     String sessionId = stripeService.createStripeSession(shope.getName(), reservationRegisterForm, httpServletRequest);

     model.addAttribute("shope", shope);
     model.addAttribute("reservationRegisterForm", reservationRegisterForm);
     model.addAttribute("sessionId", sessionId);

     return "subscription/create"
 }*/
	
	/*public String createMenyu(
			   @ModelAttribute ReservationInputForm reservationInputForm,
			   @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			   HttpServletRequest httpServletRequest,
			   Model model) {
		Shope shope = new Shope();
		Member member = userDetailsImpl.getMember();

		//予約日と予約時間を取得する
		String fromReservationDate = reservationInputForm.getFromReservationDate();
		String fromReservationTime = reservationInputForm.getFromReservationTime();

		ReservationRegisterForm  reservationRegisterForm = new ReservationRegisterForm(shope.getId(),member.getId(),fromReservationDate,fromReservationTime,reservationInputForm.getNumberOfPeople());

		String sessionId = stripeService.createStripeSession(shope.getName(), reservationRegisterForm, httpServletRequest);

		model.addAttribute("shope", shope);
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		model.addAttribute("sessionId", sessionId);

		return "subscription/create";
	}*/
	/*public String createMenyu(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,HttpServletRequest httpServletRequest, Model model, RedirectAttributes redirectAttributes) {
		Member member = memberRepository.getReferenceById(userDetailsImpl.getMember().getId());
		memberService.createSubscription(member);
		
		ReservationRegisterForm  reservationRegisterForm = new ReservationRegisterForm(shope.getId(),member.getId(),fromReservationDate,fromReservationTime,reservationInputForm.getNumberOfPeople());
		
		String sessionId = stripeService.createStripeSession(shope.getName(), reservationRegisterForm, httpServletRequest);
		model.addAttribute("successMessage", "有料会員を登録しました、");
		model.addAttribute("sessionId", sessionId);
		
		return "subscription/create";
		
	}*/
	@GetMapping("/create")
	public String createMenyu(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model, RedirectAttributes redirectAttributes) {
		Member member = memberRepository.getReferenceById(userDetailsImpl.getMember().getId());
		memberService.createSubscription(member);
		
		model.addAttribute("successMessage", "有料会員を登録してください。");
		
		return "subscription/create";
		
	}
	
	@PostMapping("/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model, RedirectAttributes redirectAttributes) {
		Member member = memberRepository.getReferenceById(userDetailsImpl.getMember().getId());
		memberService.createSubscription(member);
		
		redirectAttributes.addFlashAttribute("successMessage", "有料会員を登録しました");
		
		return "redirect:/";
		
	}
	
	@GetMapping("/delete")
	public String deleteMenyu(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model, RedirectAttributes redirectAttributes) {
		//Member member = memberRepository.getReferenceById(userDetailsImpl.getMember().getId());
		//memberService.cancelSubscription(member);
		
		//model.addAttribute("successMessage", "有料会員を削除しました");
		
		return "subscription/cancel";
	}
	
	@PostMapping("/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model, RedirectAttributes redirectAttributes) {
		Member member = memberRepository.getReferenceById(userDetailsImpl.getMember().getId());
		memberService.cancelSubscription(member);
		
		redirectAttributes.addFlashAttribute("successMessage", "有料会員を解約しました");
		
		return "redirect:/";
	}
	

	
}
