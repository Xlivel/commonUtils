package com.data.common.utils;

public class ExcelFont {

    private short color; //设置字体颜色
    private boolean bold; //设置是否粗体
    private boolean italic; //设置倾斜
    private byte underline; //设置下划线

    public ExcelFont(short color, boolean bold, boolean italic, byte underline) {
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }

    public short getColor() {
        return color;
    }

    public void setColor(short color) {
        this.color = color;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public byte getUnderline() {
        return underline;
    }

    public void setUnderline(byte underline) {
        this.underline = underline;
    }
}
