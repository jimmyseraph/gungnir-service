package vip.testops.gungnir.user.services;

import vip.testops.gungnir.user.commons.Response;
import vip.testops.gungnir.user.entities.dto.UserDTO;
import vip.testops.gungnir.user.entities.vto.UserVTO;

public interface UserService {
    void doSignIn(String email, String password, Response<UserVTO> response);
    void doSignUp(String name, String email, String password, Response<UserVTO> response);
    void doAuthorize(String token, Response<UserDTO> response);
}
