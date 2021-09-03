package vip.testops.gungnir.user.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.testops.gungnir.user.commons.Response;
import vip.testops.gungnir.user.entities.dto.UserDTO;
import vip.testops.gungnir.user.entities.requests.SignInRequest;
import vip.testops.gungnir.user.entities.requests.SignUpRequest;
import vip.testops.gungnir.user.entities.vto.UserVTO;
import vip.testops.gungnir.user.services.UserService;
import vip.testops.gungnir.user.utils.StringUtil;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign_in")
    public Response<UserVTO> signIn(@Validated @RequestBody SignInRequest request) {
        Response<UserVTO> response = new Response<>();
        userService.doSignIn(request.getEmail(), request.getPassword(), response);
        return response;
    }

    @PostMapping("/sign_up")
    public Response<UserVTO> signUp(@Validated @RequestBody SignUpRequest request) {
        Response<UserVTO> response = new Response<>();
        userService.doSignUp(request.getName(), request.getEmail(), request.getPassword(), response);
        return response;
    }

    @GetMapping("/authorize")
    public Response<UserDTO> authorize(@RequestParam(value = "token", required = false) String token) {
        Response<UserDTO> response = new Response<>();
        if(StringUtil.isEmptyOrNull(token)) {
            response.paramMissError("token");
            return response;
        }
        userService.doAuthorize(token, response);
        return response;
    }
}
