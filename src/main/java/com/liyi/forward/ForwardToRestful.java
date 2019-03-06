package com.liyi.forward;

import com.liyi.start.Application;
import com.liyi.util.Constants;
import com.liyi.util.LoadConf;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyi.
 */
public class ForwardToRestful implements Runnable {

    /**
     * 查询失效数据
     *
     * @return 返回列表List，列表中每个元素类型是JSONObject对象，每个对象只包含p_uid、p_content两个属性。
     */
    public List<JSONObject> queryData() {
        List<JSONObject> result = new ArrayList<JSONObject>();
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = Application.poolMgr.getConnection();
            ps = conn.prepareStatement("select p_uid, p_content from expfor.persistence where p_alive_status = 0 order by p_expire_time asc");
            ResultSet rs = ps.executeQuery();
            int size = rs.getFetchSize();
            if (size > 0) {
                while (rs.next()) {
                    JSONObject item = new JSONObject();
                    item.put(Constants.P_UID, rs.getString(1));
                    item.put(Constants.P_CONTENT, rs.getString(2));
                    result.add(item);
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
        return result;
    }

    /**
     * 删除失效数据
     *
     * @param uid  数据唯一标识
     */
    public void deleteData(String uid) {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = Application.poolMgr.getConnection();
            ps = conn.prepareStatement("delete from expfor.persistence where p_uid = ?");
            ps.setString(1, uid);
            int updateResult = ps.executeUpdate();
            if (updateResult == 1) {
                System.out.printf("删除%s成功!%n", uid);
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
     * 将失效数据转发到下游进行处理
     *
     * @param client  HttpClient对象实例
     * @param httpPost  HttpPost对象实例
     * @param entiry  失效数据内容
     * @return 返回JSONObject对象，里面包含p_uid、p_status两个属性
     */
    public JSONObject forwardData(CloseableHttpClient client, HttpPost httpPost, String entiry) {
        String forName = "UTF-8";
        JSONObject result = new JSONObject();
        httpPost.setEntity(new StringEntity(entiry, Charset.forName(forName)));
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpPost);
            String responseContent = EntityUtils.toString(response.getEntity(), forName);
            result = JSONObject.fromObject(responseContent);
        } catch (IOException e) {
            System.out.println("接口返回结果出现异常!");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void run() {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(Application.config.getProperties("http.url"));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        while (true) {
            if (Application.ai.get() > 0) {
                List<JSONObject> dataList = this.queryData();
                for (JSONObject entiry : dataList) {
                    System.out.println("entiry = "+entiry.toString());
                    String uid = entiry.getString(Constants.P_UID);
                    JSONObject response = this.forwardData(client, httpPost, entiry.toString());
                    if(response.containsKey(Constants.P_UID) && response.containsKey(Constants.P_STATUS) &&
                            response.getString(Constants.P_UID).equals(uid) && response.getInt(Constants.P_STATUS) == Constants.STATUS_200) {
                        this.deleteData(uid);
                        Application.ai.decrementAndGet();
                    } else {
                        System.out.printf("转发%s内容失败!%n", uid);
                    }
                }
            }
        }
    }

}
