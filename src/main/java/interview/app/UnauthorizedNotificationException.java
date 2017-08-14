package interview.app;

class UnauthorizedNotificationException extends Exception {
    public UnauthorizedNotificationException(String msg) {
        super(msg);
    }
}