package com.linagora.openpaas.james;

import org.apache.james.JamesServerBuilder;
import org.apache.james.JamesServerExtension;
import org.apache.james.modules.TestJMAPServerModule;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.linagora.openpaas.james.app.MemoryServer;
import com.linagora.openpaas.james.common.LinagoraEchoMethodContract;

public class MemoryLinagoraEchoMethodTest implements LinagoraEchoMethodContract {
    @RegisterExtension
    static JamesServerExtension jamesServerExtension = new JamesServerBuilder<>(JamesServerBuilder.defaultConfigurationProvider())
        .server(configuration -> MemoryServer.createServer(configuration)
            .overrideWith(new TestJMAPServerModule()))
        .build();
}