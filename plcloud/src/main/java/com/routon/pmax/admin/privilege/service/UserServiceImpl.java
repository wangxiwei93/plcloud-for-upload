package com.routon.pmax.admin.privilege.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.routon.pmax.admin.privilege.service.log.UserServiceLog;
import com.routon.pmax.common.EncodeUtils;
import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.PagingSortDirection;
import com.routon.pmax.common.PmaxException;
import com.routon.pmax.common.UserProfile;
import com.routon.pmax.common.constant.CVal;
import com.routon.pmax.common.dao.mybatis.PagingDaoMybatis;
import com.routon.pmax.common.model.AuthType;
import com.routon.pmax.common.model.Group;
import com.routon.pmax.common.model.Role;
import com.routon.pmax.common.model.User;
import com.routon.pmax.common.model.UserGroup;
import com.routon.pmax.common.model.UserRole;
import com.routon.pmax.common.persistence.AuthTypeMapper;
import com.routon.pmax.common.persistence.GroupMapper;
import com.routon.pmax.common.persistence.RoleMapper;
import com.routon.pmax.common.persistence.UserGroupMapper;
import com.routon.pmax.common.persistence.UserMapper;
import com.routon.pmax.common.persistence.UserRoleMapper;

/**
 * 
 * <p>
 * Title: UserServiceImpl
 * </p>
 * <p>
 * Description: 定义系统用户业务的实现类
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * <p>
 * Company:
 * </p>
 * <p>
 * Date: 2013-5-13
 * </p>
 * 
 * @author
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private EncodeUtils encodeUtil;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
	private GroupMapper groupMapper;

	@Autowired
	private UserRoleMapper userRoleMapper;

	@Autowired
	private UserGroupMapper userGroupMapper;

	@Autowired
	private EncodeUtils encodeUtils;
	
	@Autowired
	private AuthTypeMapper authTypeMapper;

	@Resource(name = "pagingDaoMybatis")
	private PagingDaoMybatis pagingDao;

	@Autowired
	private UserServiceLog userServiceLog;
	
	private String newPwd = "111111";

	
	/**
	 * 
	 * @param systemuser
	 * @return
	 */
	private Long saveSystemUser(User systemuser) {
		String username = systemuser.getUserName();
		boolean isExist = userNameExist(username);

		if (isExist) {
			logger.info("新增用户时,用户名已经存在");
			return -2l;
		}
		systemuser.setPwd(newPwd);
		String beforeMD5 = systemuser.getPwd();
		// systemuser.setPswSalt(encodeUtils.getEncodedSalt());
		systemuser.setPwd(encodeUtils.getPasswordMD5(beforeMD5));

		if (systemuser.getPwd().equals(beforeMD5)) {
			logger.info("新增用户时,密码加密时异常");
			return -1l;
		}

		systemuser.setCreateTime(new Date());
		systemuser.setModifyTime(new Date());
		systemuser.setStatus(CVal.UserStatus.valid);
		try {
			long userId = userMapper.insert(systemuser);
			userId = systemuser.getId();
			if (userId > 0) {
				return userId;
			}

			return -1l;
		}
		catch (Exception e) {
			logger.error("saveSystemUser异常", e);

			return -1l;
		}

	}

	/**
	 * 
	 * @param systemuser
	 * @return
	 */
	private Long updateSystemUser(User systemuser) {

		List<User> tem_systemusers = userMapper.selectById(systemuser.getId());
		User tem_systemuser = tem_systemusers.get(0);
		boolean isExist = userNameExist(tem_systemuser.getUserName());

		if (isExist) {
			logger.info("用户名已经存在");
			return -2l;
		}
		tem_systemuser.setUserName(systemuser.getUserName());
		tem_systemuser.setRealName(systemuser.getRealName());
		tem_systemuser.setCompany(systemuser.getCompany());
		tem_systemuser.setPhone(systemuser.getPhone());
		tem_systemuser.setModifyTime(new Date());
		tem_systemuser.setProject(systemuser.getProject());

		userMapper.update(tem_systemuser);

		return systemuser.getId();
	}

	private void fillSystemuser(User systemuser) {
		String role_texts = "";
		String roleIds = "";
		String project_texts = "";
		String projectIds = "";

		List<Role> roles = roleMapper
				.selectBySql("SELECT a.* FROM role a, userrole b WHERE a.id = b.roleId AND b.userId ="
						+ systemuser.getId());
		if (roles != null) {

			for (Role systemuserrole : roles) {
				String roleName = systemuserrole.getName();
				Long roleId = systemuserrole.getId();

				if (StringUtils.isNotBlank(roleName)) {

					if (StringUtils.isNotBlank(role_texts)) {
						role_texts += ",";
						roleIds += ",";
						role_texts += roleName;
						roleIds += roleId;
					}
					else {
						role_texts += roleName;
						roleIds += roleId;
					}

					systemuser.getRoleIdset().add(roleId);
				}

			}
		}

		systemuser.setRoleIds(roleIds);
		systemuser.setRole_texts(role_texts);

		List<Group> groups = groupMapper
				.selectBySql("SELECT a.* FROM groups a, usergroup b WHERE a.id = b.groupId AND b.userId ="
						+ systemuser.getId());

		if (groups != null) {

			for (Group systemuserproject : groups) {
				String projectName = systemuserproject.getName();
				Long projectId = systemuserproject.getId();

				if (StringUtils.isNotBlank(projectName)) {

					if (StringUtils.isNotBlank(project_texts)) {
						project_texts += ",";
						project_texts += projectName;

						projectIds += ",";
						projectIds += projectId;
					}
					else {
						projectIds += projectId;
						project_texts += projectName;
					}
				}

			}
		}

		systemuser.setGroupIds(projectIds);
		systemuser.setGroup_texts(project_texts);

	}

	@Transactional(rollbackFor = Exception.class)
	public Long add(User systemuser, String groupIds, String roleIds,
			UserProfile optUser) {
		Long userId = saveSystemUser(systemuser);

		if (userId > 0) {
			String groupId_array[] = groupIds.split(",");
			for (String groupId : groupId_array) {
				UserGroup userGroup = new UserGroup();
				userGroup.setGroupId(Long.parseLong(groupId));
				userGroup.setUserId(userId);

				userGroupMapper.insert(userGroup);
			}

			String roleId_array[] = roleIds.split(",");
			for (String roleId : roleId_array) {

				if (StringUtils.isNotBlank(roleId)) {

					UserRole userRole = new UserRole();
					userRole.setModifyTime(new Date());
					userRole.setRoleId(Long.parseLong(roleId));
					userRole.setUserId(userId);

					userRoleMapper.insert(userRole);
				}
			}
			
			userServiceLog.add(systemuser, groupIds, roleIds, optUser);

			return userId;

		}
		else {
			return userId;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public Long edit(User systemuser, String groupIds, String roleIds,
			UserProfile optUser) {
		Long userId = updateSystemUser(systemuser);

		if (userId > 0) {
			userRoleMapper.deleteByUserId(userId);
			userGroupMapper.deleteByUserId(userId);

			String groupId_array[] = groupIds.split(",");
			for (String groupId : groupId_array) {
				UserGroup userGroup = new UserGroup();
				userGroup.setGroupId(Long.parseLong(groupId));
				userGroup.setUserId(userId);

				userGroupMapper.insert(userGroup);
			}

			String roleId_array[] = roleIds.split(",");
			for (String roleId : roleId_array) {
				if (StringUtils.isNotBlank(roleId)) {

					UserRole userRole = new UserRole();
					userRole.setModifyTime(new Date());
					userRole.setRoleId(Long.parseLong(roleId));
					userRole.setUserId(userId);

					userRoleMapper.insert(userRole);
				}
			}

			userServiceLog.edit(systemuser, groupIds, roleIds, optUser);
			return userId;
		}
		else {
			return userId;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void assignSystemUserRole(String userIds, String roleIds) {
		String userId_array[] = userIds.split(",");

		for (String userId : userId_array) {

			String roleId_array[] = roleIds.split(",");
			userRoleMapper.deleteByUserId(Long.parseLong(userId));

			for (String roleId : roleId_array) {
				UserRole userRole = new UserRole();
				userRole.setModifyTime(new Date());
				userRole.setRoleId(Long.parseLong(roleId));
				userRole.setUserId(Long.parseLong(userId));
				userRoleMapper.insert(userRole);
			}
		}

	}

	@Transactional(rollbackFor = Exception.class)
	public void assignSystemUserGroup(String userIds, String groupIds) {
		String userId_array[] = userIds.split(",");

		for (String userId : userId_array) {

			String groupId_array[] = groupIds.split(",");
			userGroupMapper.deleteByUserId(Long.parseLong(userId));

			for (String groupId : groupId_array) {

				UserGroup userGroup = new UserGroup();
				userGroup.setGroupId(Long.parseLong(groupId));
				userGroup.setUserId(Long.parseLong(userId));

				userGroupMapper.insert(userGroup);
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void delete(String userIds, UserProfile optUser) {
		String userId_array[] = userIds.split(",");

		for (String userId : userId_array) {
			userGroupMapper.deleteByUserId(Long.parseLong(userId));
			userRoleMapper.deleteByUserId(Long.parseLong(userId));
			userMapper.deleteById(Integer.parseInt(userId));

		}
		userServiceLog.delete(userIds, optUser);
	}

	@Transactional(readOnly = true)
	public boolean userNameExist(String username) {
		String sql = "select a.* from users a where a.username = '" + username
				+ "'";
		List<User> systemusers = userMapper.selectBySql(sql);

		if (systemusers != null && systemusers.size() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	@Transactional(readOnly = true)
	public User getUserByUserId(Long userId) {
		List<User> systemusers = userMapper.selectById(userId);
		User systemuser = systemusers.get(0);
		if (systemuser != null) {
			fillSystemuser(systemuser);

			return systemuser;
		}
		else {
			return null;
		}
	}

	@Override
	public PagingBean<User> paging(int pageNumber, int pageSize,
			String sortCriterion, String sortDirection, User queryCondition,
			String in_userIds, String notin_userIds, Long loginUserId, boolean exportflag) {
		Map<String, Object> parameters = new HashMap<String, Object>(0);
		String pagingQueryLanguage = "select DISTINCT a.* from users a left join userrole b on a.id = b.userId left join usergroup c on a.id=c.userId where 1=1  ";
		// String countpagingQueryLanguage =
		// "select count(DISTINCT a) from users a left join a.systemuserroles b left join a.systemuserprojects c where 1=1  ";
		StringBuilder sbHQL = new StringBuilder(pagingQueryLanguage);
		// StringBuilder countsbHQL = new
		// StringBuilder(countpagingQueryLanguage);

		if (StringUtils.isNotBlank(queryCondition.getUserName())) {

			String loginName = queryCondition.getUserName();

			sbHQL.append(" and a.username like '%");
			sbHQL.append(loginName);
			sbHQL.append("%'");

		}

		if (StringUtils.isNotBlank(queryCondition.getRealName())) {

			String realName = queryCondition.getRealName();

			sbHQL.append(" and a.realName like '%");
			sbHQL.append(realName);
			sbHQL.append("%'");

		}
		if (StringUtils.isNotBlank(queryCondition.getRoleIds())) {
			sbHQL.append(" and b.roleId in (");
			sbHQL.append(queryCondition.getRoleIds());
			sbHQL.append(")");

		}

		if (StringUtils.isNotBlank(queryCondition.getStartDate_modifyTime())) {
			sbHQL.append(" and a.modifyTime >= '");
			if (queryCondition.getStartDate_modifyTime().length() > 11) {
				sbHQL.append(queryCondition.getStartDate_modifyTime());
			}
			else {
				sbHQL.append(queryCondition.getStartDate_modifyTime()
						+ " 00:00:00");
			}
			sbHQL.append("'");

		}

		if (StringUtils.isNotBlank(queryCondition.getEndDate_modifyTime())) {
			sbHQL.append(" and a.modifyTime <= '");
			if (queryCondition.getEndDate_modifyTime().length() > 11) {
				sbHQL.append(queryCondition.getEndDate_modifyTime());
			}
			else {
				sbHQL.append(queryCondition.getEndDate_modifyTime()
						+ " 23:59:59");
			}
			sbHQL.append("'");

		}

		if (StringUtils.isNotBlank(queryCondition.getStartDate_createTime())) {
			sbHQL.append(" and a.createTime >= '");
			if (queryCondition.getEndDate_createTime().length() > 11) {
				sbHQL.append(queryCondition.getStartDate_createTime());
			}
			else {
				sbHQL.append(queryCondition.getStartDate_createTime()
						+ " 23:59:59");
			}
			sbHQL.append("'");

		}

		if (StringUtils.isNotBlank(queryCondition.getEndDate_createTime())) {
			sbHQL.append(" and a.createTime <= '");
			if (queryCondition.getEndDate_createTime().length() > 11) {
				sbHQL.append(queryCondition.getEndDate_createTime());
			}
			else {
				sbHQL.append(queryCondition.getEndDate_createTime()
						+ " 23:59:59");
			}
			sbHQL.append("'");

		}

		if (in_userIds != null) {
			if (StringUtils.isNotBlank(in_userIds)) {
				sbHQL.append(" and a.id in (");
				sbHQL.append(in_userIds);
				sbHQL.append(")");

			}
			else {
				sbHQL.append(" and a.id in (");
				sbHQL.append("-1");
				sbHQL.append(")");
			}
		}

		if (StringUtils.isNotBlank(notin_userIds)) {
			sbHQL.append(" and a.id not in (");
			sbHQL.append(notin_userIds);
			sbHQL.append(")");

		}

		if (loginUserId != null) {
			parameters.put("loginUserId", loginUserId);
			sbHQL.append(" and (select count(DISTINCT bb.menuId) from rolemenu bb where bb.roleId in (select cc.roleId from userrole cc where cc.userId = a.id)) ");
			sbHQL.append("-    (select count(DISTINCT bb.menuId) from rolemenu bb where bb.roleId in (select cc.roleId from userrole cc where cc.userId = a.id) ");
			sbHQL.append("    and bb.menuId in (select cc.menuId FROM rolemenu cc where cc.roleId in (select dd.roleId from userrole dd where dd.userId ="
					+ loginUserId + " ))");
			sbHQL.append(") <= 0");

			sbHQL.append(" and a.id NOT IN (");
			sbHQL.append("	SELECT DISTINCT userid FROM usergroup WHERE groupid NOT IN ( ");
			sbHQL.append("		SELECT g.id FROM groups g JOIN usergroup b ON g.link LIKE CONCAT(CONCAT('%,', b.groupid), ',%') AND b.userid = "
					+ loginUserId);
			sbHQL.append(")");
			sbHQL.append(")  ");

			// if (StringUtils.isNotBlank(queryCondition.getGroupIds())) {
			// sbHQL.append(" and (select count(DISTINCT cc.groupId) from usergroup cc where cc.userId = a.id) ");
			// sbHQL.append("-    (select count(DISTINCT cc.groupId) from usergroup cc where cc.userId = a.id ");
			// sbHQL.append("    and cc.groupId in (" +
			// queryCondition.getGroupIds()
			// + " ))");
			// sbHQL.append(" <= 0");
			//
			//
			// }

		}

		String[] sortCriterions = null;
		if (sortCriterion == null) {
			sortCriterion = "modifyTime";
		}
		if (sortCriterion != null) {
			sortCriterions = new String[] { "a." + sortCriterion };
		}
		PagingSortDirection[] sortDirections = null;
		if (sortDirection != null) {
			sortDirections = new PagingSortDirection[] { "desc"
					.equals(sortDirection.toLowerCase()) ? PagingSortDirection.DESC
					: PagingSortDirection.ASC };
		}

		PagingBean<User> pagingSystemusers = pagingDao.query(userMapper,
				sbHQL.toString(), sortCriterions, sortDirections, pageNumber,
				pageSize,  exportflag);

		List<User> systemusers = pagingSystemusers.getDatas();
		for (User systemuser : systemusers) {

			fillSystemuser(systemuser);
		}

		return pagingSystemusers;
	}

	@Override
	public String getGroupIdsByUserId(Long userId) {
		String sql = "select a.* from usergroup a where a.userId = " + userId;
		StringBuffer sb = new StringBuffer();
		Set<Long> groupIds = new HashSet<Long>();
		List<UserGroup> systemuserprojects = userGroupMapper.selectBySql(sql);
		for (UserGroup systemuserproject : systemuserprojects) {
			// Project project = systemuserproject.getProject();
			groupIds.add(systemuserproject.getGroupId());
			// getChildProjectIds(systemuserproject.getGroupId(), groupIds);
		}

		for (Long groupId : groupIds) {
			if (sb.length() == 0) {
				sb.append(groupId);
			}
			else {
				sb.append(",");
				sb.append(groupId);
			}
		}

		return sb.toString();
	}

	@Transactional(rollbackFor = Exception.class)
	public void resetPwd(String userIds, UserProfile optUser) throws Exception {
		String userId_array[] = userIds.split(",");

		for (String userId : userId_array) {
			List<User> users = userMapper.selectById(Long.parseLong(userId));
			if (users == null) {
				throw new PmaxException("当前用户" + userId + "不存在，无法完成密码修改！");
			}
			User user = users.get(0);

			user.setPwd(encodeUtil.getPasswordMD5(newPwd));
			userMapper.update(user);

		}
		
		userServiceLog.resetPwd(userIds, optUser);

	}

	@Override
	public ArrayList<AuthType> getAuthType() {
		String sql = "select * from tb_auth_type";
		ArrayList<AuthType> list = (ArrayList<AuthType>) authTypeMapper.selectBySql(sql);
		return list;
	}

	@Override
	public List<User> queryUserbyName(String username) {
		List<User> list = userMapper.selectByUsername(username);
		return list;
	}

	@Override
	public List<AuthType> queryProjectByCompany(String company) {
		List<AuthType> list = authTypeMapper.selectAuthTypeByCompany(company);
		return list;
	}
	
	
	

	// /**
	// *
	// * @param project
	// * @param projectIds
	// */
	// private void getChildProjectIds(long pid, Set<Long> projectIds) {
	//
	// List<Group> childGroups =
	// groupMapper.selectBySql("SELECT * FROM groups WHERE pid = " + pid);
	// if (childGroups != null && childGroups.size() > 0) {
	// for (Group childProject : childGroups) {
	// projectIds.add(childProject.getId());
	// getChildProjectIds(childProject.getId(), projectIds);
	// }
	//
	// } else {
	// projectIds.add(pid);
	// }
	//
	// }

	// @Override
	// public List<TreeBean> getGroupTrees(Long opUserId, Long userId) {
	//
	//
	//
	//
	// return groupTreeBeans;
	//
	//
	// }
	//
	// private List<TreeBean> getGroupTree(Collection<Group> groups,
	// Set<Long> userGroupId, Set<Long> checkedGroupId) {
	//
	// List<TreeBean> groupTreeBeans = new ArrayList<TreeBean>();
	// for (Group group : groups) {
	// TreeBean treeBean = new TreeBean();
	// treeBean.setId(group.getId());
	// treeBean.setText(group.getName());
	//
	// if (checkedGroupId.contains(group.getId())) {
	// treeBean.setChecked(true);
	// } else {
	// treeBean.setChecked(false);
	// }
	//
	// treeBean.setParent(group.getPid());
	//
	// List<Group> childMenus =
	// groupMapper.selectBySql("SELECT * FROM groups WHERE pid = " +
	// group.getId());
	// if (childMenus != null && childMenus.size() > 0) {
	//
	//
	// treeBean.setChildren(getGroupTree(childMenus, userGroupId,
	// checkedGroupId));
	// if (treeBean.getChildren() == null || treeBean.getChildren().size() == 0)
	// {
	// continue;
	// }
	//
	// } else {
	// if (userGroupId != null && userGroupId.size() > 0) {
	//
	// if (!userGroupId.contains(group.getId())) {
	// continue;
	// }
	//
	// }
	// else {
	// continue;
	// }
	// }
	//
	// if (treeBean.getChildren() == null || treeBean.getChildren().size() == 0)
	// {
	// treeBean.setLeaf(true);
	// } else {
	// treeBean.setLeaf(false);
	// }
	//
	// groupTreeBeans.add(treeBean);
	//
	// }
	//
	// return groupTreeBeans;
	// }
	
}
