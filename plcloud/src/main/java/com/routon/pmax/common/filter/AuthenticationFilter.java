package com.routon.pmax.common.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.routon.pmax.common.UserProfile;
import com.routon.pmax.common.model.Menu;


public class AuthenticationFilter implements Filter {
	private Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	private String url = "/";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

/*		if(true){
			chain.doFilter(request, response);
			return;
		}*/
		
		try {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			HttpSession session = req.getSession();
			ServletContext application = req.getServletContext();
			String logUrl = req.getContextPath() + this.url;


			if (!logUrl.equals(req.getRequestURI())
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/images")
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/fonts")		
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/css")
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/docs-assets")		
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/js")
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/log")
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/area/tree.do")		
					&& !req.getRequestURI().toLowerCase().startsWith(req.getContextPath() + "/dl")
					&& !req.getRequestURI().toLowerCase()
							.startsWith(req.getContextPath() + "/graphics")
					&& !req.getRequestURI()
							.equalsIgnoreCase(req.getContextPath() + "/login/authen")
					&& !req.getRequestURI().toLowerCase()
							.equalsIgnoreCase(req.getContextPath() + "/copyright")
					&& !req.getRequestURI().toLowerCase()
							.equalsIgnoreCase(req.getContextPath() + "/logout")
					&& !req.getRequestURI().toLowerCase()
							.endsWith(req.getContextPath() + "/login.jsp")) {
				String requestURIWidthoutContext = req.getRequestURI().substring(
						req.getRequestURI().indexOf(req.getContextPath())
								+ req.getContextPath().length());
				UserProfile userProfile = (UserProfile) session.getAttribute("userProfile");
				this.logger.info("currentRequestURL:{}, ctx:{}, withoutContext:{}",
						req.getRequestURI(), req.getContextPath(), requestURIWidthoutContext);

				if (userProfile == null || userProfile.getCurrentUserId() < 0) {
					this.logger.warn("当前的用户会话认证无效，请登录.contextpath[" + req.getContextPath() + "]");
					res.sendRedirect(req.getContextPath() + this.url);
					return;
				} else {
					boolean isURIAuthenticated = false;
					if(req.getRequestURI().equals(req.getContextPath()+"/")){
						isURIAuthenticated = true;
						res.sendRedirect(req.getContextPath() + "/home.do");
						return;
					}
					
					
					application.setAttribute("locale", request.getLocale());
					req.setAttribute("requestURI", requestURIWidthoutContext);
					userProfile.setCurrentUserLoginIp(request.getRemoteAddr());
					
					Map<String, Menu> userPrivilege = (Map<String, Menu>) session
							.getAttribute("userPrivilege");

					if(false){
						isURIAuthenticated = true;
						chain.doFilter(request, response);
						return;
					}
					
					if (userPrivilege != null) {
						for (Iterator<String> key = userPrivilege.keySet().iterator(); key
								.hasNext();) {
							Menu menu = userPrivilege.get(key.next());
							String uriPattern = menu.getMenuPathRegex();
							//System.out.println(uriPattern);
							if(StringUtils.isNotBlank(uriPattern)){
								if (Pattern.compile(uriPattern).matcher(requestURIWidthoutContext)
										.matches()) {
									isURIAuthenticated = true;
									break;
								}
							}
						}
					}

/*					if (!Pattern.compile("/|/40(3|4).do|/unauthen/.+|/changepwd.do|/home.do")
							.matcher(requestURIWidthoutContext).matches()
							&& !isURIAuthenticated) {
						this.logger.warn("用户“{}”没有权限访问此地址资源：{}",
								userProfile.getCurrentUserLoginName(), requestURIWidthoutContext);
						res.sendRedirect(req.getContextPath() + "/403.do");
						return;
					}*/
					/*
					 * if(!req.getRequestURI().equals(req.getContextPath()+"/")&&
					 * !
					 * req.getRequestURI().startsWith(req.getContextPath()+"/menu"
					 * )&&!req.getRequestURI().startsWith(req.getContextPath()+
					 * "/homepage")){ SiteMapBean siteMapBean =
					 * userManager.getSiteMapInfo(((UserProfile)
					 * session.getAttribute
					 * ("userProfile")).getUserId(),req.getRequestURI());
					 * if(siteMapBean.isHasRight()){
					 * request.setAttribute("siteMapString",
					 * siteMapBean.getSiteMapString()); }else{
					 * //this.logger.info("No Permission! Go To 403.jsp.");
					 * //res.sendRedirect(req.getContextPath() + "/403"); } }
					 */
				}
			}
		} catch (Exception e) {
			this.logger.error("授权过滤器异常：" + e.getMessage(), e);
		} catch (Throwable e) {
			this.logger.error("授权过滤器异常：" + e.getMessage(), e);
		} finally {
			//
		}
		
		chain.doFilter(request, response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		try {
			this.url = config.getInitParameter("url");
		} catch (Exception e) {
			this.logger.error("授权过滤器异常：" + e.getMessage(), e);
		} catch (Throwable e) {
			this.logger.error("授权过滤器异常：" + e.getMessage(), e);
		}
	}

}
