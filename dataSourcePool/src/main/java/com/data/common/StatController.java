package com.data.common;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatController {
    @GetMapping("/druid/stat")
    public Object druidStat() {
//        DruidDataSourceStatManager instance = DruidDataSourceStatManager.getInstance();
        return DruidStatManagerFacade.getInstance().getDataSourceStatDataList();
    }
}
