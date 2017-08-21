package com.routon.pmax.admin.privilege.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.routon.pmax.admin.privilege.model.TreeBean;
import com.routon.pmax.admin.privilege.service.GroupService;
import com.routon.pmax.admin.privilege.service.RoleService;
import com.routon.pmax.admin.privilege.service.UserService;
import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.PmaxException;
import com.routon.pmax.common.UserProfile;
import com.routon.pmax.common.constant.CVal;
import com.routon.pmax.common.model.AuthType;
import com.routon.pmax.common.model.Role;
import com.routon.pmax.common.model.User;
import com.routon.pmax.common.service.MessageServiceImpl;
import com.routon.pmax.common.utils.JsonMsgBean;

@Controller
@SessionAttributes(value = { "userPrivilege", "userProfile" })
public class UserController {

	private Logger logger = LoggerFactory.getLogger(UserController.class);

	private final String RMPATH = "/user/";

	@Resource(name = "userServiceImpl")
	private UserService userService;

	@Resource(name = "roleServiceImpl")
	private RoleService roleService;

	@Resource(name = "messageServiceImpl")
	protected MessageServiceImpl messageService;

	@Resource(name = "groupServiceImpl")
	private GroupService groupService;

	@RequestMapping(value = RMPATH + "list")
	public String list(HttpServletRequest request, User queryCondition,
			@ModelAttribute("userProfile")
			UserProfile user, Model model, HttpSession session) {

		logger.debug("list");
		try {
			Long loginUserId = user.getCurrentUserId();
			int page = NumberUtils.toInt(request.getParameter("page"), 1);
			int pageSize = NumberUtils.toInt(request.getParameter("pageSize"),
					10);
			int startIndex = (page - 1) * pageSize;

			String groupIds = userService.getGroupIdsByUserId(loginUserId);
			queryCondition.setGroupIds(groupIds);

			PagingBean<User> pagingBean = userService.paging(startIndex,
					pageSize, request.getParameter("sort"),
					request.getParameter("dir"), queryCondition, null, null,
					loginUserId,request.getParameter("exportflag") != null&&request.getParameter("exportflag").equals("true")?true:false);

			int maxpage = (int) Math.ceil(pagingBean.getTotalCount()
					/ (double) pageSize);
			if (pagingBean.getTotalCount() == 0) {
				maxpage = 0;
			}
			if (StringUtils.isNotBlank(queryCondition.getUserName())) {
				model.addAttribute("userName", queryCondition.getUserName());
			}
			if (StringUtils.isNotBlank(queryCondition.getRealName())) {
				model.addAttribute("realName", queryCondition.getRealName());
			}

			model.addAttribute("maxpage", maxpage);
			model.addAttribute("page", page);
			model.addAttribute("pageList", pagingBean);

			// addCatalogAttribute(catalogId, model);
		}
		catch (Exception e) {
			logger.error("错误", e);
		}

		return "user/list";
	}

	/**
	 * 6.2.12 新增
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = RMPATH + "add", method = RequestMethod.GET)
	public String setupAdd(Model model, @ModelAttribute("userProfile")
	UserProfile userProfile, HttpServletRequest request) {

		User user = new User();
		Long loginUserId = userProfile.getCurrentUserId();

		List<Role> roles = roleService.getRoles(loginUserId);

		List<TreeBean> groupTreeBeans = groupService.getGroupTreeByUserId(
				loginUserId, null, null);

		int page = NumberUtils.toInt(request.getParameter("page"), 1);
		
		ArrayList<AuthType> list = userService.getAuthType();
		
		HashSet<String> companyNameSet = new HashSet<String>();
		
		for(int i = 0; i < list.size(); i++){
			companyNameSet.add(list.get(i).getCompany_name());
		}
		
		model.addAttribute("companyNameSet",companyNameSet);
		model.addAttribute("ClientType", list);
		model.addAttribute("page", page);
		model.addAttribute("roles", roles);
		model.addAttribute("user", user);
		model.addAttribute("groupTreeBeans",
				JSONArray.fromObject(groupTreeBeans).toString());
		return "user/edit";
	}

	@RequestMapping(value = RMPATH + "edit", method = RequestMethod.GET)
	public String setupEdit(Model model, @ModelAttribute("userProfile")
	UserProfile userProfile, Long id, HttpServletRequest request) {

		User user = userService.getUserByUserId(id);
		HashSet<Long> userRoleIdSet = user.getRoleIdset();

		Long loginUserId = userProfile.getCurrentUserId();
		List<TreeBean> groupTreeBeans = groupService.getGroupTreeByUserId(
				loginUserId, null, id);

		List<Role> roles = roleService.getRoles(loginUserId);
		for (Role role : roles) {
			if (userRoleIdSet.contains(role.getId())) {
				role.setChecked(true);
			}
		}
		
		ArrayList<AuthType> list = userService.getAuthType();
		HashSet<String> companyNameSet = new HashSet<String>();
		
		for(int i = 0; i < list.size(); i++){
			companyNameSet.add(list.get(i).getCompany_name());
		}
		
		int page = NumberUtils.toInt(request.getParameter("page"), 1);
		model.addAttribute("companyNameSet",companyNameSet);
		model.addAttribute("ClientType", list);
		model.addAttribute("page", page);
		model.addAttribute("roles", roles);
		model.addAttribute("user", user);
		model.addAttribute("groupTreeBeans",
				JSONArray.fromObject(groupTreeBeans).toString());

		return "user/edit";
	}

	/**
	 * 用户重置密码
	 * 
	 * @param id
	 *            待重置密码的用户ID
	 * @param optUser
	 *            当前操作用户
	 * @return ControllerResult 处理结果
	 */
	@RequestMapping(value = "user/resetPwd", method = RequestMethod.POST)
	public String resetPwd(String id, @ModelAttribute("userProfile")
	UserProfile optUser, Model model) {
		JsonMsgBean jsonMsg = null;
		try {
			userService.resetPwd(id, optUser);
			jsonMsg = new JsonMsgBean(0, CVal.Success, "");
		}
		catch (PmaxException e) {
			logger.error("重置密码以下用户\"{" + id + "}\"时异常", e);
			jsonMsg = new JsonMsgBean(0, CVal.Fail, e.getMessage());

		}
		catch (Exception e) {
			logger.error("重置密码以下用户\"{" + id + "}\"时异常", e);
			jsonMsg = new JsonMsgBean(0, CVal.Exception, "重置密码时异常!");

		}

		model.addAttribute("jsonMsg", jsonMsg);
		return "common/jsonTextHtml";

	}

