package top.techmczs.cuitxcpctool.constant;

/**
 * 提示或者表单信息常量
 */
public class MessageConstant {

    public static final String UNKNOWN_TEAM = "Unknown Team";
    public static final String UNKNOWN_TEAM_POSITION = "Unknown Position";

    public static final String PUSH_BALLOON_TASK_SUCCESS = "Push Balloon Task {} Success!";
    public static final String SKIP_BALLOON_TASK = "Skip Balloon Task!";
    public static final String GET_BALLOON_TASK_SUCCESS = "Get Balloon {} Task: {}.";
    public static final String SET_BALLOON_TASK_DONE_FAILED = "Set Balloon Task done Failed, ID: {}.";
    public static final String TASK_TO_DO = "Todo";
    public static final String TASK_ALL = "All";

    public static final String SYSTEM_ERROR = "System exception: {}";
    public static final String MUST_XLSX = "It must be an xlsx file.";

    public static final String TEAM_NEED_PRINT = "{} need print code!";
    public static final String TEAM_NEED_LOGIN = "{} need login!";

    public static final String SSE_LINK_SUCCESS = "SSE link success!";
    public static final String SEE_CLOSE = "SSE connection closed. Remaining clients: {}";
    public static final String SSE_TIME_OUT = "SSE connection timed out. The client has been removed.";
    public static final String SSE_LINK_FAILED = "SSE connection exception!";
    public static final String SSE_SEND_INIT_MESSAGE_FAILED = "SSE send initial message failed.";
    public static final String SSE_BROADCAST_FAILED = "SSE broadcast failed, event: {}, the client has been removed";

    public static final String ILLEGAL_CLIENT = "{} Authentication failed: Illegal client.";
    public static final String NO_CLIENT_CHECK = "Client check is not enabled.";
    public static final String TOKEN_TIME_OUT = "Token time out!";
    public static final String ILLEGAL_TOKEN = "Authentication failed: Illegal token.";
    public static final String TEAM_NOT_FOUND = "Login team {} not found.";
    public static final String TEAM_LOGIN_AGAIN = "{} Login again.";
    public static final String AUTH_TASK_NOT_EXIST = "Authentication task not exist.";
    public static final String IMPORT_TEAM_FROM_EXCEL = "Import teams from excel failed.";
    public static final String ADD_PRINT_TASK_FAILED = "Add print task failed.";
    public static final String TRANSFER_PRINT_TASK_FAILED = "Transfer print task failed.";
    public static final String GET_BALLOON_ERROR = "Get balloon from domjudge failed. Info: {}";
}
