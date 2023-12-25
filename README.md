# FwkDev
本项目提供一种轻量级研究和开发 Android Framework 代码的代码结构方案。

通过配置 build.gradle 索引一些核心的 framework 代码，可以轻量级导入本地，供 framework 代码学习和分析，对 Android Studio 可以很好的支持代码提示和跳转。

>本项目解决的问题是： 1.应用开发： 新的芯片项目应用做兼容，只需要提取Framework轻量级结构部分。  2. 系统Feature开发: 同时兼容不同平台，配置需要的核心源码即可。   3.系统源码阅读，对比差异，竞品分析等，只需要关注核心代码，不需要完整导入整套Android。

## 项目结构
本项目分为3块:
- 对Android原生代码的索引的分支: `android-14.0.0_r17`, `android-13.0.0_r52`等。
- 主分支 `main` 是Android Framework代码索引的结构。
- `dev`分支，是需要一定配置实现从远程服务器提取文件以及pull、push到设备的一套轻量级高效的Android Framework开发方式。

## 阅读指定代码
- 拉取指定分支， 如: 拉取Android13源码
```bash
git clone https://github.com/10cl/fwkdev.git -b android-13.0.0_r52
```
Android Studio 打开项目，即可以尝试阅读和分析Framework源码。
![](screenshots/read_eg.png)

## Framework开发环境
通过配置拉取本地项目，实现对本地设备调试断点，通过脚本简化Android Framework开发流程: 本地代码编写->同步到编译环境->编译->提取out生成文件->push到设备->重启更新。

### 索引你自己的项目
1. 初始化你项目名称
建立一个`project`，然后里面存放你的系统项目，遵循你内部或者你自定的某种标准定义，如`{芯片平台_型号_Android版本_XXX}`:
```
->project $ ls
QCOM_SM8650_14.0_xxx_xxx/
QCOM_qm215_10.0_xxx_xxx/
QCOM_sdm660_10.0_xxx_xxx/
SPRD_t610_13.0_xxx_xxx/
SPRD_9863_10.0_xxx_xxx/
SPRD_9863_11.0_xxx_xxx/
SPRD_9863_9.x_5.0_xxx_xxx/
SPRD_ums9620_11.0_xxx_xxx/
mtk_6765_10.0_xxx_xxx/
mtk_6769_11.0_xxx_xxx/
mtk_6771_11.0_xxx_xxx/
mtk_6833_11.0_xxx_xxx/
mtk_6877_11.0_xxx_xxx/
```
拉取仓库
```bash
git clone https://github.com/10cl/fwkdev.git -b dev QCOM_SM8650_14.0_xxx_xxx
```

### 配置项
项目通过 `ssh` 模块连接远程服务器用来获取远程服务器上的Framework文件，以及`out`下用来`push`的生成文件，Windows 环境下，如果下载配置了`Git` 默认包含该模块。

#### 设置本地无密码登录
`ssh-copy-id` 是一个方便的命令行工具，用于将本地计算机上的 SSH 公钥复制到远程计算机的 `authorized_keys` 文件中，以便实现无密码登录。在使用 SSH 远程登录时，通常需要输入密码进行身份验证，但通过配置 SSH 公钥认证，可以实现更安全且更方便的身份验证方式。

使用 `ssh-copy-id` 的步骤通常如下：

1. 生成 SSH 密钥对：使用 `ssh-keygen` 命令生成本地计算机上的 SSH 密钥对，包括公钥和私钥。

    ```bash
    ssh-keygen -t rsa
    ```

   此命令将在 `~/.ssh/` 目录下生成 `id_rsa`（私钥）和 `id_rsa.pub`（公钥）文件。

2. 使用 `ssh-copy-id` 将公钥复制到远程主机：运行以下命令，将本地计算机上的公钥添加到远程计算机的 `authorized_keys` 文件中。

    ```bash
   ssh-copy-id -i ~/.ssh/id_rsa.pub {用户名}@{服务器IP} -p{端口}
    ```

   这将要求输入远程计算机的用户密码，然后将本地计算机上的公钥复制到远程计算机的 `~/.ssh/authorized_keys` 文件中。以后，就可以使用 SSH 无密码登录了。

#### local.properties
- 基础配置  
基础配置完毕后，即可通过执行`pullFwk`任务拉取远程系统核心代码。
```properties
# 保留原项目配置
# ...

### FwkDev Config: Server Config ###
server.path={项目根目录}
server.ip={用户名}@{服务器IP}
server.port={端口}
```

- 变更提交  
通过新增`server.diff` 用于配置需要上传的变更文件关键词，然后通过执行`pushFwk`上传过滤后的变更文件。
```properties
# 用于过滤变更文件的完整路径上包含下面关键字的
server.diff=framework;
```

- 设备更新  
通过配置`local.pushfwk=true` 以及 `server.project={项目标识}` 通过执行`pushUpdateJar`下载远程的 /system/framework/ 下的生成文件到本地， 并通过adb push 将生成文件更新到设备上后重启。
```properties
### FwkDev Config: Push Jar file Config ###
local.pushfwk=true
# Android项目编译生成文件一般在 out/target/product/{项目标识}/system/framework/
server.project={项目标识}
```

- 多项目对比  
将 Beyond Compare 加入环境变量后，通过执行`compareFile`任务，执行本地 `BComp`，对远程的当前项目`server.path` 和 对比项目`server.compare_folder`对比 `server.compare` 配置的相对路径。
```properties
### FwkDev Config: Compare fold configuration ###
# 配置需要对比的平台根目录
server.compare_folder=/home/10cl/project/QCOM_SM8650_14.0_xxx_xxx/
# 配置需要对比的相对路径
server.compare=frameworks/base/services/core/java/com/android/server/wm/
```

- 下载单独的文件   
通过执行`pullServerFile`任务拉取服务器指定文件，配置`SERVER_FILE_LIST`变量即可.