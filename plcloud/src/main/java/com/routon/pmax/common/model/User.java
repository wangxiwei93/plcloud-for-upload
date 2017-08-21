package com.routon.pmax.common.model;

import java.util.Date;
import java.util.HashSet;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

public class User implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -799680982734501018L;

	private Long id;

	@Length(min=1, max=12,message="用户登录名长度为1-12")
	private String userName;

	@Length(min=1, max=12,message="用户真实姓名长度为1-12")
	private String realName;

	
	private String pwd;

	@NotEmpty(message="请填写电话")
	private String phone;

	@NotEmpty(message="请选择至少选择一个公司")
	private String company;

	private int status;

	private Date modifyTime;

	private Date createTime;

	private long createUserId;

	private String lastLoginIP;

	private Date lastLoginTime;
	
	private String role_texts;
	
	@NotEmpty(message="请至少选择一个角色")
	private String roleIds;
	
	private HashSet<Long> roleIdset = new HashSet<Long>();
	
	private String group_texts;
	
	@NotEmpty(message="请至少选择一个分组")
	private String groupIds;

	private String startDate_createTime;

	private String endDate_createTime;

	private String startDate_modifyTime;

	private String endDate_modifyTime;

	@NotEmpty(message="请至少选择一个项目名称")
	private String project;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(long createUserId) {
		this.createUserId = createUserId;
	}

	public String getLastLoginIP() {
		return lastLoginIP;
	}

	public void setLastLoginIP(String lastLoginIP) {
		this.lastLoginIP = lastLoginIP;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	
	public String getRole_texts() {
		return role_texts;
	}

	
	public void setRole_texts(String role_texts) {
		this.role_texts = role_texts;
	}

	
	public String getRoleIds() {
		return roleIds;
	}

	
	public void setRoleIds(String roleIds) {
		this.roleIds = roleIds;
	}

	
	public String getGroup_texts() {
		return group_texts;
	}

	
	public void setGroup_texts(String group_texts) {
		this.group_texts = group_texts;
	}

	
	public String getGroupIds() {
		return groupIds;
	}

	
	public void setGroupIds(String groupIds) {
		this.groupIds = groupIds;
	}

	
	public String getStartDate_createTime() {
		return startDate_createTime;
	}

	
	public void setStartDate_createTime(String startDate_createTime) {
		this.startDate_createTime = startDate_createTime;
	}

	
	public String getEndDate_createTime() {
		return endDate_createTime;
	}

	
	public void setEndDate_createTime(String endDate_createTime) {
		this.endDate_createTime = endDate_createTime;
	}

	
	public String getStartDate_modifyTime() {
		return startDate_modifyTime;
	}

	
	public void setStartDate_modifyTime(String startDate_modifyTime) {
		this.startDate_modifyTime = startDate_modifyTime;
	}

	
	public String getEndDate_modifyTime() {
		return endDate_modifyTime;
	}

	
	public void setEndDate_modifyTime(String endDate_modifyTime) {
		this.endDate_modifyTime = endDate_modifyTime;
	}

	public HashSet<Long> getRoleIdset() {
		return roleIdset;
	}

	public void setRoleIdset(HashSet<Long> roleIdset) {
		this.roleIdset = roleIdset;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

}
