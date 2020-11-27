package com.shop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.LeavebillMapper;
import com.shop.pojo.Leavebill;
import com.shop.service.LeaveBillService;

@Service
public class LeaveBillServiceImpl implements LeaveBillService {

	@Autowired
	private LeavebillMapper leaveBillMapper;
	@Override
	public void saveLeaveBill(Leavebill leaveBill) {
		if (leaveBill.getId()!=null) {
			leaveBillMapper.updateByPrimaryKey(leaveBill);
		}
		else {
			this.leaveBillMapper.insert(leaveBill);
		}
	}

	
	

}
