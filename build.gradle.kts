plugins {
    java
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.anthropic:anthropic-java:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.samskivert:jmustache:1.15")

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

fun registerDemo(taskName: String, fullyQualifiedClass: String) {
    tasks.register<JavaExec>(taskName) {
        group = "demos"
        description = "Run $fullyQualifiedClass"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(fullyQualifiedClass)
        doFirst {
            environment("ANTHROPIC_API_KEY", findProperty("anthropicApiKey")
                ?: System.getenv("ANTHROPIC_API_KEY")
                ?: error("Set anthropicApiKey in gradle.properties or export ANTHROPIC_API_KEY"))
        }
    }
}

// Module 1
registerDemo("runChatHistoryDemo",  "com.example.api.ChatHistoryDemo")
registerDemo("runModelOutputDemo",  "com.example.api.ModelOutputDemo")
registerDemo("runStreamingDemo",    "com.example.api.StreamingDemo")
registerDemo("runSystemPromptDemo", "com.example.api.SystemPromptDemo")
registerDemo("runTemperatureDemo",  "com.example.api.TemperatureDemo")
registerDemo("runStopSequenceDemo", "com.example.api.StopSequenceDemo")
registerDemo("runStructuredData",   "com.example.api.StructuredDataDemo")

// Module 2
registerDemo("runPromptEvaluation", "com.example.evaluation.PromptEvaluation")

// Module 3
registerDemo("runPromptEngineering",  "com.example.engineering.PromptEngineering")

// Module 4
registerDemo("runBasicToolDemo", "com.example.tooluse.BasicToolDemo")
registerDemo("runMultiToolDemo", "com.example.tooluse.MultiToolDemo")

// Module 5
registerDemo("runChunkingDemo",  "com.example.rag.ChunkingDemo")
registerDemo("runTextEmbeddingsDemo",  "com.example.rag.TextEmbeddingsDemo")
registerDemo("runRagWorkflowDemo",    "com.example.rag.RagWorkflowDemo")

// Module 6
registerDemo("runExtendedThinkingDemo",  "com.example.thinking.ExtendedThinkingDemo")
registerDemo("runImageDemo", "com.example.features.ImageDemo")
registerDemo("runPdfDemo", "com.example.features.PdfDemo")
registerDemo("runPdfCitationDemo", "com.example.features.PdfCitationsDemo")
registerDemo("runTextCitationDemo", "com.example.features.TextCitationsDemo")
registerDemo("runSystemPromptCachingDemo", "com.example.features.SystemPromptCachingDemo")
registerDemo("runUserPromptCachingDemo", "com.example.features.UserPromptCachingDemo")
registerDemo("runFilesApiDemo",        "com.example.features.FilesApiImageDemo")
registerDemo("runCodeExecutionDemo",   "com.example.features.CodeExecutionDemo")