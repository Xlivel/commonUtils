package com.data.common.utils;

import com.sun.istack.NotNull;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 单元格规则： 内容^备注^表格颜色^字体颜色^垂直位置^左右位置
 */
public class ExcelUtil {

    private String blank = "";
    private String noneValue = "";

    public List<Map<String, Object>> readExcel(InputStream inputStream, String suffixes, String sheetName, Integer sheetIndex) {
        Workbook sheets = getSheets(inputStream, suffixes);
        Sheet sheet = null;
        if (sheets != null) {
            if (StringUtils.isBlank(sheetName) && sheetIndex == null) {
                sheets.getSheetAt(sheets.getActiveSheetIndex());
            } else if (StringUtils.isNoneBlank(sheetName)) {
                sheet = sheets.getSheet(sheetName);
            } else {
                sheet = sheets.getSheetAt(sheetIndex);
            }
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum > 0) {
                List<Cell> title = getTitle(sheet);
                return getDataMap(title, sheet, lastRowNum);
            }
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> getDataMap(List<Cell> title, Sheet sheet, int lastRowNum) {
        List<Map<String, Object>> dataList = new ArrayList<>(lastRowNum);
        for (int i = 1; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            row2Map(row, title, dataList);
        }
        return dataList;
    }

    private void row2Map(Row row, List<Cell> title, List<Map<String, Object>> dataList) {
        Map<String, Object> objectObjectHashMap = new HashMap<>(1);
        for (int i = 0; i < title.size(); i++) {
            Cell titleCell = title.get(i);
            Cell cell = row.getCell(i);
            String titleString = titleCell.getStringCellValue();
            if (StringUtils.isBlank(titleCell.getStringCellValue())) {
                titleString = String.valueOf(titleCell.getColumnIndex());
            }
            if (cell != null) {
                switch (cell.getCellType()) {
                    case BLANK:
                        objectObjectHashMap.put(titleString, blank);
                        break;
                    case _NONE:
                        objectObjectHashMap.put(titleString, noneValue);
                        break;
                    case STRING:
                        objectObjectHashMap.put(titleString, cell.getStringCellValue());
                        break;
                    case BOOLEAN:
                        objectObjectHashMap.put(titleString, cell.getBooleanCellValue());
                        break;
                    case NUMERIC:
                        objectObjectHashMap.put(titleString, cell.getNumericCellValue());
                        break;
                    case FORMULA:
                        objectObjectHashMap.put(titleString, cell.getCellFormula());
                        break;
                    case ERROR:
                        objectObjectHashMap.put(titleString, cell.getErrorCellValue());
                        break;
                }
            } else
                objectObjectHashMap.put(titleString, "");

        }
        dataList.add(objectObjectHashMap);
    }

    private List<Cell> getTitle(Sheet sheet) {
        Row row = sheet.getRow(0);
        List<Cell> cellList = Lists.newArrayList();
        short lastCellNum = row.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cellList.add(cell);
        }
        return cellList;
    }

    private Workbook getSheets(InputStream inputStream, String suffixes) {
        try {
            if ("xls".equals(suffixes.toLowerCase())) {
                return new HSSFWorkbook(inputStream);
            } else {
                return new XSSFWorkbook(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void writeExcel(OutputStream outputStream, List<Map<String, Object>> dataList, List<String> titles, @NotNull String suffixes) {
        Workbook workbook;
        if ("xls".equals(suffixes.toLowerCase())) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
        Sheet sheet = workbook.createSheet();
        // 创建标题行
        Row row = sheet.createRow(0);
        // 存储标题在Excel文件中的序号
        Map<String, Integer> titleOrder = new HashMap<>();
        for (int i = 0; i < titles.size(); i++) {
            Cell cell = row.createCell(i);
            String title = titles.get(i);
            cell.setCellValue(title);
            titleOrder.put(title, i);
        }
        // 写入正文
        Iterator<Map<String, Object>> iterator = dataList.iterator();
        // 行号
        int index = 1;
        while (iterator.hasNext()) {
            row = sheet.createRow(index);
            Map<String, Object> value = iterator.next();
            for (Map.Entry<String, Object> map : value.entrySet()) {
                // 获取列名
                String title = map.getKey();
                // 根据列名获取序号
                int i = titleOrder.get(title);
                // 在指定序号处创建cell
                Cell cell = row.createCell(i);
                // 获取列的值
                Object object = map.getValue();
                // 判断object的类型
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (object instanceof Double) {
                    cell.setCellValue((Double) object);
                } else if (object instanceof Date) {
                    String time = simpleDateFormat.format((Date) object);
                    cell.setCellValue(time);
                } else if (object instanceof Calendar) {
                    Calendar calendar = (Calendar) object;
                    String time = simpleDateFormat.format(calendar.getTime());
                    cell.setCellValue(time);
                } else if (object instanceof Boolean) {
                    cell.setCellValue((Boolean) object);
                } else {
                    if (object != null) {
                        cell.setCellValue(object.toString());
                    }
                }
            }
            index++;
        }
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        ExcelUtil excelUtil = new ExcelUtil();
        FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\wu_ang\\Desktop\\3.24-4.21补贴 - 副本.xlsx"));
        List<Map<String, Object>> maps = excelUtil.readExcel(fileInputStream, "xlsx", "", 1);

        excelUtil.writeExcel(null, maps, null, "xlsx");

        System.out.println("123");
    }

}
