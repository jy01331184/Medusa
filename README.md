# Medusa --美杜莎 基于gradle构建的可插拔插件化
##### 安装依赖 ：jdk7(jdk8)、maven3.0.5、python、androidsdk、gradle2.10 并且加入到环境变量中。    
#
#
#
#
##### 该工程由以下几个module组成  

app: 主项目集成 最终可生成apk安装包->android工程  

bundle1：依赖bundle2，拥有activity，service，broadcast等。->android工程    
bundle2：普通bundle，拥有activity，对外提供视图view。->android工程    
bundle3：普通bundle，拥有activity，broadcast，懒加载。->android工程    

MedusaPlugin：对bundle合app提供gradle插件。->groovy工程  
MedusaSdk：提供插件化加载支持的sdk。->android工程

##### 运行方法
1.  android studio导入工程 ，由于app和bundle都依赖于 MedusaPlugin插件，所以先把settings.gradle中其他module移除，只留pluginmodule。然后调用gradle uploadArchives 上传plugin到本地maven仓库中(也可以上传到nexus中，可在build.gradle中配置)。
2.  在setting.gradle中添加sdk项目，调用uploadArchives上传sdk到本地maven仓库中，规则同上。
3.  添加app、bundle1、bundle2、bundle3进来。bundle项目可以直接调用bundle分组下的installBundleLocal或者installBundleRemote来打本地包或者远程包(本地包远程包的概念后面会说)。app项目调用bundle分组下的assemableRapier生成安装包、installRapier生成并安装安装包。








