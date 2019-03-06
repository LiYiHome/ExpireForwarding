package com.liyi.persistence;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;

/**
 * Created by liyi.
 */
public class DataSourceFactory {

    /**
     * 创建数据源连接池
     *
     * @param url 数据源地址
     * @return 返回数据源连接池对象
     */
    public static ConnectionPoolDataSource createDataSource(String url) {
        EmbeddedConnectionPoolDataSource dataSource = new EmbeddedConnectionPoolDataSource();
        dataSource.setDatabaseName(url);
        dataSource.setCreateDatabase("create");
        try {
            dataSource.setLogWriter(new PrintWriter(System.out));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }

}
