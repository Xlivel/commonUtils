package com.data.common.dataSourcePool;

import org.apache.commons.lang3.StringUtils;

public class ValidationQuery {

    public static String getValidationQuery(String driver) {
        if (StringUtils.isNotBlank(driver)) {
            if (StringUtils.containsIgnoreCase(driver, "oracle")) {
                return "select 1 from dual";
            } else if (StringUtils.containsIgnoreCase(driver, "db2")) {
                return "select 1 from sysibm.sysdummy1";
            } else if (StringUtils.containsIgnoreCase(driver, "hsqldb")) {
                return "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";
            } else if (StringUtils.containsIgnoreCase(driver, "postgresql")) {
                return "select version()";
            } else if (StringUtils.containsIgnoreCase(driver, "informix")) {
                return "select count(*) from systables";
            } else {
                //默认MySQL，Microsoft SQL Server，SQLite，ingres，Apache Derby，H2
                return "select 1";
            }
        }
        return null;
    }

}
