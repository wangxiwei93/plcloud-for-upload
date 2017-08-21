package com.routon.pmax.admin.clientinfo.action;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.routon.pmax.admin.clientinfo.service.ClientInfoService;
import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.UserProfile;
import com.routon.pmax.common.model.ClientInfo;
import com.routon.pmax.common.persistence.ClientInfoMapper;

/**
 * 客户信息查询导入导出
 * @author wangxiwei
 *
 */

@Controller
@SessionAttributes(value = { "userPrivilege", "userProfile" })
public class ClientInfoController {
	
	
	private Logger logger = Logger.getLogger(ClientInfoController.class);
	
	private String rootPath = ClientInfoController.class.getResource("/").getFile().toString().replaceAll("%20", " ").replaceFirst("/", "");
	
	@Autowired
	private ClientInfoService clientInfoService;
	
	@Autowired
	private ClientInfoMapper clientInfoMapper;
	
	@RequestMapping(value ="/terminal/processbar")
	public String ProcessBarTest(){
		return "clientInfo/ProcessBar";
	}
	
	@RequestMapping(value ="/terminal/show")
	public String list(HttpServletRequest request, ClientInfo info,
			@ModelAttribute("userProfile")
			UserProfile user, Model model, HttpSession session, String hotelName, String hotelCode,String startDate_createTime, String endDate_createTime){
		
		Long loginUserId = user.getCurrentUserId();

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

		PagingBean<ClientInfo> pagingBean = clientInfoService.paging(startIndex, pageSize, null, null, loginUserId,
				company, project, false, hotelName, hotelCode, startDate_createTime, endDate_createTime);

		int maxpage = (int) Math.ceil(pagingBean.getTotalCount()
				/ (double) pageSize);
		if (pagingBean.getTotalCount() == 0) {
			maxpage = 0;
		}
		model.addAttribute("pageList", pagingBean);
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("page", page);
		model.addAttribute("hotelName", hotelName);
		model.addAttribute("hotelCode", hotelCode);
		model.addAttribute("startDate_createTime", startDate_createTime);
		model.addAttribute("endDate_createTime", endDate_createTime);
		return "clientInfo/ClientInfo";
	}
	
