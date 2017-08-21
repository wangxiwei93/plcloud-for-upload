/**
 * 
 */
package com.routon.pmax.admin.privilege.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.routon.pmax.admin.privilege.service.log.PrivilegeServiceLog;
import com.routon.pmax.common.EncodeUtils;
import com.routon.pmax.common.UserProfile;
import com.routon.pmax.common.constant.CVal;
import com.routon.pmax.common.model.Menu;
import com.routon.pmax.common.model.RoleMenu;
import com.routon.pmax.common.model.User;
import com.routon.pmax.common.model.UserRole;
import com.routon.pmax.common.persistence.MenuMapper;
import com.routon.pmax.common.persistence.RoleMapper;
import com.routon.pmax.common.persistence.RoleMenuMapper;
import com.routon.pmax.common.persistence.UserMapper;
import com.routon.pmax.common.persistence.UserRoleMapper;
import com.routon.pmax.common.utils.DateUtil;


/**
 * <p>
 * Title: PrivilegeServiceImpl
 * </p>
 * <p>
 * Description: 定义用户功能权限控制业务类
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * <p>
 * Company: Routon Electronic Co.,Ltd.
 * </p>
 * <p>
 * Date: 2013-5-2
 * </p>
 * 
 * @author zhouzhihui
 * @version 1.0
 */
@Service
public class PrivilegeServiceImpl implements PrivilegeService {


	@Autowired
	private EncodeUtils encodeUtil;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private MenuMapper menuMapper;
	
	@Autowired
	private UserRoleMapper userRoleMapper;
	
	@Autowired
	private RoleMapper roleMapper;
	
	@Autowired
	private RoleMenuMapper roleMenuMapper;
	
