package me.mugon.lendit.config.jwt;

/**
 * Reference
 * https://dzone.com/articles/spring-boot-security-json-web-tokenjwt-hello-world
 */
public class JwtConstants {
    public static final String SECRET = "lendit";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER = "Authorization";
    public static final long TOKEN_VALIDITY =  5 * 60 * 60;
}
