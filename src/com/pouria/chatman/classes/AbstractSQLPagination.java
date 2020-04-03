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

import java.sql.*;

/**
 *
 * @author pouriap
 * 
 * an abstract pagination class
 * gets the query and limit and returns limit-sized results of that query
 */
public abstract class AbstractSQLPagination {

	private final int limit;
	private final String query;
	private final String dbPath;
	
	private int page = -1;
	private boolean hasMore = true;

    public AbstractSQLPagination(int limit, String dbPath, String query){
        this.query = query;
        this.dbPath = dbPath;
        this.limit = limit;
    }

    //loads one page(fetches a limit-sezed result of the given query)
    private void loadPage(int page) throws Exception{

        try(Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath) ){

        	Class.forName("org.sqlite.JDBC");
	        con.setAutoCommit(false);

	        Statement countStmt = con.createStatement();
	        ResultSet countRes = countStmt.executeQuery("select count(*) as 'count' from chat_sessions");
			countRes.next();
			int allRowsCount = countRes.getInt("count");

	        Statement pageStmt = con.createStatement();
            int l1 = page * limit;
            int l2 = limit;
	        ResultSet pageRes = pageStmt.executeQuery( query + " LIMIT " + l1 + "," + l2 );
            
            //the doPopulate method is abstract
            //user should implement it and do whatever he wants with the result set
	        int pageRowsCount = 0;
            while(pageRes.next()){
	            doPopulate(pageRes, page);
	            pageRowsCount++;
            }
            
            hasMore = allRowsCount > ( (limit * page) + pageRowsCount);

	        countRes.close();
	        countStmt.close();
            pageRes.close();
            pageStmt.close();

        }catch(Exception e){
        	//let try close the resource then throw the exception
        	throw e;
        }

    }
    
    //loads the next page. the doPopulate method is called during the process
    public void nextPage() throws Exception{

        if(!hasNext()){
	        return;
        }

	    page++;
        loadPage(page);
    }
    
    //loads the previous page. the doPopulate method is called during the process
    public void prevPage() throws Exception{

        if(!hasPrev()){
	        return;
        }

	    page--;
        loadPage(page);
    }
    
    public boolean hasNext(){
        return hasMore;
    }
    
    public boolean hasPrev() {
        return page - 1 >= 0;
    }

    //Should return the number of results
    protected abstract void doPopulate(ResultSet rowInPage, int pageNumber) throws Exception;
    
}
