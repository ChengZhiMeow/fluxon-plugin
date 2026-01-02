# FluxonPlugin

Fluxon 脚本的运行环境插件，为在 Bukkit/BungeeCord/Velocity 服务器上运行 Fluxon 脚本提供完整的运行时支持。

## 构建发行版本

发行版本用于正常使用, 不含 TabooLib 本体。

```
./gradlew build
```

## 构建开发版本

开发版本包含 TabooLib 本体, 用于开发者使用, 但不可运行。

```
./gradlew taboolibBuildApi -PDeleteCode
```

> 参数 -PDeleteCode 表示移除所有逻辑代码以减少体积。