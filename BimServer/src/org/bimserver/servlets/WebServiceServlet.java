package org.bimserver.servlets;

/******************************************************************************
 * (c) Copyright bimserver.org 2009
 * Licensed under GNU GPLv3
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * For more information mail to license@bimserver.org
 *
 * Bimserver.org is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bimserver.org is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License a 
 * long with Bimserver.org . If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.bimserver.shared.ServiceInterface;

public class WebServiceServlet extends CXFNonSpringServlet {

	private static final long serialVersionUID = 3633266184012679113L;

	@Override
	public void loadBus(ServletConfig servletConfig) throws ServletException {
		super.loadBus(servletConfig);
		Bus bus = getBus();
		BusFactory.setDefaultBus(bus);
		ServerFactoryBean serverFactoryBean = new ServerFactoryBean();
		Map<String,Object> properties = new HashMap<String, Object>();
		properties.put("mtom-enabled", Boolean.TRUE);
		serverFactoryBean.setProperties(properties);
		serverFactoryBean.setServiceClass(ServiceInterface.class);
		ServiceInterface service = (ServiceInterface) servletConfig.getServletContext().getAttribute("service");
		BeanInvoker beanInvoker = new BeanInvoker(service);
		serverFactoryBean.setInvoker(beanInvoker);
		serverFactoryBean.setAddress("/soap");
		serverFactoryBean.setTransportId("http://schemas.xmlsoap.org/soap/http");
		serverFactoryBean.create();
	}
}