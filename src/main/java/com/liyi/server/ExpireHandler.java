package com.liyi.server;

import com.liyi.start.Application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by liyi.
 */
public class ExpireHandler implements Runnable {

    private String uid;

    public ExpireHandler(String uid) {
        this.uid = uid;
    }

    public void run() {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = Application.poolMgr.getConnection();
            ps = conn.prepareStatement("update expfor.persistence set p_alive_status = 0 where p_uid = ?");
            ps.setString(1, uid);
            int updateResult = ps.executeUpdate();
            if (updateResult == 1) {
                System.out.printf("更新%s: p_status成功!%n", uid);
                Application.ai.incrementAndGet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
