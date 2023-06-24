package tccn.zhub;

// ================================================== lock ==================================================
public class Lock {
    private String name;
    private String uuid;
    private int duration;
    private ZHubClient hubClient;

    protected Lock(String name, String uuid, int duration, ZHubClient hubClient) {
        this.name = name;
        this.uuid = uuid;
        this.duration = duration;
        this.hubClient = hubClient;
    }

    public void unLock() {
        hubClient.send("unlock", name, uuid);
    }
}
