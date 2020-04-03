/*
 * Copyright (c) 2020. Pouria Pirhadi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.classes;

import com.pouria.chatman.CMHelper;
import com.puria.ShamsiDate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author pouriap
 * 
 * our implementation of the AbstractPagination class
 */
public class HistoryTablePagination extends AbstractSQLPagination {
    
    private final static String QUERY = "SELECT date,text FROM chat_sessions ORDER BY date DESC";
    private final JTable table;

    public HistoryTablePagination(int limit, JTable table, String dbPath){
        super(limit, dbPath, QUERY);
        this.table = table;
    }
    
    @Override
    protected void doPopulate(ResultSet row, int pageNumber) throws Exception{

        DateFormat dateFormat;
        String format; 
        Date now = new Date();
        long oneDay = TimeUnit.DAYS.toMillis(1);

        Date d = new Date(row.getLong("date"));
        String textCol = row.getString("text");
		String dateCol = "";

        //u = day of week
        dateFormat = new SimpleDateFormat("u");
        String weekDay = dateFormat.format(d);
        String[] weekdays = CMHelper.getInstance().getStr("week_days").split(",");
        weekDay = weekdays[Integer.parseInt(weekDay) -1];
        format = "'" + weekDay + "' yyyy-MM-dd ";

        //D = day in year
        dateFormat = new SimpleDateFormat("D");
        int todayNumber = Integer.parseInt(dateFormat.format(now));
        dateFormat = new SimpleDateFormat("D");
        int dayNumber = Integer.parseInt(dateFormat.format(d));

        long distance =  now.getTime() - d.getTime();
        //because we only want it to be applied to last week
        //it doesn't work on new years first week but whatever! ;)
        if(distance < (4 * oneDay)){
            switch(todayNumber - dayNumber){
                case 0:
                    format = "'" + CMHelper.getInstance().getStr("today") + " '";
                    break;
                case 1:
                    format = "'" + CMHelper.getInstance().getStr("yesterday") + " '";
                    break;
                case 2:
                    format = "'" + CMHelper.getInstance().getStr("two_days_ago") + " '";
                    break;
                case 3:
                    format = "'" + CMHelper.getInstance().getStr("three_days_ago") + " '";
                    break;
                case 4:
                    format = "'" + CMHelper.getInstance().getStr("four_days_ago") + " '";
                    break;
                default:
                    break;
            }
			dateFormat = new SimpleDateFormat(format);
			dateCol = dateFormat.format(d);
        }
		else{
			ShamsiDate shamsiDate = new ShamsiDate(d.getTime());
			dateCol = " " + shamsiDate.getStandardShamsi();
		}

        ((DefaultTableModel)this.table.getModel()).addRow(new Object[]{dateCol, textCol});

    }
}
