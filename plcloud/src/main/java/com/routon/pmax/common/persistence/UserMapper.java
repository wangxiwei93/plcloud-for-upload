package com.routon.pmax.common.persistence;


import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import com.routon.pmax.common.model.Role;
import com.routon.pmax.common.model.User;


public interface UserMapper extends PageMapper<User> {
	//@Select("${sql} limit #{offset} , #{rowCount}")
	@Select("${sql} limit #{rowCount} offset #{offset}")
	List<User> selectByCondition(@Param("sql") String sql,  @Param("offset")int offset, @Param("rowCount")int rowCount);
	
	@Select("${sql}")
	List<User> selectBySql(@Param("sql") String sql);

	@Insert("INSERT INTO users (username,realName,pwd,phone,company,status,modifytime,createtime,createUserId,lastLoginIP,lastLoginTime,project) "
			+ "VALUES (#{userName},#{realName},#{pwd},#{phone},#{company},#{status},#{modifyTime},#{createTime},#{createUserId},#{lastLoginIP},#{lastLoginTime},#{project})")
	@SelectKey(statement="SELECT currval('users_id_seq'::regclass) AS id", keyProperty="id", before=false, resultType=long.class) 
	long insert(User user);

	@Update("UPDATE  users SET username=#{userName},realName=#{realName},pwd=#{pwd},phone=#{phone},company=#{company},status=#{status},modifytime=#{modifyTime},createtime=#{createTime},createUserId=#{createUserId},lastLoginIP=#{lastLoginIP},lastLoginTime=#{lastLoginTime},project=#{project}"
			+ " WHERE id = #{id} ")
	void update(User user);
	
	@Delete("DELETE FROM users WHERE id =#{id}")
	void deleteById(long id);
	
	
	@Select("select * from users where id = #{id}")
	List<User> selectById(long id);
	
	@Select("select * from users where username = #{username}")
	List<User> selectByUsername(String username);
}
