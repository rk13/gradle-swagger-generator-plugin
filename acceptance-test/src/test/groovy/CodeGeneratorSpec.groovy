import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

import static Fixture.cleanBuildDir
import static Fixture.placePetstoreYaml

class CodeGeneratorSpec extends Specification {

    GradleRunner runner

    def setup() {
        runner = GradleRunner.create()
            .withProjectDir(new File('code-generator'))
            .withPluginClasspath()
            .forwardOutput()
        cleanBuildDir(runner)
    }

    def 'default tasks should be added into the project'() {
        given:
        runner.withArguments('--stacktrace', 'tasks')

        when:
        def result = runner.build()

        then:
        result.output.contains('generateSwaggerCode -')
        result.output.contains('generateSwaggerCodeHelp -')
    }

    def 'generateSwaggerCode task should generate a code'() {
        given:
        placePetstoreYaml(runner, Fixture.PetstoreYaml.valid)
        runner.withArguments('--stacktrace', 'generateSwaggerCode')

        when:
        def result = runner.build()

        then:
        result.task(':generateSwaggerCode').outcome == TaskOutcome.NO_SOURCE
        result.task(':generateSwaggerCodePetstore').outcome == TaskOutcome.SUCCESS
        new File(runner.projectDir, 'build/swagger-code-petstore/src/main/java/example/api/PetsApi.java').exists()

        when:
        def rerunResult = runner.build()

        then:
        rerunResult.task(':generateSwaggerCode').outcome == TaskOutcome.NO_SOURCE
        rerunResult.task(':generateSwaggerCodePetstore').outcome == TaskOutcome.UP_TO_DATE
    }

    def 'build task should build the generated code'() {
        given:
        placePetstoreYaml(runner, Fixture.PetstoreYaml.valid)
        runner.withArguments('--stacktrace', 'build')

        when:
        runner.build()

        then:
        new File(runner.projectDir, 'build/libs/code-generator.jar').exists()
    }

    def 'generateSwaggerCodePetstoreHelp task should show help'() {
        given:
        runner.withArguments('--stacktrace', 'generateSwaggerCodePetstoreHelp')

        when:
        def result = runner.build()

        then:
        result.output.contains('CONFIG OPTIONS')
    }

}
