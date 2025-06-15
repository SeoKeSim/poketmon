package sks.poketmon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sks.poketmon.dto.user.LoginRequestDto;
import sks.poketmon.dto.user.LoginResponseDto;
import sks.poketmon.dto.user.RegisterRequestDto;
import sks.poketmon.dto.user.RegisterResponseDto;
import sks.poketmon.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 회원가입 폼 페이지
     * GET /users/register
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        RegisterRequestDto user = new RegisterRequestDto();
        // 빈 값으로 초기화하여 템플릿 에러 방지
        user.setUserId("");
        user.setUserName("");
        model.addAttribute("user", user);
        return "register";
    }

    /**
     * 회원가입 처리
     * POST /users/register
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequestDto request,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        RegisterResponseDto response = userService.registerUser(request);

        if (response.getUserCode() != null) {
            // 회원가입 성공
            redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            redirectAttributes.addFlashAttribute("userName", response.getUserName());
            return "redirect:/users/register-success";
        } else {
            // 회원가입 실패
            model.addAttribute("errorMessage", response.getMessage());
            model.addAttribute("user", request);
            return "register";
        }
    }

    /**
     * 회원가입 성공 페이지
     * GET /users/register-success
     */
    @GetMapping("/register-success")
    public String showRegisterSuccess() {
        return "register-success";
    }

    /**
     * 로그인 폼 페이지
     * GET /users/login
     */
    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        // 이미 로그인된 경우 메인 페이지로 리다이렉트
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/";
        }

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUserId("");
        model.addAttribute("loginRequest", loginRequest);
        return "login";
    }

    /**
     * 로그인 처리
     * POST /users/login
     */
    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequestDto request,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        LoginResponseDto response = userService.loginUser(request);

        if (response.isSuccess()) {
            // 로그인 성공 - 세션에 사용자 정보 저장
            session.setAttribute("loginUser", response);
            redirectAttributes.addFlashAttribute("successMessage",
                    response.getUserName() + "님, 환영합니다!");
            return "redirect:/";
        } else {
            // 로그인 실패
            model.addAttribute("errorMessage", response.getMessage());
            model.addAttribute("loginRequest", request);
            return "login";
        }
    }

    /**
     * 로그아웃 처리
     * POST /users/logout
     */
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("loginUser");
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "로그아웃되었습니다.");
        return "redirect:/";
    }

    /**
     * 사용자 ID 중복 체크 (AJAX용)
     * GET /users/check-id?userId=test123
     */
    @GetMapping("/check-id")
    @ResponseBody
    public boolean checkUserId(@RequestParam String userId) {
        return userService.isUserIdExists(userId);
    }
}