import io.izzel.taboolib.gradle.Hytale

taboolib {
    env {
        install(Hytale)
    }
    subproject = true
}


dependencies {
    compileOnly("com.hypixel:hytale-server:1.0.0")
}