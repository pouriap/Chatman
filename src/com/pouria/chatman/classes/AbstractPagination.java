/*
 * Copyright (C) 2016 Pouria Pirhadi
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author pouriap
 * 
 * an abstract pagination class
 * gets the query and limit and returns limit-sized results of that query
 */
public abstract class AbstractPagination {
    protected int limit = 10;
    protected int page = 0;
    protected boolean hasMore = true;
    protected String[] resultSet;
    protected String query;
    protected String dbPath;
    
    //default limit is 10
    public AbstractPagination(String dbPath, String query){
        this.query = query;
        this.dbPath = dbPath;
        
        resultSet = new String[this.limit];
    }
    
    public AbstractPagination(int limit, String dbPath, String query){
        this.query = query;
        this.dbPath = dbPath;
        this.limit = limit;
        
        resultSet = new String[this.limit];
    }

    //loads one page(fetches a limit-sezed result of the given query)
    private void loadPage(int page) throws SQLException{
   
        Connection c;
        Statement stmt;
        ResultSet rs;
        
        //pages start from 1 but indices start from 0
        page--;
        
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            c.setAutoCommit(false);

            stmt = c.createStatement();
            int l1 = page * limit;
            int l2 = limit;
            rs = stmt.executeQuery( query + " LIMIT " + l1 + "," + l2 );
            
            //the doPopulate(ResultSet) method is abstract
            //user should implement it and do whatever he wants with the result set and return the number of results
            //because Java is stupid and doesn't even have a function to give us the number of results
            int count = doPopulate(rs);
            
            //if number of results is the same as limit it means we have more
            //unless in the occasion that we have exactly the same number of records as limit
            //we know about that occassion, we just don't care
            hasMore = count == limit;

            rs.close();
            stmt.close();
            c.close();
        }catch(ClassNotFoundException e){
            //i know. i just don't care. (it's a reference from the batman movie)
        }

    }
    
    //loads the next page. the doPopulate method is called during the process
    public boolean nextPage() throws SQLException{
        if(!hasNext())
            return false;
        
        page++;
        loadPage(page);
        return true;
       
    }
    
    //loads the previous page. the doPopulate method is called during the process
    public boolean prevPage() throws SQLException{
        if(!hasPrev())
            return false;
        
        page--;
        loadPage(page);
        return true;
    }
    
    public boolean hasNext(){
        //page is set to -1 if result is less than limit
        return hasMore;
    }
    
    public boolean hasPrev() {
        return page-1 > 0;
    }
    
    public String[] getResultSet(){
        return resultSet;
    }
    
    public void setLimit(int limit){
        this.limit = limit;
    }
    
    public void setPage(int page){
        this.page = page;
    }
    
    public void setQuery(String query){
        this.query = query;
    }
    
    //Should return the number of results
    protected abstract int doPopulate(ResultSet result);
    
}
