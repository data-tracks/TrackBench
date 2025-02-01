package dev.trackbench.util.database;

import dev.trackbench.configuration.BenchmarkConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LiteDB {

    private final String name;

    private final BenchmarkConfig config;

    private static final String DB_PREFIX = "jdbc:lite:";
    private final Connection connection;


    public LiteDB( String name, BenchmarkConfig config ) {
        this.name = name;
        this.config = config;
        this.connection = connect();
    }


    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection( DB_PREFIX + this.name );
            System.out.println( "Connected to SQLite." );
        } catch ( SQLException e ) {
            System.out.println( e.getMessage() );
        }
        return conn;
    }


}
