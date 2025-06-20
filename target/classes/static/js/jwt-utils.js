// JWT 토큰 관리 유틸리티 - 디버깅 강화 버전
const JwtUtils = {
    // 토큰 저장 (localStorage + 쿠키 동시 저장)
    setToken(token) {
        if (!token) {
            console.warn('빈 토큰은 저장할 수 없습니다');
            return false;
        }

        try {
            console.log('토큰 저장 시작:', token.substring(0, 20) + '...');

            // localStorage에 저장
            localStorage.setItem('jwtToken', token);
            console.log('localStorage 저장 완료');

            // 쿠키에도 저장 (서버에서 읽을 수 있도록)
            const expires = new Date();
            expires.setTime(expires.getTime() + (24 * 60 * 60 * 1000)); // 24시간

            // 다양한 쿠키 설정 시도
            const cookieConfigs = [
                // 기본 설정
                `jwtToken=${encodeURIComponent(token)}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`,
                // Secure 없이 (HTTP 환경용)
                `authToken=${encodeURIComponent(token)}; expires=${expires.toUTCString()}; path=/`,
                // 단순 설정
                `jwt=${token}; path=/`
            ];

            cookieConfigs.forEach((cookieString, index) => {
                document.cookie = cookieString;
                console.log(`쿠키 설정 ${index + 1}:`, cookieString);
            });

            // 쿠키 설정 확인
            setTimeout(() => {
                console.log('설정 후 모든 쿠키:', document.cookie);
                this.debugTokenStatus();
            }, 100);

            return true;
        } catch (error) {
            console.error('토큰 저장 실패:', error);
            return false;
        }
    },

    // 토큰 가져오기 - 개선된 버전
    getToken() {
        try {
            // localStorage에서 먼저 확인
            let token = localStorage.getItem('jwtToken');
            if (token && token.trim() !== '') {
                console.log('localStorage에서 토큰 발견');
                return token;
            }

            // 쿠키에서 확인 (여러 이름으로 시도)
            const cookieNames = ['jwtToken', 'authToken', 'jwt'];
            for (const name of cookieNames) {
                token = this.getTokenFromCookie(name);
                if (token) {
                    console.log(`쿠키 ${name}에서 토큰 발견`);
                    // 쿠키에서 찾았으면 localStorage에도 동기화
                    localStorage.setItem('jwtToken', token);
                    return token;
                }
            }
        } catch (error) {
            console.error('토큰 가져오기 실패:', error);
        }

        console.log('토큰을 찾을 수 없음');
        return null;
    },

    // 쿠키에서 토큰 추출 - 이름 지정 가능
    getTokenFromCookie(cookieName = 'jwtToken') {
        try {
            const cookies = document.cookie.split(';');
            for (let cookie of cookies) {
                const [name, value] = cookie.trim().split('=');
                if (name === cookieName && value) {
                    const decodedValue = cookieName === 'jwt' ? value : decodeURIComponent(value);
                    console.log(`쿠키 ${cookieName}에서 토큰 추출:`, decodedValue.substring(0, 20) + '...');
                    return decodedValue;
                }
            }
        } catch (error) {
            console.error(`쿠키 ${cookieName}에서 토큰 추출 실패:`, error);
        }
        return null;
    },

    // 토큰 삭제 - 개선된 버전
    removeToken() {
        try {
            console.log('토큰 삭제 시작');

            // localStorage와 sessionStorage에서 제거
            localStorage.removeItem('jwtToken');
            sessionStorage.removeItem('jwtToken');
            console.log('스토리지에서 토큰 제거 완료');

            // 모든 쿠키 삭제
            const cookieNames = ['jwtToken', 'authToken', 'jwt'];
            cookieNames.forEach(name => {
                document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`;
                document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; domain=${window.location.hostname}`;
            });

            console.log('쿠키 삭제 완료');
            console.log('삭제 후 쿠키:', document.cookie);
        } catch (error) {
            console.error('토큰 삭제 실패:', error);
        }
    },

    // API 요청에 토큰 헤더 추가
    getAuthHeaders() {
        const token = this.getToken();
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
            headers['X-Auth-Token'] = token; // 추가 헤더
        }

        console.log('인증 헤더 생성:', Object.keys(headers));
        return headers;
    },

    // fetch 요청 래퍼 (자동으로 토큰 헤더 추가)
    async authFetch(url, options = {}) {
        try {
            console.log('API 요청 시작:', url);

            const headers = {
                ...this.getAuthHeaders(),
                ...(options.headers || {})
            };

            console.log('요청 헤더:', headers);

            const response = await fetch(url, {
                ...options,
                headers,
                credentials: 'include' // 쿠키 포함
            });

            console.log('응답 상태:', response.status);

            // 401 에러면 토큰이 만료되었으므로 로그인 페이지로 이동
            if (response.status === 401) {
                console.warn('인증 실패 (401), 토큰 제거 후 로그인 페이지로 이동');
                this.removeToken();
                window.location.href = '/users/login';
                return null;
            }

            return response;
        } catch (error) {
            console.error('API 요청 실패:', error);
            throw error;
        }
    },

    // 페이지 이동 시 토큰을 쿠키로 전달 - 개선된 버전
    navigateWithToken(url) {
        console.log('페이지 이동 요청:', url);

        const token = this.getToken();
        if (token) {
            console.log('토큰 확인됨, 페이지 이동 준비');

            // 토큰을 쿠키에 다시 설정하여 확실히 전달되도록 함
            const success = this.setToken(token);

            if (success) {
                // 쿠키 설정 완료를 위한 지연
                setTimeout(() => {
                    console.log('페이지 이동 실행:', url);
                    window.location.href = url;
                }, 200);
            } else {
                console.error('토큰 설정 실패');
                alert('토큰 설정에 실패했습니다. 다시 로그인해주세요.');
                window.location.href = '/users/login';
            }
        } else {
            console.log('토큰이 없어 로그인 페이지로 이동');
            alert('로그인이 필요합니다.');
            window.location.href = '/users/login';
        }
    },

    // 로그인 후 토큰 검증
    async validateToken() {
        try {
            console.log('토큰 검증 시작');
            const response = await this.authFetch('/api/auth/validate');
            if (response && response.ok) {
                const result = await response.json();
                console.log('토큰 검증 결과:', result);
                return result.success;
            }
            console.log('토큰 검증 실패: 응답 없음 또는 오류');
            return false;
        } catch (error) {
            console.error('토큰 검증 실패:', error);
            return false;
        }
    },

    // 토큰 상태 디버깅 정보
    debugTokenStatus() {
        const localToken = localStorage.getItem('jwtToken');
        const cookieToken = this.getTokenFromCookie('jwtToken');
        const authToken = this.getTokenFromCookie('authToken');
        const jwtCookie = this.getTokenFromCookie('jwt');

        console.log('========================');
        console.log('=== JWT 토큰 상태 디버깅 ===');
        console.log('========================');
        console.log('LocalStorage jwtToken:', localToken ? '존재 (' + localToken.length + '자)' : '없음');
        console.log('Cookie jwtToken:', cookieToken ? '존재' : '없음');
        console.log('Cookie authToken:', authToken ? '존재' : '없음');
        console.log('Cookie jwt:', jwtCookie ? '존재' : '없음');
        console.log('현재 모든 쿠키:', document.cookie);
        console.log('현재 도메인:', window.location.hostname);
        console.log('현재 프로토콜:', window.location.protocol);
        console.log('========================');

        return {
            localStorage: localToken,
            cookies: {
                jwtToken: cookieToken,
                authToken: authToken,
                jwt: jwtCookie
            },
            allCookies: document.cookie,
            domain: window.location.hostname,
            protocol: window.location.protocol
        };
    },

    // 강제 토큰 테스트 (개발용)
    testTokenSetting() {
        console.log('토큰 설정 테스트 시작');
        const testToken = 'test-token-' + Date.now();

        console.log('테스트 토큰:', testToken);
        this.setToken(testToken);

        setTimeout(() => {
            const retrieved = this.getToken();
            console.log('검색된 토큰:', retrieved);
            console.log('테스트 결과:', retrieved === testToken ? '성공' : '실패');

            // 테스트 토큰 제거
            this.removeToken();
        }, 500);
    }
};

