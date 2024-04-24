package dev.zhub;

import dev.zhub.client.ZHubClient;
import org.redkale.annotation.Priority;
import org.redkale.cluster.ClusterAgent;
import org.redkale.cluster.ClusterAgentProvider;
import org.redkale.util.AnyValue;

@Priority(1)
public class ZhubProvider implements ClusterAgentProvider {

    @Override
    public boolean acceptsConf(AnyValue config) {
        return new ZHubClient().acceptsConf(config);
    }

    @Override
    public ClusterAgent createInstance() {
        return new ZHubClient();
    }
}
