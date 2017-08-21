package com.routon.pmax.admin.privilege.action;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.routon.pmax.admin.privilege.service.PrivilegeService;
import com.routon.pmax.common.UserProfile;
import com.routon.pmax.common.constant.CVal;
import com.routon.pmax.common.model.Menu;
import com.routon.pmax.common.model.User;
import com.routon.pmax.common.utils.AjaxUtil;
import com.routon.pmax.common.utils.JsonMsgBean;
import com.routon.pmax.common.utils.web.SecurityCode;

@Controller
public class HomeController {

	/**
	 * 首页控制器的日志记录器
	 */
	private Logger logger = LoggerFactory.getLogger(HomeController.class);

	/**
	 * 权限管理业务对象
	 */
	@Resource(name = "privilegeServiceImpl")
	private PrivilegeService privilegeService;

	/**
	 * 导航到系统主页控制器
	 * 
	 * @param session
	 *            Session对象，用于当前登录的用户的功能权限
	 * 
	 * @return 系统主页地址
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public ModelAndView welcome(HttpSession session) {
		Map<String, Menu> userPrivilege = (Map<String, Menu>) session
				.getAttribute("userPrivilege");
		logger.info("Go to index page.");

		session.setAttribute("mainmenus", this.privilegeService
				.retrieveCurrentUserMainMenu(userPrivilege));
		//return new ModelAndView("index");
		return new ModelAndView("redirect:/terminal/show.do");
	}

	/**
	 * 生成登陆用的验证码
	 * 
	 * @param response
	 *            Http响应对象，用于输出验证码流
	 * @param session
	 *            Session对象，用于存放验证码
	 * @throws IOException
	 */
	@RequestMapping(value = "/graphics", method = RequestMethod.GET)
	public void securityCode(HttpServletResponse response, HttpSession session)
			throws IOException {
		// SecurityCode sc = SecurityCode.GenerateCode();
		SecurityCode sc = SecurityCode.GenerateCode(4, false, false, false,
				false, new Color(0, 0, 255), new Color(0, 0, 0));
		// 禁止缓存
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "No-cache");
		response.setDateHeader("Expires", 0);
		// 指定生成的响应是图片
		response.setContentType("image/jpeg");

		// 将生成的验证码保存到Session中
		session.setAttribute("randCheckCode", sc.getCode());
		this.logger.info("Generate security code:" + sc.getCode());

