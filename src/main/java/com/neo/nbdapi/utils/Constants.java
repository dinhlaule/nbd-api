package com.neo.nbdapi.utils;

public interface Constants {

    class APPLICATION_API {
        public static final String API_PREFIX = "/api/v1";

        public class MODULE {
            public static final String URI_LOGIN = "/authenticate";
            public static final String URI_USER_INFO = "/user-info";
            public static final String URI_MAIL_CONFIG = "/mail-config";
            public static final String URI_GROUP_MAIL_RECEIVE = "/group-mail-receive";
            public static final String URI_GROUP_MAIL_RECEIVE_DETAIL = "/group-mail-receive-detail";
            public static final String URI_MENU_MANAGE = "/menu-manage";
            public static final String URI_LOG_ACT = "/log-act";
            public static final String URI_USER_MANAGER = "/user-manager";
            public static final String URI_STATION = "/station";
            public static final String URI_VALUE_TYPES = "/value-type";
            public static final String URI_CONFIG_VALUE_TYPES = "/config-value-type";
            public static final String URI_CONFIG_WARNING_THRESHOLD = "/warning-thresold";
            public static final String URI_CHANGER_PASS = "/changer-pass";
            public static final String URI_CONFIG_WARNING_THRESHOLD_STATION = "/warning-threshold-station";
            public static final String URI_CONFIG_WARNING_MANAGER_STATION = "/warning-manager-station";
            public static final String URI_MANAGER_OUTPUTS = "/management-of-outputs";
            public static final String URI_GROUP_MAIL_CONFIG = "/group-mail-config";
            public static final String URI_CDH_HISTORY = "/cdh-history";
            public static final String URI_SEND_MAIL_HISTORY = "/send-mail-history";
            public static final String URI_CONFIG_WATER_LEVEL = "/water-level";
            public static final String URI_CONFIG_USER_EXPAND = "/user-expand";
            public static final String URI_CONFIG_EMAIL = "/email";
            public static final String URI_USER_EXPAND = "/user-expand";

            // module report
            public static final String URI_REPORT = "/report";
            public static final String URI_CASSBIN_RULE = "/casbin-rule";
            public static final String URI_NOTIFICATION = "/notification";
            public static final String URI_DATA = "/data";
            public static final String OBJECT_TYPE = "/object-type";
        }
    }

    class EXCEPTION {
        public static final int BAD_REQUEST = 400;
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    class LOGGER {
        public static final String MAKER_LOG_ACTION_CRUD = "LOG_ACTION_CRUD";
    }

    class ConstantParams{
        public static final String SPLIT_CHARACTER = ",";
        public static final String SPLIT_FILE_CHARACTER = "\\.";

        public static final String ENCODE_UTF8 = "UTF-8";
        public static final String ERROR_VIEW_NAME = "error";
        public static final int DEFAULT_COOKIE_EXPIRE_DATE = 1 * 24 * 60 * 60;
        public static final int INITIAL_PAGE = 0;
        public static int PAGE_SIZE = 10;
        public static int BUTTONS_TO_SHOW = 5;
        public static int[] PAGE_SIZES = { 5, 10, 20 };

        public static String SECRET_KEY_PATH = "ZDf+fWqwFNUPelJpn87uZQ==";

         public static String sqlFile = "sql.properties";
//        public static String sqlFile = "static/sql.properties";
        public static String LOG_CONFIG_FILE = "log4j.properties";
//        public static String LOG_CONFIG_FILE = "static/log4j.properties";
        //ConstantParams.class.getClassLoader().getResource("log4j.properties").getPath();
        // public static String sqlFile = "sql.properties";
        public static final int REFRESH_DELAY = 2 * 1000;
        //	public static final int REFRESH_DELAY = 1000;
    }

    class LOG_ACT {
        public static final String FILE_NAME_EXPORT_LOG_ACT = "log_act";

        public static final String ACTION_CREATE = "CREATE";
        public static final String ACTION_EDIT = "EDIT";
        public static final String ACTION_DELETE = "DELETE";
        public static final String ACTION_EXPORT= "EXPORT";
        public static final String ACTION_DOWNLOAD_FILE = "DOWNLOAD_FILE";
        public static final String ACTION_LOGIN = "LOGIN";
    }
    class LOG_CDH {
        public static final String FILE_NAME_EXPORT_LOG_CDH= "LOG_CDH";
    }

    class  WATER_LEVEL{
        //id c???a y???u t??? m???c n?????c
        public static final String PARAMETER_TYPE_ID = "80";
        // th?? m???c c???u h??nh ?????y file d??? li???u chay
        public static final String FOLDER_EXPORT = "/water_level";

        public static final String ID_PHU_QUOC = "9_59_492_402";
        public static final String ID_GANH_HAO = "9_63_-1_404";
        public static final String ID_HA_TIEN = "9_59_482_403";

        public static final String FILE_PHU_QUOC = "phu_quoc";

        public static final String FILE_GANH_HAO = "ganh_hao";
        public static final String FILE_HA_TIEN = "ha_tien";

        public static final String  FILE_EXECUTE_GUESS = "stations.dat";

        public static final  String REGEX_FILE_UPLOAD = ".*.dat$";
        public static final String FILE_CONFIG = "tides.cfg";
        public static final String URL_GUESS = "http://192.168.1.20:8082/water-level/guess";
        public static final String URL_EXECUTE = "http://192.168.1.20:8082/water-level/excute";
    }

    class STATION {
        public static final int ACTIVE = 1;
        public static final int IS_DEL_FALSE = 0;
        public static final int IS_DEL_TRUE = 1;
        public static final int IN_ACTIVE = 0;
    }

    class USER_INFO {

        public static final int IS_DELETE_FALSE = 0;
        public static final int STATUS_ACTIVE = 1;
        public static final int CHECK_ROLE_NOT_FOUNT = -1;
        public static final int CHECK_ROLE_OK= 1;
        public static final int CHECK_ROLE_NOT_OK = 0;
    }
    class URI_RESTEMPLATE {
        public static final String URI_WATER_LEVEL = "http://192.168.1.20/:8082/";
    }

    class MENU {
        public static final String ACTION_VIEW_MENU = "xem";
        public static final int PUBLISH_OK = 1;
        public static final int PUBLISH_NOT_OK = 0;
    }
}
