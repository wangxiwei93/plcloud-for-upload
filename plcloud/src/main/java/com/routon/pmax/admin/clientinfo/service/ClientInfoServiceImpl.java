package com.routon.pmax.admin.clientinfo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.PagingSortDirection;
import com.routon.pmax.common.dao.mybatis.PagingDaoMybatis;
import com.routon.pmax.common.model.ClientInfo;
import com.routon.pmax.common.persistence.ClientInfoMapper;


/**
 * 
 * @author wangxiwei
 *
 */
@Service
public class ClientInfoServiceImpl implements ClientInfoService{

	@Autowired
	private ClientInfoMapper clientInfoMapper;
	
	@Resource(name = "pagingDaoMybatis")
	private PagingDaoMybatis pagingDao;
	
	@Override
	public PagingBean<ClientInfo> paging(int startIndex, int pageSize, String sortCriterion, String sortDirection,
			Long loginUserId, String company, String project, boolean exportflag, String hotelName, String hotelCode, String startDate_createTime, String endDate_createTime) {
		String pagingQueryLanguage = "SELECT a.rec_id as id, a.company_name as companyName, a.project as project,"
				+ " a.province as province, a.city as city, a.district as district, a.client_code as clientCode, a.client_name as clientName,"
				+ " a.contact as contact, a.telno as telno, a.address as address, a.remark as remark, a.time as time FROM tb_client_info a WHERE 1=1 ";
		StringBuilder sbHQL = new StringBuilder(pagingQueryLanguage);
		if(company != null){
			sbHQL.append("and a.company_name = '" + company + "'");
		}
		if(project != null){
			sbHQL.append(" and a.project = '" + project + "'");
		}
		if(hotelName != null && !hotelName.equals("")){
			sbHQL.append(" and a.client_name like '%" + hotelName + "%'");
		}
		if(hotelCode != null && !hotelCode.equals("")){
			sbHQL.append(" and a.client_code like '%" + hotelCode + "%'");
		}
		/*if(!StringUtils.isEmpty(startDate_createTime) && !StringUtils.isEmpty(endDate_createTime)) {
			sbHQL.append(" and a.time BETWEEN to_timestamp('" + startDate_createTime + "','yyyy-mm-dd hh24:mi:ss') and to_timestamp('"+ endDate_createTime +"','yyyy-mm-dd hh24:mi:ss')");
		}*/
		if(!StringUtils.isEmpty(startDate_createTime)){
			sbHQL.append(" and a.time >= to_timestamp('" + startDate_createTime + "','yyyy-mm-dd hh24:mi:ss')");
		}
		if(!StringUtils.isEmpty(endDate_createTime)){
			sbHQL.append(" and a.time <= to_timestamp('" + endDate_createTime + "','yyyy-mm-dd hh24:mi:ss')");
		}
		String[] sortCriterions = null;
		if (sortCriterion != null) {
			sortCriterions = new String[] {sortCriterion };
		}
		PagingSortDirection[] sortDirections = null;
		if(StringUtils.isBlank(sortDirection)){
			sortDirection = "desc";
		}
		if (sortDirection != null) {
			sortDirections = new PagingSortDirection[] { "desc"
					.equals(sortDirection.toLowerCase()) ? PagingSortDirection.DESC
					: PagingSortDirection.ASC };
		}
		PagingBean<ClientInfo> paging = pagingDao.query(clientInfoMapper,
				sbHQL.toString(), sortCriterions, sortDirections, startIndex, pageSize,  exportflag);
		return paging;
	}

