package com.data.common.utils;

import java.util.Map;

public class StyleCollection {

    private Map<String, CellStyle> cellStyleMap;
    private Map<String, ExcelFont> excelFontMap;

    public Map<String, CellStyle> getCellStyleMap() {
        return cellStyleMap;
    }

    public void setCellStyleMap(Map<String, CellStyle> cellStyleMap) {
        this.cellStyleMap = cellStyleMap;
    }

    public Map<String, ExcelFont> getExcelFontMap() {
        return excelFontMap;
    }

    public void setExcelFontMap(Map<String, ExcelFont> excelFontMap) {
        this.excelFontMap = excelFontMap;
    }
}
