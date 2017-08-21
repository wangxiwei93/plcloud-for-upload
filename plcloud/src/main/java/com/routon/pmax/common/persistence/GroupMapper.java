package com.routon.pmax.common.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import com.routon.pmax.common.model.Group;


public interface GroupMapper extends PageMapper<Group> {
	
	@Insert("INSERT INTO groups (pid,name,createTime,modifyTime) "
			+ " VALUES (#{pid},#{name},#{createtime},#{modifytime})")
	@SelectKey(statement="SELECT currval('groups_id_seq'::regclass) AS id", keyProperty="id", before=false, resultType=long.class) 
	long insert(Group group);
	
	@Update("UPDATE groups SET pid=#{pid}, name=#{name}, link=#{link}, createTime=#{createtime},modifyTime=#{modifytime} "
			+ " WHERE id = #{id} ")
	void update(Group group);
	
	@Delete("DELETE FROM groups WHERE id =#{id}")
	void deleteById(long id);
	
	@Select("${sql} limit #{offset} , #{rowCount}")
	List<Group> selectByCondition(@Param("sql") String sql,  @Param("offset")int offset, @Param("rowCount")int rowCount);
	
	@Select("${sql}")
	List<Group> selectBySql(@Param("sql") String sql);
	
	@Select("select * from groups")
	List<Group> findAllGroup();
	
	
	@Select("select * from groups where id=#{id}")
	Group selectById(long id);
	
}
