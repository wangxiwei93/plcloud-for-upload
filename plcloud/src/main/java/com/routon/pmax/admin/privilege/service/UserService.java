package com.routon.pmax.admin.privilege.service;

import java.util.ArrayList;
import java.util.List;

import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.UserProfile;
import com.routon.pmax.common.model.AuthType;
import com.routon.pmax.common.model.User;

/**
 * 
 * <p>
 * Title: SystemUserService
 * </p>
 * <p>
 * Description: 定义系统用户业务接口
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * <p>
 * Company: Routon Electronic Co.,Ltd.
 * </p>
 * <p>
 * Date: 2013-5-15
 * </p>
 * 
 * @author
 * @version 1.0
 */
public interface UserService {

	/**
	 * 新增系统用户
	 * 
	 * @param systemuser
	 *            待新增的用户实体
	 * @param groupIds
	 *            系统用户的分组
	 * @param roleIds
	 *            系统用户的角色
	 * @param optUser
	 *            当前操作用户
	 * @return -2 用户名已经存在;-1 失败; 大于0 用户id;
	 */
	public Long add(User systemuser, String groupIds, String roleIds,
			UserProfile optUser);

	/**
	 * 编辑系统用户
	 * 
	 * @param systemuser
	 *            待编辑的用户实体
	 * @param groupIds
	 *            系统用户的分组
	 * @param roleIds
	 *            系统用户的角色
	 * @param optUser
	 *            当前操作用户
	 * @return 系统用户Id
	 */
	public Long edit(User systemuser, String groupIds, String roleIds,
			UserProfile optUser);

	/**
	 * 指派用户角色
	 * 
	 * @param userIds
	 *            用户ids
	 * @param roleIds
	 *            角色Ids
	 */
	public void assignSystemUserRole(String userIds, String roleIds);

	/**
	 * 指派用户分组
	 * 
	 * @param userIds
	 * @param groupIds
	 */
	public void assignSystemUserGroup(String userIds, String groupIds);

	/**
	 * 删除用户
	 * 
	 * @param userIds
	 *            待删除用户的ID集合
	 * @param optUser
	 *            当前操作用户
	 */
	public void delete(String userIds, UserProfile optUser);

	/**
	 * 
	 * @param userIds
	 * @param optUser
	 */
	public void resetPwd(String userIds, UserProfile optUser) throws Exception;

	/**
	 * 用户名是否重名
	 * 
	 * @param username
	 *            待判定的用户登录名
	 * @return 存在返回true，反之返回false
	 */
	public boolean userNameExist(String username);

	/**
	 * 获取 user信息
	 * 
	 * @param userId
	 *            用户ID
	 * @return Systemuser 系统用户
	 */
	public User getUserByUserId(Long userId);

	/**
	 * 获取用户列表的分页查询
	 * 
	 * @param startIndex
	 *            起始数据索引，从0开始
	 * @param pageSize
	 *            页大小
	 * @param sortCriterion
	 *            用于排序的字段
	 * @param sortDirection
	 *            排序方向
	 * @param queryCondition
	 *            查询条件
	 * @param in_userIds
	 * @param notin_userIds
	 * @param loginUserId
	 * @return 已分页的用户集合
	 */
	public PagingBean<User> paging(int startIndex, int pageSize,
			String sortCriterion, String sortDirection, User queryCondition,
			String in_userIds, String notin_userIds, Long loginUserId, boolean exportflag);

	/**
	 * 获取用户 权限范围内的分组ID
	 * 
	 * @param userId
	 *            用户ID
	 * @return 分组ID字符串(分组ID之间逗号分隔)
	 */
	String getGroupIdsByUserId(Long userId);

	// /**
	// * 获取分组树
	// *
	// * @param opUserId
	// * 当前使用用户id
	// * @param UserId
	// * 所选用户Id 可NULL
	// * @return 分组树集合
	// */
	// public List<TreeBean> getGroupTrees(Long opUserId, Long userId);
	
	public ArrayList<AuthType> getAuthType();
	
	public List<User> queryUserbyName(String username);
	
	public List<AuthType> queryProjectByCompany(String company);

}
