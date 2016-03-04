/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author SH
 */
public class HistoryTablePagination extends AbstractPagination{
    
    ChatFrame gui;
    JTable table;
    
    public HistoryTablePagination(JTable table, String dbPath, String query){
        super(dbPath, query);
        this.gui = ChatFrame.getInstance();
        this.table = table;
    }
    
    @Override
    protected int doPopulate(ResultSet rs){

        DateFormat dateFormat;
        String format; 
        Date now = new Date();
        int resultCount = 0;
        
        ((DefaultTableModel)this.table.getModel()).setRowCount(0);
        try {
            while (rs.next()) {
                Date d = new Date(rs.getLong("date"));
                String text = rs.getString("text");

                dateFormat = new SimpleDateFormat("E");
                String weekDay = dateFormat.format(d);
                format = "'" + gui.persianWeekDay(weekDay) + "' HH:mm yyyy-MM-dd ";
                
                dateFormat = new SimpleDateFormat("dd");
                int todayNumber = Integer.valueOf(dateFormat.format(now));
                dateFormat = new SimpleDateFormat("dd");
                int dayNumber = Integer.valueOf(dateFormat.format(d));

                long distance =  now.getTime() - d.getTime();
                if(distance < 24*60*60*1000){
                    if(todayNumber == dayNumber)
                        format = "'امروز'   HH:mm ";
                    else
                        format = "'دیروز'   HH:mm ";
                }
   
                dateFormat = new SimpleDateFormat(format);
                ((DefaultTableModel)this.table.getModel()).addRow(new Object[]{dateFormat.format(d), text});
                
                resultCount++;
            }//while
            
        }catch ( Exception e ){
            gui.message("could not load history: " + e.getMessage());
        }
        
        return resultCount;
    }
}