		ImageIO.write(sc.getImg(), "GIF", response.getOutputStream());
	}

	/**
	 * 导航到系统登陆页面控制器
	 * 
	 * @return 系统登陆页面
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Model model) {
		this.logger.info("Go to login page.");

		model.addAttribute("user", new User());
		return "login";
	}

	/**
	 * 没有权限时页面跳转请求
	 * 
	 * @return forward to 403.jsp
	 */
	@RequestMapping(value = "/403", method = RequestMethod.GET)
	public String noPermission() {
		this.logger.info("Go to 403 page.");

		return "403";
	}

	/**
	 * 用户登陆验证
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            用户密码
	 * @param securitycode
	 *            验证码
	 * @param request
	 *            当前Http请求, 用于获取成功登录用户的IP
	 * @param session
	 *            当前会话，用于存放成功登录用户的信息和获取验证码
	 * @param model
	 *            推送到视图的模型数据
	 * @return 登录验证成功重定向到首页控制器，反之返回登录错误码：1 验证校验错误，2 用户名或密码校验错误，3 验证码失效
	 */
	@SuppressWarnings("unused")
	@RequestMapping(value = "/login/authen", method = { RequestMethod.POST })
	public String authenLogin(User user, String securitycode,
			HttpServletRequest request, HttpSession session, Model model) {
		String username = user.getUserName();
		String password = user.getPwd();

		this.logger.info("Go to user login authen,name[{}],pwd[{}],sec[{}]",
				username, password, securitycode);
		// 手动校验用户输入数据，不允许为空（可考虑改用SpringMVC框架的校验）
		if (username == null || password == null
		// || securitycode == null
				|| session == null) {
			this.logger
					.info("User login fail, username[{}],password[{}],securitycode[{}], session[{}]",
							username, password, securitycode, session);
			return "login";
			// return new ModelAndView("/login");
		}
		// 检查验证码是否失效
		if (false && session.getAttribute("randCheckCode") == null) {
			model.addAttribute("loginResult", 3);
			this.logger.info("User {} login fail, securitycode invalidation",
					username);
			return "login";
			// return new ModelAndView("/login");
		}
		// 检查验证码输入
		if (false && !securitycode.toLowerCase().equals(
				session.getAttribute("randCheckCode").toString().toLowerCase())) {
			model.addAttribute("loginResult", 1);
			this.logger
					.info("User {} login fail, securitycode incorrect, expected[{}],actual[{}]",
							username, session.getAttribute("randCheckCode")
									.toString().toLowerCase(),
							securitycode.toLowerCase());
			return "login";
			// return new ModelAndView("/login");
		}
		// 检查用户登录
		UserProfile userProfile = null;

		try {
			userProfile = this.privilegeService.userLogin(username, password,
					getIpAddr(request));
		}
		catch (Exception e) {
			this.logger.error("invoker userManager.userLogin exception", e);
		}

		if (userProfile == null || userProfile.getCurrentUserId() <= 0) {
			model.addAttribute("loginResult", 2);
			this.logger.info(
					"User {} login fail, username or password incorrect",
					username);
			return "login";
			// return new ModelAndView("/login");
		}

		session.setAttribute("userProfile", userProfile);
		session.setAttribute("userPrivilege", this.privilegeService
				.buildUserPrivilege(userProfile.getCurrentUserId()));

		// model.addAttribute("loginResult", 0);
		this.logger.info("User {}[{}] login success at {},ip[{}]", username,
				userProfile.getCurrentUserId(), new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(new Date()),
				getIpAddr(request));
		return "redirect:/home.do";
		// return new ModelAndView("redirect:/");
	}

	/**
	 * 系统注销的控制器
	 * 
	 * @param request
	 *            当前的Http请求，用于获取上下文路径
	 * @param session
	 *            当前会话，用于获取当前登录用户的信息
	 * @return 重定向到登录页面
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public RedirectView logout(HttpServletRequest request, HttpSession session) {
		UserProfile userProfile = (UserProfile) session
				.getAttribute("userProfile");

		this.logger.info("User {}[{}] logout.",
				userProfile != null ? userProfile.getCurrentUserLoginName()
						: "",
				userProfile != null ? userProfile.getCurrentUserId() : -1);
		session.invalidate();

		return new RedirectView(request.getContextPath() + "/", false);
	}

	/**
	 * 更改用户密码的控制器
	 * 
	 * @param userId
	 *            待更改密码的用户ID
	 * @param loginName
	 *            待更改密码的用户登录名
	 * @param oldPwd
	 *            原有密码
	 * @param newPwd
	 *            新密码
	 * @param newPwdConfirm
	 *            新密码，确认
	 * @param session
	 *            当前会话
	 * @return 修改成功返回{success: false}，反之返回{success: true}
	 */
	@RequestMapping(value = "/changepwd", method = RequestMethod.POST)
	public String changePassword(int userId, String loginName, String oldPwd,
			String newPwd, String newPwdConfirm, HttpSession session,
			Model model) {

		JsonMsgBean jsonMsg = null;

		this.logger.info("User {}[{}] change password, new[{},{}] old[{}]",
				loginName, userId, newPwd, newPwdConfirm, oldPwd);
		UserProfile userProfile = (UserProfile) session
				.getAttribute("userProfile");

		try {
			this.privilegeService.userChangePassword(userId, oldPwd, newPwd,
					newPwdConfirm, userProfile);
			jsonMsg = new JsonMsgBean(0, CVal.Success, "");
		}
		catch (Exception e) {
			this.logger.error(
					"invoker userManager.userChangePassword exception", e);

			jsonMsg = new JsonMsgBean(0, CVal.Fail, e.getMessage());

		}

		model.addAttribute("jsonMsg", jsonMsg);
		return "common/jsonTextHtml";
	}

	/**
	 * 导航到菜单的控制器
	 * 
	 * @param requestedWith
	 *            ajax请求标示
	 * @param userId
	 *            当前用户ID
	 * @param model
	 *            推送到视图的模型数据
	 * @return 菜单页面地址
	 */
	@RequestMapping(value = "/menu/{userId}", method = RequestMethod.GET)
	public String menu(
			@RequestHeader(value = "X-Requested-With", required = false)
			String requestedWith, @PathVariable
			int userId, Model model) {
		logger.info("To create menu of userId:{}.", userId);
		// List<MenuGroup> menuGroups = new ArrayList<MenuGroup>();
		//
		// try {
		// menuGroups = userManager.getGroupPrlgByUserId(userId);
		// } catch (Exception e) {
		// this.logger.error("invoker userManager.getGroupPrlgByUserId exception",
		// e);
		// }
		//
		// model.addAttribute("menuGroups", menuGroups);
		model.addAttribute("ajaxRequest", AjaxUtil.isAjaxRequest(requestedWith));

		return "staticmenu.jsp";
	}

	/**
	 * 版权（页脚）块内容填充的控制器
	 * 
	 * @return 版权页面
	 */
	@RequestMapping(value = "/copyright", method = RequestMethod.GET)
	public String copyright() {
		logger.info("Get copyright page.");

		return "copyright.jsp";
	}

	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader(" x-forwarded-for ");
		if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
			ip = request.getHeader(" Proxy-Client-IP ");
		}
		if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
			ip = request.getHeader(" WL-Proxy-Client-IP ");
		}
		if (ip == null || ip.length() == 0 || " unknown ".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

}