	/**
	 * 根据IMEI查询查询已授权的终端数
	 */
	@Override
	public ArrayList<Map<String, String>> queryAuthTerminalbyIMEI(String company, String project) {
		String pagingQueryLanguage = "select a.company_name,a.project,count(DISTINCT  a.term_code) as imei from tb_licence_response a where 1=1 ";
		StringBuilder sbHQL = new StringBuilder(pagingQueryLanguage);
		if(company != null){
			sbHQL.append("and a.company_name = '" + company + "'");
		}
		if(project != null){
			sbHQL.append(" and a.project = '" + project + "'");
		}
		sbHQL.append(" group by a.company_name,a.project");
		ArrayList<Map<String,String>> list = clientInfoMapper.selectBySql2(sbHQL.toString());
		return list;
	}

	
	/**
	 * 根据sn查询查询已授权的终端数
	 */
	@Override
	public ArrayList<Map<String, String>> queryAuthTerminalbySn(String company, String project) {
		String pagingQueryLanguage = "select a.company_name,a.project,count(DISTINCT  a.term_sn) termsn from tb_licence_response a where 1=1 ";
		StringBuilder sbHQL = new StringBuilder(pagingQueryLanguage);
		if(company != null){
			sbHQL.append("and a.company_name = '" + company + "'");
		}
		if(project != null){
			sbHQL.append(" and a.project = '" + project + "'");
		}
		sbHQL.append(" group by a.company_name,a.project");
		ArrayList<Map<String,String>> list = clientInfoMapper.selectBySql2(sbHQL.toString());
		return list;
	}

	
	/**
	 * 查询授权清单
	 */
	@Override
	public PagingBean<Map<String, String>> queryAuthList(String company, String project, int startIndex, int pageSize,
			String sortCriterion, String sortDirection, boolean exportflag, String hotelName, String hotelCode, String startDate_createTime, String endDate_createTime) {
		String pagingQueryLanguage = "SELECT a.client_code as client_code,b.client_name as client_name,a.term_code as term_code,"
				+ "min(a.time) as time FROM tb_licence_response a,tb_client_info b WHERE a.client_code=b.client_code ";
		StringBuilder sbHQL = new StringBuilder(pagingQueryLanguage);
		if(company != null){
			sbHQL.append("and a.company_name = '" + company + "'");
		}
		if(project != null){
			sbHQL.append(" and a.project = '" + project + "'");
		}
		if(hotelName != null && !hotelName.equals("")){
			sbHQL.append(" and b.client_name like '%" + hotelName + "%'");
		}
		if(hotelCode != null && !hotelCode.equals("")){
			sbHQL.append(" and b.client_code like '%" + hotelCode + "%'");
		}
/*		if(!StringUtils.isEmpty(startDate_createTime) && !StringUtils.isEmpty(endDate_createTime)) {
			sbHQL.append(" and a.time BETWEEN to_timestamp('" + startDate_createTime + "','yyyy-mm-dd hh24:mi:ss') and to_timestamp('"+ endDate_createTime +"','yyyy-mm-dd hh24:mi:ss')");
		}*/
		if(!StringUtils.isEmpty(startDate_createTime)){
			sbHQL.append(" and a.time >= to_timestamp('" + startDate_createTime + "','yyyy-mm-dd hh24:mi:ss')");
		}
		if(!StringUtils.isEmpty(endDate_createTime)){
			sbHQL.append(" and a.time <= to_timestamp('" + endDate_createTime + "','yyyy-mm-dd hh24:mi:ss')");
		}
		sbHQL.append(" GROUP BY a.client_code,a.term_code,b.client_name ORDER BY a.client_code");
		ArrayList<Map<String,String>> list = clientInfoMapper.selectByConditionMap(sbHQL.toString(), startIndex, pageSize);
		String countsql = "SELECT count(*) from (" + sbHQL.toString() + ") as sub";
		Integer count = clientInfoMapper.selectCountByCondition(countsql);
		PagingBean<Map<String, String>> bean = new PagingBean<Map<String, String>>();
		bean.setTotalCount(count);
		bean.setDatas(list);
		bean.setStart(startIndex);
		bean.setLimit(pageSize);
		return bean;
	}

	
	/**
	 * 查询未授权清单
	 */
	@Override
	public PagingBean<Map<String, String>> queryUnAuthList(String company, String project,int startIndex, int pageSize,
			String sortCriterion, String sortDirection, boolean exportflag, String hotelName, String hotelCode, String startDate_createTime, String endDate_createTime) {
		String pagingQueryLanguage = "SELECT * FROM tb_client_info a WHERE a.client_code NOT IN (SELECT client_code FROM tb_licence_response) ";
		StringBuilder sbHQL = new StringBuilder(pagingQueryLanguage);
		if(company != null){
			sbHQL.append("and a.company_name = '" + company + "'");
		}
		if(project != null){
			sbHQL.append(" and a.project = '" + project + "'");
		}
		if(hotelName != null && !hotelName.equals("")){
			sbHQL.append(" and a.client_name like '%" + hotelName + "%'");
		}
		if(hotelCode != null && !hotelCode.equals("")){
			sbHQL.append(" and a.client_code like '%" + hotelCode + "%'");
		}
		if(!StringUtils.isEmpty(startDate_createTime)){
			sbHQL.append(" and a.time >= to_timestamp('" + startDate_createTime + "','yyyy-mm-dd hh24:mi:ss')");
		}
		if(!StringUtils.isEmpty(endDate_createTime)){
			sbHQL.append(" and a.time <= to_timestamp('" + endDate_createTime + "','yyyy-mm-dd hh24:mi:ss')");
		}
		
		ArrayList<Map<String,String>> list = clientInfoMapper.selectByConditionMap(sbHQL.toString(), startIndex, pageSize);
		String countsql = "SELECT count(*) from ("+ sbHQL.toString() +") as sub";
		Integer count = clientInfoMapper.selectCountByCondition(countsql);
		PagingBean<Map<String, String>> bean = new PagingBean<Map<String, String>>();
		bean.setTotalCount(count);
		bean.setDatas(list);
		bean.setStart(startIndex);
		bean.setLimit(pageSize);
		return bean;
	}

	
}
