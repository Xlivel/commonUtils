package com.data.common.utils;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public class CellStyle {

    private ExcelFont font; //为单元格设置字体样式
    private HorizontalAlignment horizontalAlignment; // 设置水平对齐方式
    private VerticalAlignment verticalAlignment; // 设置垂直对齐方式
    private FillPatternType fillPattern; //
    private short fillForegroundColor; //设置前景色
    private short fillBackgroundColor; //设置背景颜色

    public CellStyle(ExcelFont font, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, FillPatternType fillPattern, short fillForegroundColor, short fillBackgroundColor) {
        this.font = font;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.fillPattern = fillPattern;
        this.fillForegroundColor = fillForegroundColor;
        this.fillBackgroundColor = fillBackgroundColor;
    }

    public ExcelFont getFont() {
        return font;
    }

    public void setFont(ExcelFont font) {
        this.font = font;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public FillPatternType getFillPattern() {
        return fillPattern;
    }

    public void setFillPattern(FillPatternType fillPattern) {
        this.fillPattern = fillPattern;
    }

    public short getFillForegroundColor() {
        return fillForegroundColor;
    }

    public void setFillForegroundColor(short fillForegroundColor) {
        this.fillForegroundColor = fillForegroundColor;
    }

    public short getFillBackgroundColor() {
        return fillBackgroundColor;
    }

    public void setFillBackgroundColor(short fillBackgroundColor) {
        this.fillBackgroundColor = fillBackgroundColor;
    }
}
