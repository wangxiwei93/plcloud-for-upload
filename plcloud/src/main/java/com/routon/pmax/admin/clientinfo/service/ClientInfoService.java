package com.routon.pmax.admin.clientinfo.service;

import java.util.ArrayList;
import java.util.Map;

import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.model.ClientInfo;

/**
 * 
 * @author wangxiwei
 *
 */
public interface ClientInfoService {
	public PagingBean<ClientInfo> paging(int startIndex, int pageSize,
			String sortCriterion, String sortDirection,
			Long loginUserId, String company, String project, boolean exportflag, String hotelName, String hotelCode, String startDate_createTime, String endDate_createTime);
	
	public ArrayList<Map<String,String>> queryAuthTerminalbyIMEI(String company, String project);
	
	public ArrayList<Map<String,String>> queryAuthTerminalbySn(String company, String project);
	
	public PagingBean<Map<String,String>> queryAuthList(String company, String project, int startIndex, int pageSize,
			String sortCriterion, String sortDirection, boolean exportflag, String hotelName, String hotelCode, String startDate_createTime, String endDate_createTime);
	
	public PagingBean<Map<String,String>> queryUnAuthList(String company, String project,int startIndex, int pageSize,
			String sortCriterion, String sortDirection, boolean exportflag, String hotelName, String hotelCode, String startDate_createTime, String endDate_createTime);
}
