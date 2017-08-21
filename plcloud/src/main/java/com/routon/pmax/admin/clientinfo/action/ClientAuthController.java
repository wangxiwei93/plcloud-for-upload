package com.routon.pmax.admin.clientinfo.action;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.routon.pmax.admin.clientinfo.service.ClientInfoService;
import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.UserProfile;


/**
 * 
 * @author wangxiwei
 *
 */
@Controller
@SessionAttributes(value = { "userPrivilege", "userProfile" })
public class ClientAuthController {
	
	@Autowired
	private ClientInfoService clientInfoService;
	
	@RequestMapping(value ="/terminal/setprofile")
	public String queryAuthProfile(HttpServletRequest request, Model model, @ModelAttribute("userProfile") UserProfile user,
			 String hotelName, String hotelCode,String startDate_createTime, String endDate_createTime){

		String company = user.getCurrentUserCompany();
		String project = user.getCurrentUserProject();
		
		if(user.getCurrentUserLoginName().equals("admin")){
			company = null;
			project = null;
		}
		
		int page = NumberUtils.toInt(request.getParameter("page"), 1);
		int pageSize = NumberUtils.toInt(request.getParameter("pageSize"),
				10);
		int startIndex = (page - 1) * pageSize;

		
		ArrayList<Map<String,String>> pagelistIMEI = clientInfoService.queryAuthTerminalbyIMEI(company, project);
		
/*		ArrayList<Map<String,String>> pagelistSn = clientInfoService.queryAuthTerminalbySn(company, project);*/
		
		PagingBean<Map<String,String>> pageAuthList = clientInfoService.queryAuthList(company, project, startIndex, pageSize,
				null, null, false, hotelName, hotelCode, startDate_createTime, endDate_createTime);

		int maxpage = (int) Math.ceil(pageAuthList.getTotalCount()
				/ (double) pageSize);
		if (pageAuthList.getTotalCount() == 0) {
			maxpage = 0;
		}
		
		
		model.addAttribute("pageList", pageAuthList);
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("page", page);
		model.addAttribute("imeipage", pagelistIMEI);
		model.addAttribute("hotelName", hotelName);
		model.addAttribute("hotelCode", hotelCode);
		model.addAttribute("startDate_createTime", startDate_createTime);
		model.addAttribute("endDate_createTime", endDate_createTime);
		return "clientInfo/ImportForm";
	}
	
	@RequestMapping(value ="/terminal/queryunauthlist")
	public String queryUnAuthProfile(HttpServletRequest request, Model model, @ModelAttribute("userProfile") UserProfile user,
			String hotelName, String hotelCode,String startDate_createTime, String endDate_createTime){

		String company = user.getCurrentUserCompany();
		String project = user.getCurrentUserProject();
		
		if(user.getCurrentUserLoginName().equals("admin")){
			company = null;
			project = null;
		}
		
		int page = NumberUtils.toInt(request.getParameter("page"), 1);
		int pageSize = NumberUtils.toInt(request.getParameter("pageSize"),
				10);
		int startIndex = (page - 1) * pageSize;
		
		PagingBean<Map<String,String>> pageUnAuthList = clientInfoService.queryUnAuthList(company, project, startIndex, pageSize,
				null, null, false, hotelName, hotelCode, startDate_createTime, endDate_createTime);

		int maxpage = (int) Math.ceil(pageUnAuthList.getTotalCount()
				/ (double) pageSize);
		if (pageUnAuthList.getTotalCount() == 0) {
			maxpage = 0;
		}
		
		model.addAttribute("pageList", pageUnAuthList);
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("page", page);
		model.addAttribute("hotelName", hotelName);
		model.addAttribute("hotelCode", hotelCode);
		model.addAttribute("startDate_createTime", startDate_createTime);
		model.addAttribute("endDate_createTime", endDate_createTime);
		return "clientInfo/ClientInfoUnauth";
	}
}
