package com.mruhwedel

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.server.TestEmbeddedServer
import spock.lang.Specification
import javax.inject.Inject

@MicronautTest
class ApplicationSpec extends Specification {

    @Inject
    EmbeddedApplication<TestEmbeddedServer> application

    void 'test it works'() {
        expect:
        application.running
    }
}
