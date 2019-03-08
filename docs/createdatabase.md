## 安装Derby数据库

* 下载和解压安装包
    ```
    mkdir -p /opt/derby
    cd /opt/derby
    wget https://archive.apache.org/dist/db/derby/db-derby-10.13.1.1/db-derby-10.13.1.1-bin.zip
    unzip db-derby-10.13.1.1-bin
    cd db-derby-10.13.1.1-bin
    ```

* 配置环境变量
    ```
    vim /etc/profile
	    export DERBY_HOME=/opt/derby/db-derby-10.14.2.0-bin
	    export PATH=$DERBY_HOME/bin:$PATH
    :wq
    source /etc/profile
    ```
    
* 验证安装是否正确
    ```
    java -jar $DERBY_HOME/lib/derbyrun.jar sysinfo
        ------------------ Java 信息 ------------------
        Java 版本：        1.8.0_91
        Java 供应商：      Oracle Corporation
        Java 主目录：      /opt/jre1.8.0_91
        Java 类路径：      /opt/derby/db-derby-10.13.1.1-bin/lib/derbyrun.jar
        OS 名：            Linux
        OS 体系结构：      amd64
        OS 版本：          3.10.0-327.36.1.el7.x86_64
        Java 用户名：      root
        Java 用户主目录：/root
        Java 用户目录：    /opt/derby/db-derby-10.13.1.1-bin
        java.specification.name: Java Platform API Specification
        java.specification.version: 1.8
        java.runtime.version: 1.8.0_91-b14
        --------- Derby 信息 --------
        [/opt/derby/db-derby-10.13.1.1-bin/lib/derby.jar] 10.13.1.1 - (1765088)
        [/opt/derby/db-derby-10.13.1.1-bin/lib/derbytools.jar] 10.13.1.1 - (1765088)
        [/opt/derby/db-derby-10.13.1.1-bin/lib/derbynet.jar] 10.13.1.1 - (1765088)
        [/opt/derby/db-derby-10.13.1.1-bin/lib/derbyclient.jar] 10.13.1.1 - (1765088)
        [/opt/derby/db-derby-10.13.1.1-bin/lib/derbyoptionaltools.jar] 10.13.1.1 - (1765088)
    ```
    
* 创建数据库和表
    ```
    $DERBY_HOME/bin/ij
    connect 'jdbc:derby:expfor;create=true';
    create table expfor.persistence (
    p_uid varchar(20) primary key,
    p_expire_time bigint not null,
    p_content varchar(5000),
    p_alive_status int default 1);
    ```
* 拷贝数据库目录并修改配置文件属性derby.url的值
    ```
    derby.url = /opt/derby/db-derby-10.13.1.1-bin/expfor
    ```
