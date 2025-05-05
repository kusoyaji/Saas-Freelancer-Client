package com.freelancer.portal.monitoring;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Custom formatter for P6Spy SQL logging.
 * Formats SQL logs in a more readable and informative way, including execution time.
 */
public class CustomLineFormat implements MessageFormattingStrategy {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, 
                               String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n=================== P6Spy SQL Execution ===================\n");
        sb.append("Time         : ").append(FORMAT.format(new Date())).append("\n");
        sb.append("Elapsed Time : ").append(elapsed).append("ms\n");
        sb.append("Connection ID: ").append(connectionId).append("\n");
        sb.append("Category     : ").append(category).append("\n");
        sb.append("SQL          : \n").append(formatSql(sql)).append("\n");
        sb.append("==========================================================\n");
        
        return sb.toString();
    }
    
    /**
     * Format SQL for better readability
     */
    private String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        
        // Simple SQL formatting - in a real implementation, 
        // consider using a dedicated SQL formatter library
        String formattedSql = sql.trim()
                .replace("\n", " ")
                .replaceAll("\\s+", " ");
        
        // Add line breaks for better readability
        formattedSql = formattedSql
                .replace("SELECT ", "\nSELECT ")
                .replace("FROM ", "\nFROM ")
                .replace("WHERE ", "\nWHERE ")
                .replace("ORDER BY ", "\nORDER BY ")
                .replace("GROUP BY ", "\nGROUP BY ")
                .replace("HAVING ", "\nHAVING ")
                .replace("LEFT JOIN ", "\nLEFT JOIN ")
                .replace("RIGHT JOIN ", "\nRIGHT JOIN ")
                .replace("INNER JOIN ", "\nINNER JOIN ")
                .replace("AND ", "\n  AND ")
                .replace("OR ", "\n  OR ");
        
        return formattedSql;
    }
}