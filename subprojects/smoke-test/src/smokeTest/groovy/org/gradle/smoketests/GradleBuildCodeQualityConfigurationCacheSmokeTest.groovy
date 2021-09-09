/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.smoketests

import org.gradle.testkit.runner.TaskOutcome

class GradleBuildCodeQualityConfigurationCacheSmokeTest extends AbstractGradleBuildConfigurationCacheSmokeTest {
    def "can run Gradle codeQuality with configuration cache enabled"() {

        given:
        def tasks = [':configuration-cache:codeQuality']

        when:
        configurationCacheRun(tasks, 0)

        then:
        assertConfigurationCacheStateStored()

        when:
        run([":configuration-cache:clean"])

        then:
        configurationCacheRun(tasks, 1)

        then:
        assertConfigurationCacheStateLoaded()
        result.task(":configuration-cache:runKtlintCheckOverMainSourceSet").outcome == TaskOutcome.FROM_CACHE
        result.task(":configuration-cache:validatePlugins").outcome == TaskOutcome.SUCCESS
        result.task(":configuration-cache:codenarcIntegTest").outcome == TaskOutcome.FROM_CACHE
        result.task(":configuration-cache:checkstyleIntegTestGroovy").outcome == TaskOutcome.SUCCESS
        result.task(":configuration-cache:classycleIntegTest").outcome == TaskOutcome.FROM_CACHE
        result.task(":configuration-cache:codeQuality").outcome == TaskOutcome.SUCCESS
    }
}
