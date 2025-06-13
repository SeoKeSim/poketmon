package sks.poketmon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sks.poketmon.dto.user.RegisterRequestDto;
import sks.poketmon.dto.user.RegisterResponseDto;
import sks.poketmon.service.UserService;

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
     * 사용자 ID 중복 체크 (AJAX용)
     * GET /users/check-id?userId=test123
     */
    @GetMapping("/check-id")
    @ResponseBody
    public boolean checkUserId(@RequestParam String userId) {
        return userService.isUserIdExists(userId);
    }
}