package com.shop.listener;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shop.pojo.Employee;
import com.shop.service.EmployeeService;
import com.shop.utils.Constant;

public class CustomerTaskAssignee implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		// 调用业务类查找出上一级
		// 获取到spring容器
		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
		EmployeeService emServiece = (EmployeeService) context.getBean("employeeService");
		//获取request对象
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder
				.getRequestAttributes()).getRequest();
		Employee e = (Employee) request.getSession().getAttribute(Constant.GLOBLE_USER_SESSION);
		// 根据managerId拿到的上一级
		Employee manager = emServiece.findEmployeeManager(e.getManagerId());
		// 使用监听器分配待办人
		delegateTask.setAssignee(manager.getName());
		

	}

}
