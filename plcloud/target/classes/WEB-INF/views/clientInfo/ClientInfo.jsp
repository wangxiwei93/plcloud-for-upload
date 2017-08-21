<%@page import="com.routon.pmax.common.decorator.PageCheckboxDecorator"%>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ include file="/WEB-INF/views/head_n.jsp" %>
	<link rel="stylesheet" href="${ctx}/css/bootstrap-datetimepicker.css">
	<script src="${ctx}/js/bootstrap-datetimepicker.js"></script>
	<script src="${ctx}/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>
	<script src="${ctx}/js/jquery-easyui/jquery.easyui.min.js"></script>
	<div class="panel panel-default" style="width: 1560px;">
		<div class="panel-heading " style="padding: 0px;">
  			<div class="" style="display: inline-block;width: 100%;">
  			
	  			<div class=" col-sm-8" style="width: 1530px;">
		  			<form class="form-inline" role="form" id="queryform" name="queryform" action="${ctx}/terminal/show.do"  method="post">  		
		  			<div class="btn-group" style="margin-top: 5px;margin-bottom: 5px;">
		  				<input size="20" type="text" id="hotelName" name="hotelName" value="${hotelName}" class="form-control" placeholder="请输入酒店名称">
		  			</div>
		  			<div class="btn-group" style="margin-top: 5px;margin-bottom: 5px;">
		  				<input size="20" type="text" id="hotelCode" name="hotelCode" value="${hotelCode}" class="form-control" placeholder="请输入酒店代码">
		  			</div>
		  			<div class="btn-group" style="margin-top: 5px;margin-bottom: 5px;">
			  			<div class="input-group date form_datetime">
						    <input size="20" type="text" id="startDate_createTime" name="startDate_createTime" value="${startDate_createTime}" class="form-control" readonly placeholder="请输入导入起始时间">
						    <span class="input-group-addon">
						    <span class="glyphicon glyphicon-calendar"></span>
						    </span>
						</div>
						
					</div>
					<div class="btn-group" style="margin-top: 5px;margin-bottom: 5px;">
			  			<div class="input-group date form_datetime">
						    <input size="20" type="text" id="endDate_createTime" name="endDate_createTime" value="${endDate_createTime}" class="form-control" readonly placeholder="请输入导入结束时间">
						    <span class="input-group-addon">
						    <span class="glyphicon glyphicon-calendar"></span>
						    </span>
						</div>
						
					</div>
					
		  			<div class="btn-group">
		  				<button id="queryBtn" type="submit" class="btn btn-primary" >查询</button>
		  			</div>
		  			<div class="btn-group">
		  				<button id="clearBtn" type="button" class="btn btn-primary" >清空</button>
		  			</div>
		  				<button id= "downloadBtn" type="button" class="btn btn-primary">下载导入模板</button>
		  				<button id= "downloadFileBtn" type="button" class="btn btn-warning">下载导入说明</button>
		  				<button id= "importBtn" type="button" class="btn btn-danger">导入Excel</button>
		  			</form>
	  			</div>
  			</div> 
  		</div>
		<!-- table -->
		<display:table name="requestScope.pageList" id="curPage" class="table table-striped" sort="external"
			requestURI="show.do"
			decorator="com.routon.pmax.common.decorator.PageDecorator"
			export="false">
			<display:column property="id" title="<%=PageCheckboxDecorator.getTitle(pageContext)%>" decorator="com.routon.pmax.common.decorator.PageCheckboxDecorator"  style="width:2%;"/>
			
			<display:column title="公司名称" sortable="true" property="companyName" paramProperty="companyName"  style="width:9%;" >
			</display:column>
			<display:column title="项目名称"  property="project" sortable="true"  style="width:10%;" maxLength="50"></display:column>
			<display:column title="省份"  property="province"  sortable="true"  style="width:5%;"></display:column>
			<display:column title="城市"  property="city"  sortable="true"  style="width:4%;"></display:column>
			<display:column title="区"  property="district" sortable="true"  style="width:8%;" ></display:column>
			<display:column title="酒店代码"  property="clientCode" sortable="true"  style="width:10%;" ></display:column>
			<display:column title="酒店名称"  property="clientName" sortable="true"  style="width:10%;" ></display:column>
			<display:column title="联系人"  property="contact" sortable="true"  style="width:10%;" ></display:column>
			<display:column title="联系电话"  property="telno" sortable="true"  style="width:10%;" ></display:column>
			<display:column title="联系地址"  property="address" sortable="true"  style="width:10%;" ></display:column>
			<display:column title="备注"  property="remark" sortable="true"  style="width:5%;" ></display:column>
			<display:column title="上报时间"  property="time" sortable="true"  style="width:10%;" decorator="com.routon.pmax.common.decorator.PageDateTimeDecorator" ></display:column>
		</display:table>
	</div>
	<!-- <div id="dd" class="easyui-dialog" title="My Dialog" style="width:400px;height:200px;"data-options="iconCls:'icon-save',resizable:true,modal:true"></div> -->
	<!-- <input id = "input-1" name="image1" type="file" multiple class="file"><br>	 -->
	<%@ include file="/WEB-INF/views/common/pagination.jsp" %>
