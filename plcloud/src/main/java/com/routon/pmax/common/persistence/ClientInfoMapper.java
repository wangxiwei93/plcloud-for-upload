package com.routon.pmax.common.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import com.routon.pmax.common.model.ClientInfo;
import com.routon.pmax.common.model.User;

public interface ClientInfoMapper extends PageMapper<ClientInfo>{
	
	@Select("${sql} limit #{rowCount} offset #{offset}")
	List<ClientInfo> selectByCondition(@Param("sql") String sql,  @Param("offset")int offset, @Param("rowCount")int rowCount);
	
	@Select("${sql} limit #{rowCount} offset #{offset}")
	ArrayList<Map<String,String>> selectByConditionMap(@Param("sql") String sql,  @Param("offset")int offset, @Param("rowCount")int rowCount);
	
	@Select("${sql}")
	List<ClientInfo> selectBySql(@Param("sql") String sql);
	
	@Select("${sql}")
	Integer selectCountByCondition(@Param("sql") String sql);
	
	@Select("select a.rec_id as id, a.company_name as companyName, a.project, a.province, a.city, a.district, a.client_code as clientCode,"
			+ "a.client_name as clientName, a.contact, a.telno, a.address, a.remark, a.time from tb_client_info a where client_code = #{clientCode}")
	List<ClientInfo> queryDataByClientCode(String clientCode);
	
	@Select("${sql}")
	ArrayList<Map<String,String>> selectBySql2(@Param("sql") String sql);

	@Insert("INSERT INTO tb_client_info (company_name, project, province, city, district, client_code, client_name, contact, telno, address, remark, time) "
			+ "VALUES (#{companyName},#{project},#{province},#{city},#{district},#{clientCode},#{clientName},#{contact},#{telno},#{address},#{remark},#{time})")

	@SelectKey(statement="SELECT currval('tb_client_info_rec_id_seq'::regclass) AS id", keyProperty="rec_id", before=false, resultType=long.class)
	long insert(ClientInfo clientInfo);
	
	@Update("UPDATE tb_client_info SET company_name=#{companyName}, project=#{project}, province=#{province},city=#{city},district=#{district},client_code=#{clientCode},"
			+ "client_name=#{clientName},contact=#{contact},telno=#{telno},address=#{address},remark=#{remark},time=#{time}"
			+ " WHERE rec_id = #{id} ")
	void updateClientInfo(ClientInfo clientInfo);
}
