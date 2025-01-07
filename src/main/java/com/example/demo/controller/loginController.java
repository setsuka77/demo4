package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Users;
import com.example.demo.form.UserForm;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.UsersMapper;

import jakarta.servlet.http.HttpSession;
@Controller
public class loginController {
	
	private final UsersMapper usersMapper;
	private final CartMapper cartMapper;
	
	public loginController(UsersMapper usersMapper,CartMapper cartMapper) {
		this.usersMapper=usersMapper;
		this.cartMapper = cartMapper;
	}
	
	/**
	 * ログイン画面　初期
	 * @return
	 */
	@GetMapping("/login/login")
	public String showLoginPage() {
	    return "login/login"; 
	}
	
	/**
	 * ログインボタン押下時
	 */
	@PostMapping(path="/login/login",params="login")
	public String login(HttpSession session,UserForm userForm,Model model) {
		Integer id = userForm.getId();
		String pass = userForm.getPass();
		Users user = usersMapper.findUser(id);
		if(user!= null && user.getPass().equals(pass)) {
			session.setAttribute("user",user);
			//カート合計検索
			Integer totalItems = cartMapper.getTotalItemsForUser(id);
			session.setAttribute("totalItems", totalItems);
			}
		else {
			model.addAttribute("error", "ユーザーID、パスワードが間違ってます");
			return "login/login"; 
		}
		
		return "redirect:/";
	}
	
	/**
	 * アカウント作成リンク押下
	 */
	@GetMapping("/login/record")
	public String showUserRecord() {
		return "login/record";
	}
	
	/**
	 * 登録ボタン押下
	 * @param session
	 * @param userForm
	 * @param model
	 * @return
	 */
	@Transactional
	@PostMapping(path="/login/record",params="record")
	public String userRecord(HttpSession session,UserForm userForm,Model model) {
		Integer id = userForm.getId();	
		Users user = usersMapper.findUser(id);
		if(user != null) {
			model.addAttribute("error", "そのIDはすでに登録されています");
			return "login/record";
		}
		
		// UserForm を Users エンティティに変換
	    Users newUser = new Users();
	    newUser.setId(id);
	    newUser.setPass(userForm.getPass());
	    newUser.setName(userForm.getName());

	    // データベースに新しいユーザーを挿入
	    usersMapper.insert(newUser);

	    // セッションに登録したユーザーをセット
	    session.setAttribute("user", newUser);

	    // リダイレクトしてメインメニューに遷移
	    return "redirect:/";
	}
	
}
