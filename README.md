# CUIT XCPC TOOL

---

一个用于简化代码打印和气球打印的XCPC工具，为CUIT的XCPC校赛开发。

### 使用

本程序使用 Java 21开发，理论上Windows, Linux, macOS都能使用。

通过下面命令启动

```bash
java -jar CUITXCPCTool-XXX.jar
```

第一次启动的时候会生成`settings.properties`和`db-secret.properties`文件。

- `db-secret.properties`用于配置本地`H2`数据库密码

- `settings.properties`中需要注意：
  
  - `org.domjudge.use-special-client`如果设置为`true`，理论上本程序只设计了对`OMSClient 1.X`的判断，具体依赖请求时会发送的特别`Cookie`，如果需要实现对其它客户端的判断，需要修改`DjAuthService`的实现。
  
  - 开启后需要配合`org.domjudge.nginx-verify-route-path`使用，原理是：

```plaintext
校验通过——>返回token——>带token跳转-->nginx截获token-->请求验证token-->进入Domjudge
```

- 为了确保稳定使用，推荐给`Domjudge`设置`BaseURL`。
  
> [!NOTE]
> nginx的配置在`script`下有示例。

- `org.domjudge.print-token`用于`Domjudge`发送打印任务时校验。

关于URL的拼接，参考下面实现：

```java
public String getDomjudgeBalloonApiUrl(Boolean isNotDone){
    return this.host + ":" + this.port + routePath + "/api/v4/contests/" + contestId + "/balloons?todo=" + isNotDone.toString();
}

public String getDomjudgeBalloonApiUrl(Long balloonId){
    return this.host + ":" + this.port + routePath + "/api/v4/contests/" + contestId + "/balloons" + "/" + balloonId + "/done";
}

public String getVerifyUrl(){
    return this.host + ":" + this.port + this.nginxVerifyRoutePath;
}
```

### 自动打印

自动打印需要配合`Google Chrome`使用，其它浏览器未测试。

> [!NOTE]
> 
> `Domjudge`使用的打印命令：
> 
> ```bash
> /opt/print.sh "[location]" "[teamname]" "[teamid]" "[file]" 2>&1
> ```
> 
> 脚本在`script`下有示例。

方法如下：

> [!NOTE]
> 
> 开启自动打印前，请打印测试页，确保打印机可以正常工作，并检查测试页内容和格式

1. 关闭所有浏览器窗口，在任务管理器中确认所有浏览器相关进程均已退出。

2. 备份原Chrome/Edge快捷方式，右键单击图标，依次选择`属性` - `快捷方式`。

3. 在`目标`中原有内容后依次追加一个空格 和`--kiosk-printing`，下方是一个样例。
   
   ```textile
   "C:\Program Files\Google\Chrome\Application\chrome.exe" --kiosk-printing
   ```

4. 点击`应用`，双击修改后的快捷方式，确认打印时不再显示预览对话框。

### 队伍导入

上传的xlsx文件表头应该是：

| examNumber | teamName | school | position | account        | password       |
| ---------- | -------- | ------ | -------- | -------------- | -------------- |
| {考号}       | {队名}     | {学习}   | {座位}     | {Domjudge队伍账号} | {Domjudge队伍密码} |

### 构建

1. 需要先构建`vue`，构建后的文件会自动放入`static`。

2. 构建`jar`。