	@Autowired
	private PrivilegeServiceLog privilegeServiceLog;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.routon.idr.service.privilege.PrivilegeService#userLogin(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	@Transactional(rollbackFor = Exception.class)
	public UserProfile userLogin(String username, String password, String loginIP) {
		UserProfile userProfile = null;
		// 1.检查用户名称是否存在
		List<User> users = userMapper.selectByUsername(username);
		
		if (users == null || users.size() == 0) {
			return null;
		}
		User user = users.get(0);
		// 2.检查用户是否有效
		if (user.getStatus() == CVal.UserStatus.Invalid) {
			return null;
		}
		// 3.检查用户输入的密码是否正确
		if (user.getUserName().equals(username)
				&& encodeUtil.getPasswordMD5(password).equals(
						user.getPwd())) {
			userProfile = new UserProfile();

			userProfile.setCurrentUserLoginIp(loginIP);
			userProfile.setCurrentUserId(user.getId());
			userProfile.setCurrentUserLoginName(user.getUserName());
			userProfile.setCurrentUserRealName(user.getRealName());
			userProfile.setCurrentUserCompany(user.getCompany());
			userProfile.setCurrentUserProject(user.getProject());
			
			if (loginIP != null) {
				user.setLastLoginIP(loginIP);
			}

			//user.setLastLoginTime(DateUtil.getFormatCurrentDate("yyyy-MM-dd HH:mm:ss"));
			user.setLastLoginTime(new Date());
			userMapper.update(user);
			privilegeServiceLog.userLogin(user.getId(), username, password, loginIP);
		}

		return userProfile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.routon.idr.service.privilege.PrivilegeService#userChangePassword(
	 * long, java.lang.String, java.lang.String, java.lang.String,
	 * com.routon.idr.utility.vo.UserProfile)
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean userChangePassword(long userId, String oldPwd, String newPwd,
			String newPwdConfirm, UserProfile optUser) throws Exception {
		// 1.检查用户是否存在
		List<User> users = userMapper.selectById(userId);
		if (users == null) {
			throw new Exception("当前用户"+userId+"不存在，无法完成密码修改！");
		}
		User user = users.get(0);
		if(StringUtils.isBlank(oldPwd)){
			throw new Exception("请输入原始密码！");
		}
		if(StringUtils.isBlank(newPwd)){
			throw new Exception("请输入新密码！");
		}
		if(StringUtils.isBlank(newPwdConfirm)){
			throw new Exception("请再次输入新密码！");
		}
		// 2.检查用户输入的原始密码是否正确
		if (oldPwd == null
				|| (StringUtils.isNotEmpty(oldPwd) && !encodeUtil.getPasswordMD5(oldPwd).equals(user.getPwd()))) {
			throw new Exception("原始密码输入不正确，无法完成密码修改！");
		}
		// 3.检查用户两次输入新密码是否匹配
		if (newPwd == null || newPwdConfirm == null || !newPwd.equals(newPwdConfirm)) {
			throw new Exception("新密码两次输入不一致，无法完成密码修改");
		}
		
		user.setPwd(encodeUtil.getPasswordMD5(newPwd));
		user.setModifyTime(new Date());
		userMapper.update(user);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.routon.idr.service.privilege.PrivilegeService#buildUserPrivilege(
	 * java.lang.Long)
	 */
	@Transactional(readOnly = true)
	public Map<String, Menu> buildUserPrivilege(Long userId) {
		List<User> users = userMapper.selectById(userId);
		User user = users.get(0);
		
		Map<String, Menu> userPrivilege = new ConcurrentHashMap<String, Menu>();

		if (user == null) {
			return userPrivilege;
		}

		List<UserRole> systemuserroles = userRoleMapper.selectByUserId(userId);

		if (systemuserroles == null) {
			return userPrivilege;
		}

		for (UserRole systemuserrole : systemuserroles) {
			

			List<RoleMenu> roleMenus = roleMenuMapper.selectByRoleID(systemuserrole.getRoleId()); 
			
			if (roleMenus == null) {
				continue;
			}

			for (RoleMenu roleMenu : roleMenus) {
				List<Menu>  systemmenus = menuMapper.selectByMenuID(roleMenu.getMenuID());
				Menu systemmenu = systemmenus.get(0);
				String menuId = systemmenu.getId() + "";
				String menuPath = systemmenu.getPath();
				boolean isInUserPrivilege = false;
				
				if (StringUtils.isBlank(systemmenu.getPath())) {
					userPrivilege.put(menuId, systemmenu);
					continue;
				}

//				for (Iterator<String> iterator = userPrivilege.keySet().iterator(); iterator
//						.hasNext();) {
//					String userPrivilegeKey = iterator.next();
//					String userPrivilegeValue = userPrivilege.get(userPrivilegeKey);
//					String userPrivilegeKeyPrefix = userPrivilegeKey;
//					Long userPrivilegeKeySuffix = 0L;
//					String menuCodePrefix = menuCode;
//					Long menuCodeSuffix = 0L;
//
//					if (userPrivilegeKey.length() > 2) {
//						userPrivilegeKeyPrefix = userPrivilegeKey.substring(0, 2);
//						userPrivilegeKeySuffix = NumberUtils.toLong(userPrivilegeKey.substring(2));
//					}
//					if (menuCode.length() > 2) {
//						menuCodePrefix = menuCode.substring(0, 2);
//						menuCodeSuffix = NumberUtils.toLong(menuCode.substring(2));
//					}
//
//					if (userPrivilegeKeyPrefix.equals(menuCodePrefix)) {
//						isInUserPrivilege = true;
//						userPrivilegeKeyPrefix = userPrivilegeKeyPrefix
//								+ (userPrivilegeKeySuffix | menuCodeSuffix);
//						String[] temps = menuPath.startsWith("/") ? menuPath.substring(1)
//								.split("/") : menuPath.split("/");
//						String subParentMenuPath = "";
//						String[] menuPaths = new String[2];
//
//						if (temps.length > 2) {
//							String temp = "";
//
//							for (int i = 1; i < temps.length; i++) {
//								temp += temps[i] + "/";
//							}
//
//							menuPaths[0] = temps[0];
//							menuPaths[1] = temp.substring(0, temp.length() - 1);
//						} else {
//							menuPaths = temps;
//						}
//
//						for (int i = 0; i < menuPaths.length; i++) {
//							String subMenuPath = menuPaths[i];
//							int index = userPrivilegeValue.indexOf(subMenuPath);
//
//							if (i > 0) {
//								index = userPrivilegeValue.indexOf("|" + subMenuPath + "|");
//							}
//
//							if (index > 0) {
//								subParentMenuPath = subMenuPath;
//								continue;
//							}
//
//							if (userPrivilegeValue.indexOf(subParentMenuPath + "/(") > 0) {
//								int offset = userPrivilegeValue.indexOf(subParentMenuPath + "/(");
//								StringBuilder temp = new StringBuilder(userPrivilegeValue);
//
//								temp.insert(offset + (subParentMenuPath + "/(").length(),
//										subMenuPath + "|");
//								userPrivilegeValue = temp.toString();
//								subParentMenuPath = subMenuPath;
//							} else if (userPrivilegeValue.indexOf(subParentMenuPath + "|") > 0) {
//								int offset = userPrivilegeValue.indexOf(subParentMenuPath + "|");
//								StringBuilder temp = new StringBuilder(userPrivilegeValue);
//
//								temp.insert(offset + subParentMenuPath.length(), "/(" + subMenuPath
//										+ "|)");
//								userPrivilegeValue = temp.toString();
//								subParentMenuPath = subMenuPath;
//							}
//						}
//
//						iterator.remove();
//						userPrivilege.put(userPrivilegeKeyPrefix, userPrivilegeValue);
//						this.logger.info("UserPrivilege Map<{}, {}>", userPrivilegeKeyPrefix,
//								userPrivilegeValue);
//						break;
//					}
//				}

				if (!isInUserPrivilege) {
					String[] menuPaths = menuPath.split("/");
					String menuPathRegex = StringUtils.join(menuPaths, "/(") + "|";

					for (int i = 1; i < menuPaths.length; i++) {
						menuPathRegex += ")";
					}
					systemmenu.setMenuPathRegex(menuPathRegex);
					userPrivilege.put(menuId, systemmenu);
					this.logger.info("UserPrivilege Map<{}, {}>", menuId, menuPathRegex);
					continue;
				}
			}
		}

		return userPrivilege;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.routon.idr.service.privilege.PrivilegeService#retrieveCurrentUserMainMenu
	 * (java.util.Map)
	 */
	@Transactional(readOnly = true)
	public List<Menu> retrieveCurrentUserMainMenu(Map<String, Menu> userPrivilege) {
		
		try {
			Map<String, Menu> userMenuMap = new HashMap<String, Menu>();
			Iterator<String> userPrivilegeiterator = userPrivilege.keySet().iterator();
			
			while (userPrivilegeiterator.hasNext()) {
				String id = userPrivilegeiterator.next();
				Menu menu = userPrivilege.get(id);
				userMenuMap.put(id, menu);
			}
		
//			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(); 
//			ObjectOutputStream out = new ObjectOutputStream(byteOut); 
//			out.writeObject(userPrivilege); 
//	
//			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray()); 
//			ObjectInputStream in =new ObjectInputStream(byteIn); 
//			Map<String, Menu> userMenuMap = (Map<String, Menu>)in.readObject();
			
			
			
			Iterator<String> iterator = userMenuMap.keySet().iterator();
			Set<Long> removeIds = new HashSet<Long>();
			while (iterator.hasNext()) {
				String id = iterator.next();
				Menu menu = userMenuMap.get(id);
				if (menu.getCode() > 0) {
	
					Menu parentMenu = userMenuMap.get(menu.getpId()+"");
		
					if (parentMenu != null) {
						TreeSet<Menu> parentschild = parentMenu.getSystemmenus();
								
						if (parentschild == null) {
							parentschild  = new TreeSet<Menu>(new Comparator<Menu>() {
								@Override
								public int compare(Menu o1, Menu o2) {
									return o1.getOrder() - o2.getOrder();
								}
							});
						}
						
						parentschild.add(menu);
						parentMenu.setSystemmenus(parentschild);
						removeIds.add(Long.parseLong(id));
						// iterator.remove();
					}
				}
				else {
					removeIds.add(Long.parseLong(id));
				}
			}
			for (Long id : removeIds) {
				userMenuMap.remove(id + "");
			}
			
			ArrayList<Menu> userMenu = new ArrayList<Menu>(userMenuMap.values());
			Collections.sort(userMenu, new Comparator<Menu>() {

				@Override
				public int compare(Menu o1, Menu o2) {

					return o1.getOrder() - o2.getOrder();
				}
			});
	
			return userMenu;
		}
		catch(Exception e){
			logger.error("", e);
			return new ArrayList<Menu>();
		}
		
	}
}
