package com.situ.elder;

import com.alibaba.excel.EasyExcel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class EasyExcelDemo {
    @Test
    public void testWrite() {
        //构建数据的集合
        List<Student> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Student data = new Student();
            data.setId(i);
            data.setName("excel" + i);
            list.add(data);
        }

        //设置excel文件的路径和文件的名称
        String fileName = "D:\\excel\\01.xlsx";
        //调用方法实现写的操作
        EasyExcel.write(fileName, Student.class).sheet("用户的信息").doWrite(list);
    }

    @Test
    public void testRead() {
        String fileName = "D:\\excel\\01.xlsx";
        //调用方法实现读取操作
        EasyExcel.read(fileName, Student.class, new ExcelListener()).sheet().doRead();
    }

}
