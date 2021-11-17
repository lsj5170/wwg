package com.project.wwg.comm.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.project.wwg.comm.model.notice;


@Repository
public class notice_daolmpl implements notice_dao {
	@Autowired
	private SqlSessionTemplate sst;

//	public List<Board> list(int startRow, int endRow) {
	public List<notice> list(notice board) {
/*		HashMap<String, Integer> hm=new HashMap<String, Integer>();
		hm.put("startRow",startRow);
		hm.put("endRow",endRow);*/
		return sst.selectList("notice.list",board);
	}
	
	public int getTotal(notice board) {
		return sst.selectOne("notice.getTotal",board);
	}

}