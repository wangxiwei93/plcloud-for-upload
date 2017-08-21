/*package com.routon.pmax.common.dao.page;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

*//**
 * <p>
 * Title: DBCommon
 * 
 * @author zzh
 * @version 1.0
 *//*
@Repository
public class DBCommon {
    private Logger          logger    = LoggerFactory.getLogger(DBCommon.class);

    @Autowired
    private DataSource dataSource;

    *//**
     * 代表数据库连接为MySQL
     *//*
    public final static int DB_MySQL  = 0;

    *//**
     * 代表数据库为Oracle
     *//*
    public final static int DB_Oracle = 1;

    *//**
     * 序列SQL
     *//*
    private String          sql[]     = {
                                      // MySQL
        "select getnextseq('%s') as newid"
        // Oracle
        , "select s_%s.nextval as newid from dual" };

    @Transactional(propagation = Propagation.SUPPORTS)
    public int getNewID(String tableName) {
        int newId = 0;
        SimpleJdbcTemplate simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);

        try {
            simpleJdbcTemplate.queryForInt(String
                .format(sql[getDBType(this.dataSource)], tableName));
        }
        catch (Exception e) {
            this.logger.error("GetNewID Exception", e);
        }

        return newId;
    }

    *//**
     * 获取分页后的SQL语句
     * 
     * @param sql
     *            原始SQL语句
     * @param pageIndex
     *            页码
     * @param pageSize
     *            页大小
     * @param dataSource
     *            数据源
     * @return 分页后的SQL语句
     *//*
    public static String getPagedSQL(String sql, Integer pageIndex, Integer pageSize,
        DataSource dataSource) {
        StringBuilder sb = new StringBuilder();

        switch (getDBType(dataSource)) {
            case DBCommon.DB_Oracle:
                sb.append("Select * ");
                sb.append("From (Select m.*, Rownum Rowindex ");
                sb.append("       From (");
                sb.append(sql);
                sb.append(") m) n ");
                sb.append("Where n.Rowindex Between ");
                sb.append((pageIndex - 1) * pageSize + 1);
                sb.append(" And ");
                sb.append(pageIndex * pageSize);
                break;
            default:
                sb.append(sql);
                sb.append(" limit ");
                sb.append((pageIndex - 1) * pageSize);
                sb.append(",");
                sb.append(pageSize);
                sb.append(" ");
                break;
        }

        return sb.toString();
    }

    *//**
     * 获取指定数据源的数据库连接类型
     * 
     * @param dataSource
     *            数据源
     * @return 数据库连接类型 Mysql:0,Oracle:1
     *//*
    public static int getDBType(DataSource dataSource) {
    	    	
    	return DBCommon.DB_MySQL;
//        if (dataSource == null) {
//            return DBCommon.DB_MySQL;
//        }
//
//        if (dataSource.getUrl().toLowerCase().indexOf("oracle") > -1) {
//            return DBCommon.DB_Oracle;
//        }
//        else if (dataSource.getUrl().toLowerCase().indexOf("mysql") > -1) {
//            return DBCommon.DB_MySQL;
//        }
//        else {
//            return DBCommon.DB_MySQL;
//        }
    }
}
*/