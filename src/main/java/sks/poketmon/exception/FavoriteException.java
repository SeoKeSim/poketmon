package sks.poketmon.exception;

public class FavoriteException extends RuntimeException {

    public FavoriteException(String message) {
        super(message);
    }

    public FavoriteException(String message, Throwable cause) {
        super(message, cause);
    }

    // 이미 즐겨찾기에 추가된 포켓몬
    public static class AlreadyExistsException extends FavoriteException {
        public AlreadyExistsException(Integer pokemonId) {
            super("이미 즐겨찾기에 추가된 포켓몬입니다. (ID: " + pokemonId + ")");
        }
    }

    // 즐겨찾기에 없는 포켓몬
    public static class NotFoundException extends FavoriteException {
        public NotFoundException(Integer pokemonId) {
            super("즐겨찾기에서 찾을 수 없는 포켓몬입니다. (ID: " + pokemonId + ")");
        }
    }

    // 로그인 필요
    public static class AuthenticationRequiredException extends FavoriteException {
        public AuthenticationRequiredException() {
            super("로그인이 필요한 서비스입니다.");
        }
    }
}