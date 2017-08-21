/**
 * 
 */
package com.routon.pmax.common;

import java.io.Serializable;


public class UserProfile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7560294052028015361L;
	/**
	 * 当前登录用户ID
	 */
	protected Long currentUserId;
	/**
	 * 当前登录用户账号
	 */
	protected String currentUserLoginName;

	/**
	 * 当前登录用户真实姓名
	 */
	protected String currentUserRealName;

	/**
	 * 当前登录用户登录Ip
	 */
	protected String currentUserLoginIp;
	
	/**
	 * 当前用户所属公司
	 */
	protected String currentUserCompany;
	
	/**
	 * 当前用户所属项目
	 */
	protected String currentUserProject;

	public Long getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(Long currentUserId) {
		this.currentUserId = currentUserId;
	}

	public String getCurrentUserLoginName() {
		return currentUserLoginName;
	}

	public void setCurrentUserLoginName(String currentUserLoginName) {
		this.currentUserLoginName = currentUserLoginName;
	}

	public String getCurrentUserRealName() {
		return currentUserRealName;
	}

	public void setCurrentUserRealName(String currentUserRealName) {
		this.currentUserRealName = currentUserRealName;
	}

	public String getCurrentUserLoginIp() {
		return currentUserLoginIp;
	}

	public void setCurrentUserLoginIp(String currentUserLoginIp) {
		this.currentUserLoginIp = currentUserLoginIp;
	}

	public String getCurrentUserCompany() {
		return currentUserCompany;
	}

	public void setCurrentUserCompany(String currentUserCompany) {
		this.currentUserCompany = currentUserCompany;
	}

	public String getCurrentUserProject() {
		return currentUserProject;
	}

	public void setCurrentUserProject(String currentUserProject) {
		this.currentUserProject = currentUserProject;
	}

}
