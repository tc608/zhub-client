package net.tccn.zhub;

// ================================================== lock ==================================================
public class Lock {
    protected String name;
    protected String uuid;
    protected int duration;
    protected boolean success;
    protected ZHubClient hubClient;

    protected Lock(String name, String uuid, int duration, ZHubClient hubClient) {
        this.name = name;
        this.uuid = uuid;
        this.duration = duration;
        this.hubClient = hubClient;
    }

    public void unLock() {
        hubClient.send("unlock", name, uuid);
    }

    public boolean success() {
        return success;
    }
}
