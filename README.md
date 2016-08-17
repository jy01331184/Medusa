# Medusa --美杜莎 基于gradle构建的可插拔插件化
##### 该工程由以下几个module组成  

app: 主项目集成 最终可生成apk安装包->android工程  

bundle1：依赖bundle2，拥有activity，service，broadcast等。->android工程    
bundle2：普通bundle，拥有activity，对外提供视图view。->android工程    
bundle3：普通bundle，拥有activity，broadcast，懒加载。->android工程    

MedusaPlugin：对bundle合app提供gradle插件。->groovy工程  
MedusaSdk：提供插件化加载支持的sdk。->android工程
