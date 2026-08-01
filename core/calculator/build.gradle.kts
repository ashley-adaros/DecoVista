plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass.set("com.decovista.calculator.MainKt")
}

dependencies {
    // Kotlin Puro - Sin dependencias de Android
    testImplementation(libs.junit)
}
