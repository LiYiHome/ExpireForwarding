package com.liyi.start;

import com.liyi.forward.ForwardToRestful;
import com.liyi.persistence.DataSourceFactory;
import com.liyi.persistence.MiniConnectionPoolManager;
import com.liyi.server.ExpireHandler;
import com.liyi.server.ReceiveServer;
import com.liyi.util.LoadConf;

import javax.sql.ConnectionPoolDataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liyi.
 */
public class Application {

    public static LoadConf config = new LoadConf();
    public static Map<Long, CopyOnWriteArraySet<String>> cslm = new ConcurrentSkipListMap<Long, CopyOnWriteArraySet<String>>();
    public static MiniConnectionPoolManager poolMgr = null;
    public static AtomicInteger ai = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ConnectionPoolDataSource dataSource = DataSourceFactory.createDataSource(config.getProperties("derby.url"));
        poolMgr = new MiniConnectionPoolManager(dataSource, 5);
        Thread forwardThread = new Thread(new ForwardToRestful());
        forwardThread.start();
        int port = Integer.parseInt(config.getProperties("network.port"));
        Thread networkThread = new Thread(new ReceiveServer(port));
        networkThread.start();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
        while (true) {
            if (!(Application.cslm.isEmpty())) {
                long currentTime = System.currentTimeMillis();
                for (Map.Entry<Long, CopyOnWriteArraySet<String>> entry : Application.cslm.entrySet()) {
                    long key = entry.getKey();
                    if (currentTime / 1000 >= key) {
                        CopyOnWriteArraySet<String> uidSet = entry.getValue();
                        for (String item : uidSet) {
                            ExpireHandler forHandler = new ExpireHandler(item);
                            fixedThreadPool.submit(forHandler);
                        }
                        Application.cslm.remove(key);
                    } else {
                        break;
                    }
                }
            }
        }
    }

}