<script src="${ctx}/js/common.js"></script>
<script src="${ctx}/js/bootstrap-dialog.min.js"></script>
<script src="${ctx}/js/fileinput.min.js"></script>
<script>
//浏览器版本
var BROWER = {
	mozilla : /firefox/.test(navigator.userAgent.toLowerCase()),
	webkit : /webkit/.test(navigator.userAgent.toLowerCase()),
	opera : /opera/.test(navigator.userAgent.toLowerCase()),
	chrome : /chrome/.test(navigator.userAgent.toLowerCase()),
	msie : /msie/.test(navigator.userAgent.toLowerCase())
}

$("#importBtn").on("click", function() {
	var html = "<form id=\"submit_form\" method=\"post\" action=\"${base}terminal/import.do\" target=\"exec_target\" enctype=\"multipart/form-data\"><input id = \"input-1\" name=\"images\" type=\"file\" multiple class=\"file\"><br><input class=\"btn btn-primary\" name=\"upload\" type=\"submit\" value=\"上传\" /></form>";
    BootstrapDialog.show({
        title: '客户信息Excel导入',
        message: html
    })
});

$("#downloadBtn").on("click", function() {
	var browerType = "";
	// 判断浏览器版本
	if(BROWER.mozilla){
		browerType = "firefox";
	}
	else if(BROWER.webkit){
		browerType = "webkit";
	}
	else if(BROWER.opera){
		browerType = "opera";
	}
	else{
		browerType = "msie";
	}
	window.location.href="${base}terminal/downloadExcel.do?browerType="+browerType+"&fileName=导入模板.xls";
});

$("#downloadFileBtn").on("click", function() {
	var browerType = "";
	// 判断浏览器版本
	if(BROWER.mozilla){
		browerType = "firefox";
	}
	else if(BROWER.webkit){
		browerType = "webkit";
	}
	else if(BROWER.opera){
		browerType = "opera";
	}
	else{
		browerType = "msie";
	}
	window.location.href="${base}terminal/downloadExcel.do?browerType="+browerType+"&fileName=客户信息导入文件格式说明.doc";
});

$("#clearBtn").on("click", function() {
	$("#hotelName").val("");
	$("#hotelCode").val("");
	$("#startDate_createTime").val("");
	$("#endDate_createTime").val("");
});
</script>
<script type="text/javascript">
		jQuery(document).ready(function() {
			var result = "${result}";
	 		var readdata = "${readdata}";
			var geshi = "${geshi}"; 
			var iread = true;
			var igeshi = true;
			if (geshi != "" && geshi != null){
				$("#queryBtn").click();
				alert(geshi);
			}
			if (result != "" && result != null) {
				if (result == "uploadfail") {
					$("#queryBtn").click();
					alert('上传失败！');
				}/*  else if (result == "uploadsucess") {
					$("#queryBtn").click();
					alert('上传成功！');  
			}*/
		   }
		   if(readdata != "" && readdata != null){
			   $("#queryBtn").click();
			   alert(readdata);
		   }
		});
</script>

<script>
$(".form_datetime").datetimepicker({
    format: "yyyy-mm-dd hh:ii:ss",
    autoclose: true,
    todayBtn: true,
   	clearBtn:true,
    language:'zh-CN',
    pickerPosition: "bottom-left"
});
</script>
<%@ include file="/WEB-INF/views/foot_n.jsp" %>