package com.liyi.server;

import com.liyi.start.Application;
import com.liyi.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import net.sf.json.JSONObject;

import java.sql.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by liyi.
 */
public class ReceiveServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 添加数据
     *
     * @param uid  数据唯一标识
     * @param expireTime  数据失效时间
     * @param content  数据内容
     */
    public void insertData(String uid, Long expireTime, String content) {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = Application.poolMgr.getConnection();
            ps = conn.prepareStatement("insert into expfor.persistence(p_uid, p_expire_time, p_content) values(?, ?, ?)");
            ps.setString(1, uid);
            ps.setLong(2, expireTime);
            ps.setString(3, content);
            int updateResult = ps.executeUpdate();
            if (updateResult == 1) {
                System.out.printf("添加%s成功!%n", uid);
            }
        } catch (SQLException e) {
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

    /**
     * 更新数据
     *
     * @param uid  数据唯一标识
     * @param content  数据内容
     */
    public void updateData(String uid, String content) {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = Application.poolMgr.getConnection();
            ps = conn.prepareStatement("select p_content from expfor.persistence where p_uid = ?");
            ps.setString(1, uid);
            ResultSet rs = ps.executeQuery();
            int size = rs.getFetchSize();
            if (size == 1) {
                String temp = "";
                while (rs.next()) {
                    temp = rs.getString(1);
                }
                content = temp + ", " + content;
                ps = conn.prepareStatement("update expfor.persistence set p_content = ? where p_uid = ?");
                ps.setString(1, content);
                ps.setString(2, uid);
                int updateResult = ps.executeUpdate();
                if (updateResult == 1) {
                    System.out.printf("更新%s: p_content成功!%n", uid);
                }
            }
        } catch (SQLException e) {
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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        JSONObject json = JSONObject.fromObject(in.toString(CharsetUtil.UTF_8));
        System.out.println("服务端接收到消息: " + json.toString());
        ReferenceCountUtil.release(msg);
        JSONObject result = new JSONObject();
        if (json.containsKey(Constants.P_UID) && json.containsKey(Constants.P_EXPIRE_TIME) && json.containsKey(Constants.P_CONTENT)) {
            String uid = json.getString(Constants.P_UID);
            Long expireTime = json.getLong(Constants.P_EXPIRE_TIME);
            String content = json.getString(Constants.P_CONTENT);
            if (!(Application.cslm.containsKey(expireTime))) {
                //失效时间首次出现
                CopyOnWriteArraySet<String> uidSet = new CopyOnWriteArraySet<String>();
                uidSet.add(uid);
                Application.cslm.put(expireTime, uidSet);
                this.insertData(uid, expireTime, content);
            } else {
                //失效时间非首次出现
                CopyOnWriteArraySet<String> uidSet = Application.cslm.get(expireTime);
                if (uidSet.contains(uid)) {
                    //唯一标识非首次出现
                    this.updateData(uid, content);
                } else {
                    //唯一标识首次出现
                    uidSet.add(uid);
                    Application.cslm.put(expireTime, uidSet);
                    this.insertData(uid, expireTime, content);
                }
            }
            result.put(Constants.P_UID, uid);
            result.put(Constants.P_STATUS, Constants.STATUS_200);
        } else {
            result.put(Constants.P_STATUS, Constants.STATUS_400);
        }
        ctx.writeAndFlush(Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务端关闭通道!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
