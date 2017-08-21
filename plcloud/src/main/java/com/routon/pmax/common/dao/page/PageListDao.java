/*package com.routon.pmax.common.dao.page;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

*//**
 * <p>
 * Title: PageListDao
 * </p>
 * <p>
 * Description: Displaytag 分页数据访问类（支持自定义数据来源）
 * 
 * @version 1.0
 *//*
public class PageListDao<T> implements PaginatedList {
    *//**
     * 列表总记录数
     *//*
    private int           fullListSize;

    *//**
     * 列表集合
     *//*
    private List<T>       list;

    *//**
     * 每页记录数
     *//*
    private int           objectsPerPage = 12;

    *//**
     * 当前第几页
     *//*
    private int           pageNumber     = 1;

    *//**
     * 关键字
     *//*
    private String        searchId;

    *//**
     * 排序字段
     *//*
    private String        sortCriterion;

    *//**
     * 排序方向(ASC、DESC)
     *//*
    private SortOrderEnum sortDirection  = SortOrderEnum.DESCENDING;

    private Logger        logger         = LoggerFactory.getLogger(PageListDao.class);

    public PageListDao(HttpServletRequest request, String sql, Class<T> dataType) throws Exception {
        this(request, sql, null, null, dataType, null);
    }

    public PageListDao(HttpServletRequest request, String sql, Class<T> dataType,
        DataSource dataSource) throws Exception {
        this(request, sql, null, null, dataType, dataSource);
    }

    public PageListDao(HttpServletRequest request, String sql, String sortCriterion,
        String sortDirection, Class<T> dataType) throws Exception {
        this(request, sql, sortCriterion, sortDirection, dataType, null);
    }

    public PageListDao(HttpServletRequest request, String sql, String sortCriterion,
        String sortDirection, Class<T> dataType, DataSource dataSource) throws Exception {
        this(request, sql, 0, 0, sortCriterion, sortDirection, dataType, dataSource);
    }

    public PageListDao(HttpServletRequest request, String sql, int pageNumber, int objectsPerPage,
        String sortCriterion, String sortDirection, Class<T> dataType) throws Exception {
        this(request, sql, pageNumber, objectsPerPage, sortCriterion, sortDirection, dataType, null);
    }

    public PageListDao(HttpServletRequest request, String sql, int pageNumber, int objectsPerPage,
        String sortCriterion, String sortDirection, Class<T> dataType, DataSource dataSource)
        throws Exception {
        StringBuffer sb = new StringBuffer(sql);
        if (request == null)
            throw new Exception("构造列表页面数据源时request不能为空");

        SimpleJdbcTemplate simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);

        getPagingParams(request);

        if (dataSource == null) {
            dataSource = (DataSource)WebApplicationContextUtils.getWebApplicationContext(
                request.getSession().getServletContext()).getBean("dataSource");
        }

        if (this.pageNumber <= 0 && pageNumber > 0)
            this.pageNumber = pageNumber;

        if (this.objectsPerPage <= 0 && objectsPerPage > 0)
            this.objectsPerPage = objectsPerPage;

        if (StringUtils.isEmpty(this.sortCriterion) || StringUtils.isBlank(this.sortCriterion)) {
            if (StringUtils.isNotEmpty(sortCriterion) && StringUtils.isNotBlank(sortCriterion))
                this.sortCriterion = sortCriterion;
        }

        if (this.sortDirection == null) {
            if (StringUtils.isNotEmpty(sortDirection) && StringUtils.isNotBlank(sortDirection)) {
                if ("asc".equals(sortDirection.toLowerCase()))
                    this.sortDirection = SortOrderEnum.ASCENDING;
                else
                    this.sortDirection = SortOrderEnum.DESCENDING;
            }
        }

        if (StringUtils.isNotBlank(this.sortCriterion)) {
            sb.append(" order by ");
            sb.append(this.sortCriterion);
            sb.append(" ");

            if (this.sortDirection == SortOrderEnum.DESCENDING)
                sb.append("DESC");
            else
                sb.append("ASC");
        }

        // 是否为导出
        String temp = request.getParameter((new org.displaytag.util.ParamEncoder("curPage")
            .encodeParameterName(org.displaytag.tags.TableTagParameters.PARAMETER_EXPORTTYPE)));

        if (StringUtils.isNotBlank(sql)) {
            // 查询
            if (temp == null) {
                String sqlPaging = DBCommon.getPagedSQL(sb.toString(), this.pageNumber,
                    this.objectsPerPage, dataSource);
                long t1 = java.util.Calendar.getInstance().getTimeInMillis();
                this.list = simpleJdbcTemplate.query(sqlPaging, ParameterizedBeanPropertyRowMapper
                    .newInstance(dataType));
                this.logger.info("PageListDao - 分页sql：" + sqlPaging);
                long t2 = java.util.Calendar.getInstance().getTimeInMillis();
                String sqlCount = "select count(*) from (" + sql + ") aaa_count";
                this.fullListSize = simpleJdbcTemplate.queryForInt(sqlCount);
                this.logger.info("PageListDao - 分页sql(count)：" + sqlCount);
                long t3 = java.util.Calendar.getInstance().getTimeInMillis();
                this.logger.info("PageList - 总记录数：" + this.fullListSize + "，用时：" + (t3 - t1)
                    + " ms(=" + (t3 - t2) + "+" + (t2 - t1) + ")");
            }// 导出
            else {
                long t1 = java.util.Calendar.getInstance().getTimeInMillis();
                this.list = simpleJdbcTemplate.query(sb.toString(),
                    ParameterizedBeanPropertyRowMapper.newInstance(dataType));
                this.fullListSize = this.list.size();
                long t2 = java.util.Calendar.getInstance().getTimeInMillis();
                this.logger.info("PageListDao - 导出sql：" + sb.toString());
                this.logger.info("PageList - 总记录数：" + this.fullListSize + "，用时：" + (t2 - t1)
                    + " ms");
            }
        }
    }

    private void getPagingParams(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getParameter("page")))
            this.pageNumber = NumberUtils.toInt(request.getParameter("page"), 1);

        if (StringUtils.isNotBlank(request.getParameter("pageSize")))
            this.objectsPerPage = NumberUtils.toInt(request.getParameter("pageSize"), 12);

        if (StringUtils.isNotBlank(request.getParameter("sort")))
            this.sortCriterion = request.getParameter("sort");

        if (StringUtils.isNotBlank(request.getParameter("dir"))) {
            if ("asc".equals(request.getParameter("dir").toLowerCase()))
                this.sortDirection = SortOrderEnum.ASCENDING;
            else if ("desc".equals(request.getParameter("dir").toLowerCase()))
                this.sortDirection = SortOrderEnum.DESCENDING;
            else
                this.sortDirection = null;
        } else {
            this.sortDirection = null;
        }
    }

    @Override
    public int getFullListSize() {
        return this.fullListSize;
    }

    @Override
    public List<T> getList() {
        return this.list;
    }

    @Override
    public int getObjectsPerPage() {
        return this.objectsPerPage;
    }

    @Override
    public int getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public String getSearchId() {
        return this.searchId;
    }

    @Override
    public String getSortCriterion() {
        return this.sortCriterion;
    }

    @Override
    public SortOrderEnum getSortDirection() {
        return this.sortDirection;
    }

	public void setList(List<T> list) {
		this.list = list;
	}
    
    
}
*/