	@RequestMapping(value ="/terminal/import")
	public ModelAndView importExcel(HttpServletRequest request, @ModelAttribute("userProfile")
			UserProfile user, @RequestParam(value = "images", required = false) MultipartFile file) throws FileNotFoundException{
		String up_path = "toupload";
	    String companyName = user.getCurrentUserCompany();
	    String project = user.getCurrentUserProject();
		//存储上传结果信息
		Map<String, String> map = new HashMap<String, String>();
		String realFileName = file.getOriginalFilename();
		//获取文件名后缀名
		String suffixfilename = realFileName.substring(realFileName.lastIndexOf('.') + 1, realFileName.length());
		Boolean iread = false;
		String ctxPath = request.getSession().getServletContext().getRealPath("/") + up_path + "/";
		logger.info("upload path:" + ctxPath + realFileName);
		//创建文件目录
		File dirPath = new File(ctxPath);  
		if (!dirPath.exists()) {
			//如果不存则创建文件路径
			dirPath.mkdir();
		}
		//上传文件到服务器
		File uploadFile = new File(ctxPath + realFileName);
		InputStream is = null;
		if (file != null && !file.isEmpty()) {
			try {
				is = file.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			DataOutputStream os = null;
			try {
				//创建一个输出流，将数据写入到指定的文件
				os = new DataOutputStream(new FileOutputStream(uploadFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			byte[] buffer = null;
			try {
				//创建一个字节流缓冲区，大小为预计的可读取字节流的大小
				buffer = new byte[is.available()];
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				//将数据写入到输入缓冲区
				is.read(buffer);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				//将缓冲区中的数据读取到输出流中
				os.write(buffer);
				map.put("result", "uploadsucess");
			} catch (IOException e1) {
				e1.printStackTrace();
				map.put("result", "uploadfail");
			}
			try {
				//将数据强制写入到输出流
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
				map.put("result", "uploadfail");
			}
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				map.put("result", "uploadfail");
			}
		}
		try {
			StringBuffer result = this.uploadValidat(ctxPath, realFileName, suffixfilename);
			if (result.toString() != null && !result.toString().isEmpty()) {
				map.put("geshi", result.toString());
			} else {
				iread = true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(iread){
			if (suffixfilename.equals("xls")) {
				
				StringBuffer bbbbs = this.readXls(ctxPath, realFileName, companyName, project);
			    map.put("readdata", bbbbs.toString());
		
			}
		}
		return new ModelAndView("clientInfo/ClientInfo", map);
	}
	
	public StringBuffer uploadValidat(String ctxPath, String fileName, String suffixfilename) throws FileNotFoundException{
		//用于传递错误信息
		StringBuffer abs = new StringBuffer();
		StringBuffer error = new StringBuffer();
		if (suffixfilename.equals("xls")) {
			try {
				InputStream is = new FileInputStream(new File(ctxPath + fileName));
				HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
				int isheet = 0;
				for(int numSheet = 0; numSheet < 1/*hssfWorkbook.getNumberOfSheets()*/; numSheet++){
					HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
					if (hssfSheet == null) {
						isheet++;
						continue;
					}
					HSSFRow hssfRow = hssfSheet.getRow(0);   //获取第一行的格式
					for(short cellNum = 0; cellNum < 10 /*hssfRow.getLastCellNum()*/; cellNum++){
						HSSFCell hssfCell = hssfRow.getCell(cellNum);
						if (hssfCell == null) {
							abs.append("第"+ (cellNum + 1)+"列列名为空");
							break;
						}
						if (cellNum == 0) {
							String orgCode = getValueofXls(hssfCell);
							if (!orgCode.equals("序号")) {
								abs.append("第1列列名不对");
								break;
							}
						}
						if (cellNum == 1) {
							String orgName = getValueofXls(hssfCell);
							if (!orgName.equals("省份")) {
								abs.append("第2列列名不对");
								break;
							}
						}
						if (cellNum == 2) {
							String contactPhone = getValueofXls(hssfCell);
							if (!contactPhone.equals("城市")) {
								abs.append("第3列列名不对");
								break;
							}
						}
						if (cellNum == 3) {
							String contactMobile = getValueofXls(hssfCell);
							if (!contactMobile.equals("区")) {
								abs.append("第4列列名不对");
								break;
							}
						}
						if (cellNum == 4) {
							String contactPerson = getValueofXls(hssfCell);
							if (!contactPerson.equals("酒店代码")) {
								abs.append("第5列列名不对");
								break;
							}
						}
						if (cellNum == 5) {
							String contactPerson = getValueofXls(hssfCell);
							if (!contactPerson.equals("酒店名称")) {
								abs.append("第6列列名不对");
								break;
							}
						}
						if (cellNum == 6) {
							String contactPerson = getValueofXls(hssfCell);
							if (!contactPerson.equals("联系人")) {
								abs.append("第7列列名不对");
								break;
							}
						}
						if (cellNum == 7) {
							String contactPerson = getValueofXls(hssfCell);
							if (!contactPerson.equals("联系电话")) {
								abs.append("第8列列名不对");
								break;
							}
						}
						if (cellNum == 8) {
							String contactPerson = getValueofXls(hssfCell);
							if (!contactPerson.equals("联系地址")) {
								abs.append("第9列列名不对");
								break;
							}
						}
						if (cellNum == 9) {
							String contactPerson = getValueofXls(hssfCell);
							if (!contactPerson.equals("备注")) {
								abs.append("第10列列名不对");
								break;
							}
						}
						if (cellNum >= 10) {// 由于hssfCell为null就会立马停止循环，只有不为空才能走下去
							abs.append("列数过多，检测到每行有{"+hssfRow.getLastCellNum()+"}列，格式不对，请删除多余无内容但是存在的单元格");
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				error.append("导入失败，服务器内部错误！！！");
				return error;
			}
		}
		return abs;
		
	}
	
	public StringBuffer readXls(String ctxPath, String fileName, String companyName, String project) throws FileNotFoundException{
		//用于传递错误信息
		StringBuffer bs = new StringBuffer();
		StringBuffer error = new StringBuffer();
		try {
		InputStream is = new FileInputStream(ctxPath + fileName);
		/*Properties prop = new Properties();
		FileInputStream inStream = new FileInputStream(rootPath + "setting.properties");
		
			prop.load(new InputStreamReader(inStream, "UTF-8"));
			String companyName = prop.getProperty("company_info");
			String project = prop.getProperty("project");*/
			HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
			for (int numSheet =0; numSheet < 1/*hssfWorkbook.getNumberOfSheets()*/; numSheet++) {
				HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
				System.out.println("表单数"+hssfWorkbook.getNumberOfSheets());
				if (hssfSheet == null) {
					continue;
				}
				int totalImportRowNum = hssfSheet.getLastRowNum();
				int successImportRowNum = 0;
				int failImportRowNum = 0;
				int rownum = hssfSheet.getPhysicalNumberOfRows();
				// 循环行Row
				for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
					HSSFRow hssfRow = hssfSheet.getRow(rowNum);
					if (hssfRow == null) {
						continue;
					}
					ClientInfo clientInfo = new ClientInfo();
					Boolean iTrue = false;
					Boolean update = false;
					String order = null;
					String province = null;
					String city = null;
					String district = null;
					String clientCode = null;
					String clientName = null;
					String contact = null;
					String telno = null;
					String address = null;
					String remark = null;
					Date updateTime = new Date();
					for (short cellNum = 0; cellNum < 10/*hssfRow.getLastCellNum()*/; cellNum++) {
						HSSFCell hssfCell = hssfRow.getCell(cellNum);
/*						if (hssfCell == null) {
							continue;
						}*/
						if(cellNum == 0){
							order = getValueofXls(hssfCell);
							if (order == null|| order.equals("")){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其序号为空。");
								failImportRowNum++;
								break;
							} 
						}
						if (cellNum == 1) { 
							province = getValueofXls(hssfCell);
/*							if(province.length() > 10 || isNumeric(province)){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其省份字符限制或格式不正确。");
								failImportRowNum++;
								break;
							}*/
						}
						if (cellNum == 2) { 
							city = getValueofXls(hssfCell);
						}
						if (cellNum == 3) { 
							district = getValueofXls(hssfCell);
						}
						if (cellNum == 4) { 
							clientCode = getValueofXls(hssfCell);
							if (clientCode == null|| clientCode.equals("")){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其酒店代码为空。");
								failImportRowNum++;
								break;
							}
							if(clientCode.length() != 10 || !isNumeric(clientCode)){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其酒店代码必须为十位数字。");
								failImportRowNum++;
								break;
							}
							List<ClientInfo> list = clientInfoMapper.queryDataByClientCode(clientCode);
							if(list.size() > 1){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，现有记录中存在酒店代码为{"+ clientCode +"}的多条记录。");
								failImportRowNum++;
								break;
							} else if(list.size() == 1){
								clientInfo = list.get(0);
								update = true;
							}
							
						}
						if (cellNum == 5) { 
							clientName = getValueofXls(hssfCell);
							if(clientName.length() > 32){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其酒店名称超出字符限制。");
								failImportRowNum++;
								break;
							}
						}
						if (cellNum == 6) { 
							contact = getValueofXls(hssfCell);
							if(contact.length() > 10){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其联系人超出字符限制。");
								failImportRowNum++;
								break;
							}
						}
						if (cellNum == 7) { 
							telno = getValueofXls(hssfCell);
/*							if(!isPhone(telno) && !isFixedLine(telno)){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，联系方式格式不正确。");
								failImportRowNum++;
								break;
							}*/
						}
						if (cellNum == 8) { 
							address = getValueofXls(hssfCell);
							if(address.length() > 32){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其联系地址超出字符限制。");
								failImportRowNum++;
								break;
							}
						}
						if (cellNum == 9) { 
							remark = getValueofXls(hssfCell);
							if(remark.length() > 30){
								bs.append("第" + numSheet+1 + "个sheet第" + (rowNum + 1) + "行未导入，其备注超出字符限制。");
								failImportRowNum++;
								break;
							}
							iTrue = true;
						}
						if(iTrue){
							clientInfo.setCompanyName(companyName);
							clientInfo.setProject(project);
							clientInfo.setProvince(province);
							clientInfo.setCity(city);
							clientInfo.setDistrict(district);
							clientInfo.setClientCode(clientCode);
							clientInfo.setClientName(clientName);
							clientInfo.setContact(contact);
							clientInfo.setTelno(telno);
							clientInfo.setAddress(address);
							clientInfo.setRemark(remark);
							clientInfo.setTime(updateTime);
							if(update){
								clientInfoMapper.updateClientInfo(clientInfo);
							} else{
								clientInfoMapper.insert(clientInfo);
							}
							successImportRowNum++;
						}
					}

				}
				bs.append("第" + numSheet+1 + "个sheet共导入："+ totalImportRowNum +"条记录，导入成功：" +successImportRowNum+ "条记录，导入失败：" + failImportRowNum +"条记录。");
			}
			//inStream.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			error.append("导入失败，内部错误！！！");
			return error;
		}
		return bs;
	}
	
	/**
	 * @Description:读取后缀为xls的excel单元格的内容
	 * @param :hssfCell excel单元格
	 * @return String 返回单元格数据
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private String getValueofXls(HSSFCell hssfCell) {
		DecimalFormat df = new DecimalFormat("0");
		if(hssfCell == null){
			return "";
		}
		if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(hssfCell.getBooleanCellValue());
		} else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
			return df.format(hssfCell.getNumericCellValue());
		} else {
			return String.valueOf(hssfCell.getRichStringCellValue());
		}
	}
	
	@RequestMapping("/terminal/downloadExcel")
	public void download(HttpServletRequest request,
			HttpServletResponse response,String browerType, String fileName) throws MalformedURLException {

		response.setContentType("text/html;charset=utf-8");
		try {
			// 解决中文文件名乱码问题
			String eFileName = URLEncoder.encode(fileName, "UTF-8");//IE浏览器
			//firefox、webkit、opera浏览器
			if (!browerType.equals("msie")) {
				eFileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1"); 
			} 
			request.setCharacterEncoding("UTF-8");
			FileInputStream bis = null;
			ServletOutputStream bos = null;
			
			String ctxPath = request.getSession().getServletContext().getResource("/").getPath() + "templeExcel/"; 
			File dirPath = new File(ctxPath);
			if (!dirPath.exists()) {
				//如果不存在则创建文件路径
				dirPath.mkdir();
			}
			String downLoadPath = ctxPath + fileName;
			long fileLength = new File(downLoadPath).length();
			response.setContentType("application/x-msdownload");
			response.setHeader("content-Disposition", "attachment; filename="+ eFileName);
			response.setHeader("Content-Length", String.valueOf(fileLength));
			bis = new FileInputStream(downLoadPath);
			bos = response.getOutputStream();
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		} catch (Exception e) {
			String msg = "下载上传文件模板异常：" + e.getMessage();
			logger.error(msg,e);
			e.printStackTrace();
		}

	}
	
	@RequestMapping("/terminal/writeProfile")
	public @ResponseBody String save(HttpServletRequest request, String company_name, String project) throws UnsupportedEncodingException{
		String entName = new String(company_name.getBytes("ISO8859-1"),"UTF-8");
		System.out.println(rootPath);
		System.out.println(request.getCharacterEncoding());
		FileInputStream inStream = null;
		OutputStream out = null;
		if(company_name == null || company_name == ""){
			company_name = "";
		}
		Properties prop = new Properties();
		try {
			inStream = new FileInputStream(rootPath + "setting.properties");
			try {
				prop.load(inStream);
				prop.setProperty("company_info", company_name);
				prop.setProperty("project", project);
				inStream.close();
				out = new FileOutputStream(rootPath + "setting.properties");
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
				prop.store(bw, "Update");
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "2";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "0";
		}
		return "1";
		
	}
	
	@RequestMapping("/terminal/queryinfo")
	public void query(HttpServletResponse response){
		Properties prop = new Properties();
		FileInputStream inStream = null;
		StringBuffer bs = new StringBuffer();
		try {
			inStream = new FileInputStream(rootPath + "setting.properties");
			/*FileReader reader = new FileReader(rootPath + "setting.properties");
			BufferedReader bw = new BufferedReader(reader);*/
			try {
				prop.load(new InputStreamReader(inStream, "UTF-8"));
				String companyInfo = prop.getProperty("company_info");
				String project = prop.getProperty("project");
				bs.append(companyInfo).append(";").append(project);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		response.setCharacterEncoding("utf-8");
	    response.setContentType("text/html;charset=utf-8");
	    response.setHeader("Cache-Control", "no-cache");
	    PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    out.print(bs);
	    out.flush();
	    out.close();
		
	}
	
	public static boolean isNumeric(String str){
	    Pattern pattern = Pattern.compile("[0-9]*");
	    return pattern.matcher(str).matches();   
	}
	
	public static boolean isPhone(String str){
		Pattern pattern = Pattern.compile("^(1[3,4,5,7,8][0-9])\\d{8}$");
		return pattern.matcher(str).matches();
	}
	
	public static boolean isFixedLine(String str){
		Pattern pattern = Pattern.compile("^([0-9]{3}-?[0-9]{7,8})|([0-9]{4}-?[0-9]{7,8})$");
		return pattern.matcher(str).matches();
	}
}
