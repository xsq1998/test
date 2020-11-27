package com.shop.service;

import com.shop.pojo.Employee;

public interface EmployeeService {
	Employee login(String username);
	Employee findEmployeeManager(long id);

}
