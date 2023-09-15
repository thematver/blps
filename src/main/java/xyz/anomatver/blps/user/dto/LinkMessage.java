package xyz.anomatver.blps.user.dto;

public class LinkMessage {

    private Long userId;
    private String uuid;


    public LinkMessage(Long userId, String uuid) {
        this.userId = userId;
        this.uuid = uuid;
    }
}