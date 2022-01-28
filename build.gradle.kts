import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
    `java-gradle-plugin`
}

group = "com.zconger"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "com.zconger.greeting"
            implementationClass = "com.zconger.GreetingPlugin"
        }
    }
}

class GreetingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<GreetingPluginExtension>("greeting")
        val extensor = project.extensions.create<GreetingPluginExtensor>("greetinations")
        project.task("hello") {
            doLast {
                println("Hello from the GreetingPlugin.")
//                println(extension.message.get())
//                println("${extensor.message.get()} from ${extensor.greeter.get()}!")
            }
        }
    }
}

// This is to make the GreetingPlugin configurable
abstract class GreetingPluginExtension {
    abstract val message: Property<String>

    init {
        message.convention("Hello from GreetingPlugin.")
    }
}

interface GreetingPluginExtensor {
    val message: Property<String>
    val greeter: Property<String>
}

// Apply the GreetingPlugin to make its tasks available to this build script
apply<GreetingPlugin>()

// Configure GreetingPlugin in a roundabout way through the GreetingPluginExtension, where the configurable
// things are.
configure<GreetingPluginExtension> {
    message.set("Hi there!")
}

configure<GreetingPluginExtensor> {
    message.set("Hey buddy")
    greeter.set("Gradle")
}

/**
 * Write something to a file as a task
 */

abstract class GreetingToFileTask: DefaultTask() {
    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    fun greet() {
        val file = destination.get().asFile
        file.parentFile.mkdirs()
        file.writeText("Greetings from GreetingToFileTask!")
    }
}

val greetingFile = objects.fileProperty()

tasks.register<GreetingToFileTask>("greet") {
    destination.set(greetingFile)
}

// Here's the sayGreeting task! `./gradlew -q sayGreeting`
tasks.register("sayGreeting") {
    dependsOn("greet")  // Kick off the "greet" task to fill in the filename at the last moment
    doLast {
        val file = greetingFile.get().asFile
        println("${file.readText()} (${file.name})")
    }
}

// Put the greeting in ./build/hello.txt
greetingFile.set(layout.buildDirectory.file("hello.txt"))

tasks.forEach<Task> {
    println(it)
}