	/**
	 * 删除用户
	 * 
	 * @param id
	 *            待删除用户的ID
	 * @param optUser
	 *            当前操作用户
	 * @return ControllerResult 处理结果
	 */
	@RequestMapping(value = "user/delete", method = RequestMethod.POST)
	public String delUser(String id, @ModelAttribute("userProfile")
	UserProfile optUser, Model model) {
		JsonMsgBean jsonMsg = null;
		try {
			Long loginUserId = optUser.getCurrentUserId();
			String userId_array[] = id.split(",");
			String delUserIds = "";
			boolean isContainLoginUserId = false;

			for (String userId : userId_array) {
				if (Long.parseLong(userId) == loginUserId.longValue()) {
					isContainLoginUserId = true;
					continue;
				}

				if (delUserIds.equals("")) {
					delUserIds += userId;
				}
				else {
					delUserIds += ",";
					delUserIds += userId;
				}

			}

			if (!isContainLoginUserId) {
				userService.delete(delUserIds, optUser);
				this.logger.info("成功删除了以下用户：{}", delUserIds);
				jsonMsg = new JsonMsgBean(0, CVal.Success, "");

			}
			else {
				if (!delUserIds.equals("")) {
					userService.delete(delUserIds, optUser);
					this.logger.info("成功删除了以下用户：{}", delUserIds);
					jsonMsg = new JsonMsgBean(0, CVal.Fail,
							"所选用户中包含当前登陆用户未能删除,其他用户已删除成功!");
					// return new ResultBean(-2,
					// "所选用户中包含当前登陆用户未能删除,其他用户已删除成功!");
				}
				else {
					jsonMsg = new JsonMsgBean(0, CVal.Fail, "删除的用户为当前登陆用户不能删除!");

				}
			}

		}
		catch (Exception e) {
			logger.error("删除以下用户\"{" + id + "}\"时异常", e);
			jsonMsg = new JsonMsgBean(0, CVal.Fail, "删除系统用户异常!");

		}

		model.addAttribute("jsonMsg", jsonMsg);
		return "common/jsonTextHtml";

	}

	@RequestMapping(value = RMPATH + "save", method = RequestMethod.POST)
	public String save(@Valid
	User user, BindingResult result,
			MultipartHttpServletRequest multipartRequest, Model model,
			@ModelAttribute("userProfile")
			UserProfile userProfile) {
		JsonMsgBean jsonMsg = null;

/*		String username = user.getUserName();
		List<User> listUser = userService.queryUserbyName(username);
		if(listUser.size() > 0){
			jsonMsg = new JsonMsgBean(0, CVal.Fail, "该用户名已存在!");
			model.addAttribute("jsonMsg", jsonMsg);
			return "common/jsonTextHtml";
		}*/
		do {
			try {

				if (result.hasErrors()) {

					jsonMsg = new JsonMsgBean(0, CVal.Fail,
							messageService.getOneMessage(result, "userName",
									"realName", "phone", "roleIds", "groupIds", "company", "project"));
					break;
				}
				Long id = null;
				logger.info("提交");
				if (user.getId() != null && user.getId() > 0) {
					id = userService.edit(user, user.getGroupIds(),
							user.getRoleIds(), userProfile);
				}
				else {

					id = userService.add(user, user.getGroupIds(),
							user.getRoleIds(), userProfile);
				}

				if (id > 0) {

					jsonMsg = new JsonMsgBean(0, CVal.Success, "");
				}
				else if (id == -2) {
					jsonMsg = new JsonMsgBean(0, CVal.Fail, "角色名已经存在!");
				}
				else {
					jsonMsg = new JsonMsgBean(0, CVal.Fail, "保存角色失败!");
				}

			}
			catch (Exception e) {
				logger.error("角色保存异常", e);
				jsonMsg = new JsonMsgBean(0, CVal.Exception, "保存失败，请稍候重试!");
			}
		} while (false);

		model.addAttribute("jsonMsg", jsonMsg);

		return "common/jsonTextHtml";
	}
	
	@RequestMapping(value = RMPATH + "queryproject", method = RequestMethod.POST)
	public @ResponseBody 
	List<AuthType> queryProject(Model model, @ModelAttribute("userProfile")
	UserProfile userProfile, HttpServletRequest request, String data) {
		List<AuthType> list = userService.queryProjectByCompany(data);
		return list;

	}
}
