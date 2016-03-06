/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.classes;

import com.pouria.chatman.gui.ChatFrame;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author pouriap
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
        int oneDay = 24*60*60*1000;
        int resultCount = 0;
        
        ((DefaultTableModel)this.table.getModel()).setRowCount(0);
        try {
            while (rs.next()) {
                Date d = new Date(rs.getLong("date"));
                String text = rs.getString("text");

                //u = day of week
                dateFormat = new SimpleDateFormat("u");
                String weekDay = dateFormat.format(d);
                String[] weekdays = gui.l.getString("week_days").split(",");
                weekDay = weekdays[Integer.valueOf(weekDay)];
                format = "'" + weekDay + "' HH:mm yyyy-MM-dd ";
                
                //D = day in year
                dateFormat = new SimpleDateFormat("D");
                int todayNumber = Integer.valueOf(dateFormat.format(now));
                dateFormat = new SimpleDateFormat("D");
                int dayNumber = Integer.valueOf(dateFormat.format(d));
                
                long distance =  now.getTime() - d.getTime();
                //because we only want it to be applied to last week
                //it doesn't work on new years first week but whatever! ;)
                if(distance < (7 * oneDay)){
                    switch(todayNumber - dayNumber){
                        case 0:
                            format = "'" + gui.l.getString("today") + "'   HH:mm ";
                            break;
                        case 1:
                            format = "'" + gui.l.getString("yesterday") + "'   HH:mm ";
                            break;
                        case 2:
                            format = "'" + gui.l.getString("two_days_ago") + "'   HH:mm ";
                            break;
                        case 3:
                            format = "'" + gui.l.getString("three_days_ago") + "'   HH:mm ";
                            break;
                        case 4:
                            format = "'" + gui.l.getString("four_days_ago") + "'   HH:mm ";
                            break;
                        default:
                            break;
                            
                    }
                }
   
                dateFormat = new SimpleDateFormat(format);
                ((DefaultTableModel)this.table.getModel()).addRow(new Object[]{dateFormat.format(d), text});
                
                resultCount++;
            }//while
            
        }catch ( Exception e ){
            gui.message(gui.l.getString("history_fail") + e.getMessage());
        }
        
        return resultCount;
    }
}
