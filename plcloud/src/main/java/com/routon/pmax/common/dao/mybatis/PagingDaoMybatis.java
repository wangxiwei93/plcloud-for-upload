package com.routon.pmax.common.dao.mybatis;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.routon.pmax.common.PagingBean;
import com.routon.pmax.common.PagingSortDirection;
import com.routon.pmax.common.persistence.PageMapper;


/**
 * <p>
 * Title: PagingDaoHibernate
 * </p>
 * <p>
 * Description:
 * 
 * @version 1.0
 */

@Repository
public class PagingDaoMybatis {

    private final static Logger logger = LoggerFactory.getLogger(PagingDaoMybatis.class);

    @Transactional(readOnly = true)
   	public <T> PagingBean<T> query(PageMapper<T> pageMapper, 
   			String sql,		
   			String countsql,
   			String[] sortCriterion, PagingSortDirection[] sortDirection,
   			int startIndex, int pageSize, boolean exportflag) {
   		
   		final PagingBean<T> pagingBean = new PagingBean<T>();
   		
   		StringBuffer order = new StringBuffer(" "); 
           if (sortCriterion != null && sortDirection != null && sortCriterion.length > 0 
           		&& sortCriterion.length == sortDirection.length) {

           	order.append(" order by ");

               for (int i = 0; i < sortCriterion.length; i++) {
               	order.append(sortCriterion[i]);
               	order.append(" ");
               	order.append(sortDirection[i]);

                   if (i + 1 < sortCriterion.length)
                   	order.append(",");
                   
                   String sort = "";
                   try {
                      // if(sortCriterion[i].indexOf(".") > 0) {
                       	sort = sortCriterion[i].split("\\.")[1];
                       //}
                       //else {
                       	sort = sortCriterion[i];
                       //}
                   }
                   catch (Exception e) {
                   	
   				}
                   pagingBean.setSort(sort);
                   pagingBean.setDir(sortDirection[i] == PagingSortDirection.ASC?"ASC":"DESC");
                   
               }
           }
               
           long queryCountStart = 0, queryCountEnd = 0; 
           if (true){
               queryCountStart = System.currentTimeMillis();
               if (StringUtils.isBlank(countsql)) {
            	   //countsql = "select count(DISTINCT a.id) "+ sql.substring(sql.toLowerCase().indexOf("from"));
            	   countsql = "select count(*) "+ sql.substring(sql.toLowerCase().indexOf("from"));
               }
               Integer count = pageMapper.selectCountByCondition(countsql);
               queryCountEnd = System.currentTimeMillis();
               pagingBean.setTotalCount(count==null?0:count);        	
           }
           
           if (order.length() > 0){
           	if (sql == null){
           		sql = order.toString();
           	} else {
           		sql += order.toString();
           	}
           }
           
           logger.info("Paging分页查询{}", sql);
   		
           long queryStart = System.currentTimeMillis();
           List<T> datas = null;
           if(exportflag){
        	   datas = pageMapper.selectBySql(sql);
           }
           else {
        	   datas = pageMapper.selectByCondition(sql, startIndex, pageSize);
           }
           
           
           
           long queryEnd = System.currentTimeMillis();		
   		
           logger.info(String.format(
                   "Paging分页查询执行情况：查询数据范围[%s-%s],查询数据总记录数[%s]条耗时(%s)ms,查询当前页记录数[%s]条耗时(%s)ms",
                   startIndex, startIndex + pageSize - 1,
                   pagingBean.getTotalCount(), queryCountEnd - queryCountStart, datas.size(), queryEnd - queryStart));

           pagingBean.setDatas(datas);
           pagingBean.setLimit(pageSize);
           pagingBean.setStart(startIndex);

           return pagingBean;
   	}
    
    @Transactional(readOnly = true)
	public <T> PagingBean<T> query(PageMapper<T> pageMapper, 
			String sql,			 			
			String[] sortCriterion, PagingSortDirection[] sortDirection,
			int startIndex, int pageSize, boolean exportflag) {
    	return query(pageMapper, sql, null, sortCriterion, sortDirection, startIndex, pageSize,exportflag);
    }

}
