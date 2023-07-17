package com.example.myapp.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.myapp.user.model.User;
import com.example.myapp.user.service.IUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class UserController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	IUserService userService;

	@RequestMapping(value = "/user/insert", method = RequestMethod.GET)
	public String insertUser(Model model) {
		model.addAttribute("user", new User());
		return "user/form";
	}
	
	@RequestMapping(value = "/user/insert", method = RequestMethod.POST)
	public String insertUser(User user, HttpSession session, Model model) {
	    try {
	        userService.insertUser(user);
	    } catch (DuplicateKeyException e) {
	        user.setUserId(null);
	        model.addAttribute("user", user);
	        model.addAttribute("message", "이미 존재하는 아이디입니다.");
	        return "user/form";
	    }
	    session.invalidate();
	    return "home";
	}

	@RequestMapping(value = "/user/login", method = RequestMethod.GET)
	public String login() {
		return "user/login";
	}

	@RequestMapping(value = "/user/login", method = RequestMethod.POST)
	public String login(String userId, String userPwd, HttpSession session, Model model) {
		User user = userService.selectUser(userId);
		if (user != null) {
			logger.info(user.toString());
			String dbPassword = user.getUserPwd();
			if (dbPassword.equals(userPwd)) { // 비밀번호 일치
				session.setMaxInactiveInterval(600); // 10분
				session.setAttribute("userId", userId);
				session.setAttribute("userName", user.getUserName());
			} else { // 비밀번호가 다름
				session.invalidate();
				model.addAttribute("message", "비밀번호를 잘못 입력하셨습니다. 다시 확인해주세요.");
			}
		} else { // 아이디가 없음
			session.invalidate();
			model.addAttribute("message", "존재하지 않는 아이디입니다. 다시 확인해주세요.");
		}
		return "user/login";
	}

	@RequestMapping(value = "/user/logout", method = RequestMethod.GET)
	public String logout(HttpSession session, HttpServletRequest request) {
		session.invalidate(); // 로그아웃
		return "home";
	}

	@RequestMapping(value = "/user/update", method = RequestMethod.GET)
	public String updateUser(HttpSession session, Model model) {
		String userId = (String) session.getAttribute("userId");
		if (userId != null && !userId.equals("")) {
			User user = userService.selectUser(userId);
			model.addAttribute("user", user);
			model.addAttribute("message", "유저 정보가 업데이트되었습니다.");
			return "user/update";
		} else {
			// userId가 세션에 없을 때 (로그인하지 않았을 때)
			model.addAttribute("message", "로그인 하지 않은 사용자입니다.");
			return "user/login";
		}
	}

	@RequestMapping(value = "/user/update", method = RequestMethod.POST)
	public String updateUser(@Validated User user, BindingResult result, HttpSession session, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("user", user);
			return "user/update";
		}
		try {
			userService.updateUser(user);
			model.addAttribute("message", "회원 정보가 수정됐습니다.");
			model.addAttribute("user", user);
			return "user/login";
		} catch (Exception e) {
			model.addAttribute("message", e.getMessage());
			e.printStackTrace();
			return "user/error";
		}
	}

	@RequestMapping(value = "/user/delete", method = RequestMethod.GET)
	public String deleteUser(HttpSession session, Model model) {
		String userId = (String) session.getAttribute("userId");
		if (userId != null && !userId.equals("")) {
			User user = userService.selectUser(userId);
			model.addAttribute("user", user);
			model.addAttribute("message", "비밀번호 확인");
			return "user/delete";
		} else {
			// userId가 세션에 없을 때 (로그인 하지 않았을 때)
			model.addAttribute("message", "로그인 하지 않은 사용자입니다.");
			return "user/login";
		}
	}

	@RequestMapping(value = "/user/delete", method = RequestMethod.POST)
	public String deleteUser(String userPwd, HttpSession session, Model model) {
		try {
			User user = new User();
			user.setUserId((String) session.getAttribute("userId"));
			String dbpw = userService.getPassword(user.getUserId());
			if (userPwd != null && userPwd.equals(dbpw)) {
				user.setUserPwd(userPwd);
				userService.deleteUser(user);
				model.addAttribute("message", "회원정보를 영구 삭제합니다.");
				session.invalidate();// 삭제되었으면 로그아웃 처리
				return "user/login";
			} else {
				model.addAttribute("message", "비밀번호가 다릅니다.");
				return "user/delete";
			}
		} catch (Exception e) {
			model.addAttribute("message", "회원정보 삭제를 실패하였습니다.");
			e.printStackTrace();
			return "user/delete";
		}
	}
}
