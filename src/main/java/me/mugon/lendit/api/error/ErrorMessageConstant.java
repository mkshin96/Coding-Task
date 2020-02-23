package me.mugon.lendit.api.error;

/**
 * 에러 메시지의 type safety를 위해 생성
 */
public class ErrorMessageConstant {
    public static String KEY = "message";
    public static String PRODUCTNOTFOUND = "상품을 찾을 수 없습니다.";
    public static String USERNOTFOUND = "사용자를 찾을 수 없습니다.";
    public static String INVALIDIDORPASSWORD = "아이디 또는 비밀번호가 맞지 않습니다.";
    public static String DUPLICATEDUSER = "중복된 유저입니다.";
    public static String OVERTHELIMIT = "예치금이 부족합니다.";
    public static String SHORTAGEOFGOODS = "재고가 부족합니다.";
    public static String REGISTEREDBYONESELF = "자신이 등록한 상품은 주문할 수 없습니다.";
    public static String INVALIDUSER = "상품을 등록한 사용자가 아닙니다.";
}
