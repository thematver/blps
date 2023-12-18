public enum UserState {
    INITIAL,
    HAS_UUID,
    AUTHORIZED,
    BLOCKED,
}

class UserStateResult {
    private final UserState userState;
    private final String uuid;

    public UserStateResult(UserState userState, String uuid) {
        this.userState = userState;
        this.uuid = uuid;
    }

    public UserState getUserState() {
        return userState;
    }

    public String getUuid() {
        return uuid;
    }
}
