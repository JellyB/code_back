package com.huatu.ztk.user.galaxy.report;

/**
 * @author zhengyi
 * @date 2019-01-11 19:06
 **/
public class UserGetCaptchaContext {

    private static final ThreadLocal<UserGetCaptchaEvent> CONTEXT = ThreadLocal.withInitial(UserGetCaptchaEvent::new);

    public static UserGetCaptchaEvent get() {
        return CONTEXT.get();
    }

    public static class UserGetCaptchaEvent extends Event {
        /**
         * create terminal
         */
        private int terminal;
        /**
         * create captcha
         */
        private String captcha;
        /**
         * phone
         */
        private String phone;
        /**
         * client version
         */
        private String cv;

        public String getCaptcha() {
            return captcha;
        }

        public void setCaptcha(String captcha) {
            this.captcha = captcha;
        }

        public String getCv() {
            return cv;
        }

        public void setCv(String cv) {
            this.cv = cv;
        }

        public int getTerminal() {
            return terminal;
        }

        public void setTerminal(int terminal) {
            this.terminal = terminal;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public static String GET_CAPTCHA = "get_captcha";
        public static String CHECK_CAPTCHA = "check_captcha";
    }

}