// 페이지 로드 시 토큰 검증 - 디버깅 강화
document.addEventListener('DOMContentLoaded', async function() {
    console.log('=== 페이지 로드 완료 ===');
    console.log('현재 URL:', window.location.href);

    // 토큰 상태 디버깅
    const debugInfo = JwtUtils.debugTokenStatus();

    const token = JwtUtils.getToken();
    if (token) {
        console.log('토큰 발견, 유효성 검증 시도 중...');

        // 토큰 검증 API가 없다면 스킵
        try {
            const isValid = await JwtUtils.validateToken();
            if (!isValid) {
                console.log('토큰이 유효하지 않아 제거됨');
                JwtUtils.removeToken();
            } else {
                console.log('유효한 토큰 확인됨');
            }
        } catch (error) {
            console.log('토큰 검증 API 호출 실패, 토큰은 유지:', error.message);
        }
    } else {
        console.log('토큰이 없음 - 로그인 필요');
    }
});

// 즐겨찾기 페이지로 이동하는 함수 - 디버깅 강화
function goToFavorites() {
    console.log('=== 즐겨찾기 이동 요청 ===');

    // 토큰 상태 상세 확인
    JwtUtils.debugTokenStatus();

    const token = JwtUtils.getToken();
    if (token) {
        console.log('토큰 확인됨, 즐겨찾기 페이지로 이동 시도');
        JwtUtils.navigateWithToken('/jwt/favorites');
    } else {
        console.log('토큰 없음, 로그인 페이지로 이동');
        alert('로그인이 필요합니다.');
        window.location.href = '/users/login';
    }
}

// JWT 로그아웃 함수
function jwtLogout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        console.log('로그아웃 처리 중...');
        JwtUtils.removeToken();
        window.location.href = '/users/login';
    }
}

// 개발용 테스트 함수들
function testJwtToken() {
    JwtUtils.testTokenSetting();
}

function debugJwtStatus() {
    return JwtUtils.debugTokenStatus();
}

// 전역으로 export
if (typeof window !== 'undefined') {
    window.JwtUtils = JwtUtils;
    window.goToFavorites = goToFavorites;
    window.jwtLogout = jwtLogout;
    window.testJwtToken = testJwtToken;  // 개발용
    window.debugJwtStatus = debugJwtStatus;  // 개발용
}