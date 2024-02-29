package org.choongang.commons.exceptions;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends CommonException {
    public UnAuthorizedException() {
        super("UnAuthorized", HttpStatus.UNAUTHORIZED);
        setMessageCode(true);
    }
}
